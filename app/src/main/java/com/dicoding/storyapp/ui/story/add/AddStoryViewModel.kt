package com.dicoding.storyapp.ui.story.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.data.model.User
import com.dicoding.storyapp.data.response.DefaultResponse
import com.dicoding.storyapp.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val repository: UserRepository) : ViewModel() {
    fun addNewStory(token: String, photo: MultipartBody.Part, description: RequestBody, location: Location?): DefaultResponse? {
        var result: DefaultResponse? = null

        runBlocking(Dispatchers.IO) {
            val call = ApiConfig.apiInstance.addNewStory("Bearer $token", description, photo, location?.latitude, location?.longitude)
            val response = call.execute()
            if (response.isSuccessful) {
                result = response.body()
            }
        }

        return result
    }

    fun getSession(): LiveData<User> {
        return repository.getSession().asLiveData()
    }
}