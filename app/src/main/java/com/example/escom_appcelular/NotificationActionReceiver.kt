package com.example.escom_appcelular

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recibe las acciones de las notificaciones: Completado.
 * "Recordarme más tarde" se maneja via SnoozeDialogActivity.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notifId    = intent.getIntExtra(NotificationHelper.EXTRA_NOTIF_ID, -1)
        val reminderId = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return

        when (intent.action) {
            NotificationHelper.ACTION_COMPLETADO -> {
                // Marcar como completado en SharedPreferences
                context.getSharedPreferences("reminders_done", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(reminderId, true)
                    .apply()

                // Cancelar todos los workers pendientes de este recordatorio
                ReminderScheduler.cancelEvent(context, reminderId)

                // Cancelar la notificación visible
                if (notifId != -1) NotificationHelper.cancelNotification(context, notifId)
            }
        }
    }
}
