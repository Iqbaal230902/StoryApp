package com.dicoding.storyapp.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.CoroutineTestRule
import com.dicoding.storyapp.DataDummy
import com.dicoding.storyapp.PagingTest
import com.dicoding.storyapp.adapter.StoryAdapter
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.data.repository.StoryRepository
import com.dicoding.storyapp.data.repository.UserRepository
import com.dicoding.storyapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var mainViewModel: MainViewModel

    private lateinit var dummyToken: String
    private lateinit var dummyStories: List<Story>
    private lateinit var data: PagingData<Story>
    private lateinit var expectedStory: MutableLiveData<PagingData<Story>>
    private lateinit var differ: AsyncPagingDataDiffer<Story>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mainViewModel = MainViewModel(storyRepository, userRepository)
        dummyToken = DataDummy.token()
        dummyStories = DataDummy.generateDummyStoriesResponse()
        data = PagingTest.snapshot(dummyStories)
        expectedStory = MutableLiveData<PagingData<Story>>().apply {
            value = data
        }
    }

    @Test
    fun `Get stories successfully`() = runTest {
        `when`(storyRepository.getStory(dummyToken)).thenReturn(expectedStory)

        val actualStories = mainViewModel.story(dummyToken).getOrAwaitValue()
        setupDiffer()

        differ.submitData(actualStories)

        advanceUntilIdle()

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
    }

    @Test
    fun `When Get Story Should Not Null and Return Data`() = runTest {
        `when`(storyRepository.getStory(dummyToken)).thenReturn(expectedStory)

        val actualStories = mainViewModel.story(dummyToken).getOrAwaitValue()
        setupDiffer()

        differ.submitData(actualStories)

        advanceUntilIdle()

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `When Get Story Empty Should Return No Data`() = runTest {
        data = PagingData.from(emptyList())
        expectedStory.value = data

        `when`(storyRepository.getStory(dummyToken)).thenReturn(expectedStory)

        val actualStories = mainViewModel.story(dummyToken).getOrAwaitValue()
        setupDiffer()

        differ.submitData(actualStories)

        advanceUntilIdle()

        assertEquals(0, differ.snapshot().size)
    }

    private fun setupDiffer() {
        differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = updateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher
        )
    }

    private val updateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
}
