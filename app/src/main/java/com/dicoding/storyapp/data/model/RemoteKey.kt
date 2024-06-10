package com.dicoding.storyapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "remote_key")
data class RemoteKey(
    @PrimaryKey
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("prevKey")
    val prevKey: Int?,

    @field:SerializedName("nextKey")
    val nextKey: Int?
)