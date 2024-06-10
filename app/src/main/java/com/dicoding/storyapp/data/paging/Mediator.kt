package com.dicoding.storyapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.storyapp.api.ApiService
import com.dicoding.storyapp.data.model.RemoteKey
import com.dicoding.storyapp.data.model.Story
import com.dicoding.storyapp.database.StoryDatabase
import com.dicoding.storyapp.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

@OptIn(ExperimentalPagingApi::class)
class Mediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
) : RemoteMediator<Int, Story>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Story>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

            }
        }

        return wrapEspressoIdlingResource {
            try {
                val response = apiService.getAllStories(token, page, state.config.pageSize, 0).awaitResponse()
                if (response.isSuccessful) {
                    val stories = response.body()?.listStory.orEmpty()
                    val endOfPaginationReached = stories.isEmpty()

                    withContext(Dispatchers.IO) {
                        try {
                            database.withTransaction {
                                if (loadType == LoadType.REFRESH) {
                                    database.remoteKeyDao().deleteRemoteKey()
                                    database.storyDao().deleteAll()
                                }

                                val keys = stories.map {
                                    RemoteKey(id = it.id, prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1, nextKey = if (endOfPaginationReached) null else page + 1)
                                }
                                database.remoteKeyDao().insertAll(keys)
                                response.body()?.listStory?.forEach {
                                    val sto = Story(it.id, it.name, it.description, it.photoUrl, it.createdAt, it.lat, it.lon)
                                    database.storyDao().insertStory(sto)
                                }
                            }
                        } catch (e: Exception) {

                        }

                    }

                    return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                } else {
                    return MediatorResult.Error(Exception("Error ${response.code()} ${response.message()}"))
                }
            } catch (exception: Exception) {
                return MediatorResult.Error(exception)
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Story>): RemoteKey? {
        return withContext(Dispatchers.IO) {
            state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { story ->
                database.remoteKeyDao().getRemoteKeyById(story.id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Story>): RemoteKey? {
        return withContext(Dispatchers.IO) {
            state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { story ->
                database.remoteKeyDao().getRemoteKeyById(story.id)
            }
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Story>): RemoteKey? {
        return withContext(Dispatchers.IO) {
            state.anchorPosition?.let { position ->
                state.closestItemToPosition(position)?.id?.let { id ->
                    database.remoteKeyDao().getRemoteKeyById(id)
                }
            }
        }
    }
}

