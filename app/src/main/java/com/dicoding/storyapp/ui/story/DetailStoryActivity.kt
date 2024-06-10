package com.dicoding.storyapp.ui.story

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val description = intent.getStringExtra("description").toString()
        val photoUrl = intent.getStringExtra("photoUrl")

        binding.tvDetailName.text = name
        binding.tvDetailDescription.text = description

        if (photoUrl != "null") {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_baseline_account_box)
                .error(R.drawable.ic_baseline_account_box)
                .into(binding.ivDetailPhoto)
        } else {
            binding.ivDetailPhoto.setImageResource(R.drawable.ic_baseline_account_box)
        }
    }
}
