package com.dicoding.storyapp.data.response

import com.dicoding.storyapp.data.model.User

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val loginResult: User
)