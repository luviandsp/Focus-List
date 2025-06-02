package com.project.focuslist.data.model

data class UserCacheWithTimestamp(
    val userCache: UserCache?,
    val updatedAt: Long?
)

data class UserCache(
    var userId: String? = null,
    var username: String? = null,
    var userProfileImage: String? = null,
)
