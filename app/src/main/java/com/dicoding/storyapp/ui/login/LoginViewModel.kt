package com.dicoding.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    fun loginUser(email: String, password: String): User? {
        var result: User? = null

        runBlocking(Dispatchers.IO) {
            val call = ApiConfig.apiInstance.loginUser(email, password)
            val response = call.execute()
            if (response.isSuccessful) {
                result = response.body()?.loginResult
            }
        }

        return result
    }

    fun saveSession(user: User) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): LiveData<User> {
        return repository.getSession().asLiveData()
    }
}