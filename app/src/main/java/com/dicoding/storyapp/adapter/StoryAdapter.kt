package com.dicoding.storyapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.ui.story.DetailStoryActivity

class StoryAdapter :
    PagingDataAdapter<Story, StoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_story, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_image_item)
        private val description: TextView = itemView.findViewById(R.id.tv_name_item)
        private val name: TextView = itemView.findViewById(R.id.tv_title_item)

        init {
            itemView.setOnClickListener {
                val story = getItem(bindingAdapterPosition)
                val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                intent.putExtra("name", story?.name)
                intent.putExtra("description", story?.description)
                intent.putExtra("photoUrl", story?.photoUrl)
                itemView.context.startActivity(intent)
            }
        }

        fun bind(story: Story) {
            Log.d("hohoyx", story.toString())
            Glide.with(imageView.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_baseline_account_box)
                .error(R.drawable.ic_baseline_account_box)
                .into(imageView)

            description.text = story.description
            name.text = story.name
        }
    }


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}

