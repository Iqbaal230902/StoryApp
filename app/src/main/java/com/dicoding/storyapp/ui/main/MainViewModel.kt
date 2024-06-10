package com.dicoding.storyapp.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.data.model.User
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.di.Injection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (private val storyRepository: StoryRepository, private val userRepository: UserRepository) : ViewModel() {
    var scrollPosition = 0
    fun story(token: String):LiveData<PagingData<Story>>{
        return storyRepository.getStory(token).cachedIn(viewModelScope)
    }

    fun getSession(): Flow<User> {
        return userRepository.getSession()
    }


    val logout = {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(Injection.provideStoryRepository(context), Injection.provideRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}