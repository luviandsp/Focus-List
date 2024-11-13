package com.project.focuslist.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.project.focuslist.data.model.Task
import com.project.focuslist.data.room.TaskRepository

//class FocusListPagingSource(private val taskRepo: TaskRepository) : PagingSource<Int, Task>() {
//
//    companion object {
//        private const val INITIAL_PAGE_INDEX = 1
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Task>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
//    }
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Task> {
//        val page = params.key ?: INITIAL_PAGE_INDEX
//        val pageSize = params.loadSize
//
//        return try {
//            val tasks = taskRepo.getTaskList(page, pageSize)
//
//            LoadResult.Page(
//                data = tasks,
//                prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
//                nextKey = if (tasks.isEmpty()) null else page + 1
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//}
