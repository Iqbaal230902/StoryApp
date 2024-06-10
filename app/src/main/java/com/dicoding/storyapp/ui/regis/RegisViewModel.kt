package com.dicoding.storyapp.ui.regis

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.data.response.DefaultResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisViewModel() : ViewModel() {
    fun registerUser(name:String, email: String, password: String): Boolean? {
        var result: Boolean? = false
        ApiConfig.apiInstance
            .registerUser(name, email, password)
            .enqueue(object : Callback<DefaultResponse> {
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    if (response.isSuccessful) {
                        result = response.body()?.error
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                }
            })

        return result
    }
}
