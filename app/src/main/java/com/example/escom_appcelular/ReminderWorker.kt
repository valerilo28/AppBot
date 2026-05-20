package com.example.escom_appcelular

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker que dispara una notificación de recordatorio.
 * Recibe por Data: reminderId, notifId, title, text, isAviso
 */
class ReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val reminderId = inputData.getString("reminderId") ?: return Result.failure()
        val notifId    = inputData.getInt("notifId", reminderId.hashCode())
        val title      = inputData.getString("title") ?: "Recordatorio ESCOM"
        val text       = inputData.getString("text") ?: ""
        val isAviso    = inputData.getBoolean("isAviso", false)

        NotificationHelper.createChannels(applicationContext)

        if (isAviso) {
            NotificationHelper.showAvisoNotification(applicationContext, notifId, title, text)
        } else {
            // Solo mostrar si no fue marcado como completado
            val prefs = applicationContext.getSharedPreferences("reminders_done", Context.MODE_PRIVATE)
            if (!prefs.getBoolean(reminderId, false)) {
                NotificationHelper.showReminderNotification(
                    applicationContext, notifId, reminderId, title, text
                )
            }
        }

        return Result.success()
    }
}
