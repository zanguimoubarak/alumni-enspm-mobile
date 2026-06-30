package com.enspm.alumni.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.enspm.alumni.core.ui.*
import com.enspm.alumni.feed.domain.*

@Composable
fun FeedRoute(onPostClick: (Long) -> Unit, viewModel: FeedViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeedScreen(state, viewModel::refresh, viewModel::loadNextPage, viewModel::toggleLike, onPostClick)
}

@Composable
fun FeedScreen(state: FeedUiState, onRefresh: () -> Unit, onLoadMore: () -> Unit, onLike: (FeedPost) -> Unit, onPostClick: (Long) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState.firstVisibleItemIndex, state.posts.size) {
        if (state.posts.isNotEmpty() && listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 >= state.posts.lastIndex - 2) onLoadMore()
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FeedHeader(onRefresh, state.refreshing)
        state.message?.let { LiteMessageBanner(it, MessageTone.Error, Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
        if (state.loading && state.posts.isEmpty()) SkeletonFeed() else LazyColumn(state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.posts, key = { it.id }) { post -> PostCard(post, state.dataSaver, { onLike(post) }, { onPostClick(post.id) }) }
            if (state.paginating) item { Text("Chargement de la suite…", Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun FeedHeader(onRefresh: () -> Unit, refreshing: Boolean) {
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text("Fil d’actualité", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("PUBLICATIONS ALUMNI", style = MaterialTheme.typography.labelSmall, color = EnspmMuted) }
        OutlinedButton(onClick = onRefresh, enabled = !refreshing) { Text(if (refreshing) "Sync…" else "Actualiser") }
    }
}

@Composable
fun PostCard(post: FeedPost, dataSaver: Boolean, onLike: () -> Unit, onOpen: () -> Unit) {
    LiteCard(Modifier.clickable(onClick = onOpen)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(EnspmBlue.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Text(post.authorName.take(1), color = EnspmBlue, fontWeight = FontWeight.Bold) }
            Column { Text(post.authorName, fontWeight = FontWeight.SemiBold); Text(post.createdAt.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Text(post.content.ifBlank { "Publication sans texte" }, style = MaterialTheme.typography.bodyMedium)
        post.media.firstOrNull()?.let { FeedThumbnail(it, dataSaver) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onLike) { Text((if (post.isLiked) "Aimé" else "J’aime") + " · ${post.likesCount}") }
            Text("${post.commentsCount} commentaires", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FeedThumbnail(media: FeedMedia, dataSaver: Boolean) {
    Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)).background(EnspmBorder), contentAlignment = Alignment.Center) {
        if (dataSaver) Text("Image masquée · économie de données", color = EnspmMuted) else AsyncImage(model = media.thumbnailUrl, contentDescription = "Miniature de publication", modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun PostDetailRoute(onBack: () -> Unit, viewModel: PostDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PostDetailScreen(state, onBack, viewModel::toggleLike, viewModel::onCommentChange, viewModel::sendComment)
}

@Composable
fun PostDetailScreen(state: PostDetailUiState, onBack: () -> Unit, onLike: () -> Unit, onComment: (String) -> Unit, onSend: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { OutlinedButton(onClick = onBack) { Text("Retour") } }
        state.message?.let { item { LiteMessageBanner(it, if (state.fieldErrors.isEmpty()) MessageTone.Info else MessageTone.Error) } }
        state.post?.let { item { PostCard(it, state.dataSaver, onLike, {}) } }
        item {
            LiteCard {
                Text("COMMENTAIRES", style = MaterialTheme.typography.labelSmall, color = EnspmMuted)
                LiteTextField(state.commentDraft, onComment, "Votre commentaire", error = state.fieldErrors["content"] ?: state.fieldErrors["commentable_id"])
                GradientPrimaryButton("Publier", state.sending, onSend)
            }
        }
        items(state.comments, key = { it.id }) { c -> LiteCard { Text(c.authorName, fontWeight = FontWeight.SemiBold); Text(c.content); Text(c.createdAt.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable private fun SkeletonFeed() { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { repeat(4) { Box(Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(20.dp)).background(EnspmBorder.copy(alpha = .55f))) } } }
