package com.dicoding.storyapp

import com.dicoding.storyapp.data.model.Story
import kotlin.random.Random

object DataDummy {
    fun generateDummyStoriesResponse(): List<Story> {
        val listStory = mutableListOf<Story>()

        for (i in 0 until 5) {
            val mockStory = Story(
                id = "story-${generateRandomString(16)}",
                photoUrl = "https://example.com/image$i.jpg",
                createdAt = "2022-01-08T06:34:18.598Z",
                name = "User $i",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                lat = generateRandomDouble(-90.0, 90.0),
                lon = generateRandomDouble(-180.0, 180.0)
            )

            listStory.add(mockStory)
        }
        return listStory
    }

    fun token(): String
    {
        return generateRandomString(20)
    }

    private fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun generateRandomDouble(min: Double, max: Double): Double {
        return min + (max - min) * Random.nextDouble()
    }
}
