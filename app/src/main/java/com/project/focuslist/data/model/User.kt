package com.project.focuslist.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class User(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("username") val username: String = "",
    @PropertyName("profileImageUrl") val profileImageUrl: String? = null,
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("updatedAt") val updatedAt: Timestamp = Timestamp.now()
)
