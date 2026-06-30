package com.enspm.alumni.feed.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enspm.alumni.core.datastore.SessionDataStore
import com.enspm.alumni.core.networking.ApiResult
import com.enspm.alumni.feed.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(val posts: List<FeedPost> = emptyList(), val loading: Boolean = true, val refreshing: Boolean = false, val paginating: Boolean = false, val message: String? = null, val dataSaver: Boolean = false)
data class PostDetailUiState(val post: FeedPost? = null, val comments: List<FeedComment> = emptyList(), val commentDraft: String = "", val loading: Boolean = true, val sending: Boolean = false, val message: String? = null, val fieldErrors: Map<String, String> = emptyMap(), val dataSaver: Boolean = false)

@HiltViewModel
class FeedViewModel @Inject constructor(private val repository: FeedRepository, dataStore: SessionDataStore) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { combine(repository.observeFeed(), dataStore.dataSaverFlow) { posts, saver -> posts to saver }.collect { (posts, saver) -> _uiState.update { it.copy(posts = posts, loading = false, dataSaver = saver) } } }
        refresh()
    }
    fun refresh() = viewModelScope.launch { _uiState.update { it.copy(refreshing = true, message = null) }; handle(repository.refreshFirstPage()) { it.copy(refreshing = false) } }
    fun loadNextPage() = viewModelScope.launch { if (_uiState.value.paginating) return@launch; _uiState.update { it.copy(paginating = true, message = null) }; handle(repository.loadNextPage()) { it.copy(paginating = false) } }
    fun toggleLike(post: FeedPost) = viewModelScope.launch { handle(repository.toggleLike(post)) { it } }
    private fun handle(result: ApiResult<Unit>, end: (FeedUiState) -> FeedUiState) { _uiState.update { s -> end(if (result is ApiResult.Failure) s.copy(message = result.error.message) else s) } }
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(private val repository: FeedRepository, dataStore: SessionDataStore, savedStateHandle: SavedStateHandle) : ViewModel() {
    private val postId: Long = checkNotNull(savedStateHandle["postId"])
    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    init {
        viewModelScope.launch { combine(repository.observePost(postId), repository.observeComments(postId), dataStore.dataSaverFlow) { p, c, saver -> Triple(p, c, saver) }.collect { (p, c, saver) -> _uiState.update { it.copy(post = p, comments = c, loading = false, dataSaver = saver) } } }
        viewModelScope.launch { repository.refreshPost(postId); val r = repository.refreshComments(postId); if (r is ApiResult.Failure) _uiState.update { it.copy(message = r.error.message, fieldErrors = r.error.validationErrors) } }
    }
    fun onCommentChange(value: String) = _uiState.update { it.copy(commentDraft = value, fieldErrors = it.fieldErrors - "content") }
    fun sendComment() = viewModelScope.launch {
        val content = _uiState.value.commentDraft.trim(); if (content.isBlank()) return@launch
        _uiState.update { it.copy(sending = true, message = null, fieldErrors = emptyMap()) }
        when (val result = repository.addComment(postId, content)) {
            is ApiResult.Success -> _uiState.update { it.copy(sending = false, commentDraft = "", message = result.message) }
            is ApiResult.Failure -> _uiState.update { it.copy(sending = false, message = result.error.message, fieldErrors = result.error.validationErrors) }
        }
    }
    fun toggleLike() = _uiState.value.post?.let { post -> viewModelScope.launch { repository.toggleLike(post) } } ?: Unit
}
