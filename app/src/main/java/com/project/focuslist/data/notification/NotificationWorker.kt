package com.project.focuslist.data.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context, params: WorkerParameters
): Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "ToDo Reminder"
        val message = inputData.getString("message") ?: "You have a task to do!"

        NotificationUtils.createNotificationChannel(applicationContext)
        NotificationUtils.showNotification(applicationContext, title, message)

        return Result.success()
    }
}