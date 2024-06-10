package com.dicoding.storyapp.di

import android.content.Context
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.database.StoryDatabase
import com.dicoding.storyapp.database.room.RemoteKeysDao
import com.dicoding.storyapp.database.room.StoryDao
import dagger.Provides

object Injection {
    fun provideRepository(context: Context) : UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref)
    }

    fun provideStoryRepository(context: Context) : StoryRepository {
        val apiInstance = ApiConfig.apiInstance
        val storyDatabase = StoryDatabase.getDatabase(context)
        return StoryRepository.getInstance(storyDatabase, apiInstance)
    }

    @Provides
    fun provideStoryDao(storyDatabase: StoryDatabase): StoryDao = storyDatabase.storyDao()

    @Provides
    fun provideRemoteKeysDao(storyDatabase: StoryDatabase): RemoteKeysDao = storyDatabase.remoteKeyDao()
}