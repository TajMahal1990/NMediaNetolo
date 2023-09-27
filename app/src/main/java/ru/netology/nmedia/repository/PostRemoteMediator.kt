package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.errors.ApiException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                REFRESH -> {
//                    apiService.getLatest(state.config.pageSize)
                    postRemoteKeyDao.max()?.let {
                        apiService.getAfter(it, state.config.pageSize)
                    } ?: apiService.getLatest(state.config.initialLoadSize)
                }
                APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
                PREPEND -> {
//                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
//                    apiService.getAfter(id, state.config.pageSize)
                    return MediatorResult.Success(false)
                }
            }

            if (!result.isSuccessful) {
                throw ApiException(result.code(), result.message())
            }

            if (result.body().isNullOrEmpty())
                return MediatorResult.Success(true)

            val body = result.body() ?: throw ApiException(result.code(), result.message())

            appDb.withTransaction {

                when (loadType) {
                    REFRESH -> {
//                        postDao.clear()
//                        postRemoteKeyDao.insert(
//                            listOf(
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.AFTER,
//                                    body.first().id,
//                                ),
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.BEFORE,
//                                    body.last().id,
//                                ),
//                            )
//                        )
                        postRemoteKeyDao.insert(PostRemoteKeyEntity(
                            PostRemoteKeyEntity.KeyType.AFTER,
                            body.first().id))

                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.insert(PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            ))
                        }
                    }

                    APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id,
                            ),
                        )
                    }

                    PREPEND -> {
//                        postRemoteKeyDao.insert(
//                            PostRemoteKeyEntity(
//                                PostRemoteKeyEntity.KeyType.AFTER,
//                                body.first().id,
//                            ),
//                        )
                    }
                }

                postDao.insert(body.map(PostEntity.Companion::fromDto))
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}