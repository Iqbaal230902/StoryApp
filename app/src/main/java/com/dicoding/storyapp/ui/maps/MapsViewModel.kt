package com.dicoding.storyapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.data.model.User
import com.dicoding.storyapp.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class MapsViewModel @Inject constructor (private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<User> {
        return repository.getSession().asLiveData()
    }


    fun getAllStories(token: String): List<Story> {
        return runBlocking(Dispatchers.IO) {
            val response = ApiConfig.apiInstance.getAllStories(token, null, null).execute()
            if (response.isSuccessful) {
                response.body()?.listStory ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
}


