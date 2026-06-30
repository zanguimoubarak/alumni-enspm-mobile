package com.enspm.alumni.core.di

import com.enspm.alumni.BuildConfig
import com.enspm.alumni.auth.data.AuthApi
import android.content.Context
import androidx.room.Room
import com.enspm.alumni.core.database.AlumniDatabase
import com.enspm.alumni.core.database.FeedDao
import com.enspm.alumni.core.networking.AuthHeaderInterceptor
import com.enspm.alumni.feed.data.CommentsApi
import com.enspm.alumni.feed.data.LikesApi
import com.enspm.alumni.feed.data.PostsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import com.enspm.alumni.core.networking.UnauthorizedInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides @Singleton
    fun provideOkHttpClient(authHeaderInterceptor: AuthHeaderInterceptor, unauthorizedInterceptor: UnauthorizedInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlumniDatabase = Room.databaseBuilder(
        context, AlumniDatabase::class.java, "alumni.db"
    ).build()

    @Provides fun provideFeedDao(database: AlumniDatabase): FeedDao = database.feedDao()
    @Provides @Singleton fun providePostsApi(retrofit: Retrofit): PostsApi = retrofit.create(PostsApi::class.java)
    @Provides @Singleton fun provideCommentsApi(retrofit: Retrofit): CommentsApi = retrofit.create(CommentsApi::class.java)
    @Provides @Singleton fun provideLikesApi(retrofit: Retrofit): LikesApi = retrofit.create(LikesApi::class.java)
}
