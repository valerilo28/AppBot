package com.example.escom_appcelular

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object NotificationHelper {

    const val CHANNEL_RECORDATORIOS = "channel_recordatorios"
    const val CHANNEL_AVISOS = "channel_avisos"

    const val ACTION_COMPLETADO = "com.example.escom_appcelular.ACTION_COMPLETADO"
    const val ACTION_RECORDAR_MAS_TARDE = "com.example.escom_appcelular.ACTION_RECORDAR_MAS_TARDE"

    const val EXTRA_NOTIF_ID = "extra_notif_id"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_SNOOZE_OPTION = "extra_snooze_option"

    const val SNOOZE_1H = "snooze_1h"
    const val SNOOZE_TARDE = "snooze_tarde"
    const val SNOOZE_MANANA = "snooze_manana"
    const val SNOOZE_NEXT = "snooze_next"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            CHANNEL_RECORDATORIOS,
            "Recordatorios",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recordatorios de becas, servicio social y estancia profesional"
            nm.createNotificationChannel(this)
        }

        NotificationChannel(
            CHANNEL_AVISOS,
            "Avisos Importantes",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos periódicos para revisar novedades de ESCOM"
            nm.createNotificationChannel(this)
        }
    }

    /**
     * Muestra una notificación con acciones "Completado" y "Recordarme más tarde".
     */
    fun showReminderNotification(
        context: Context,
        notifId: Int,
        reminderId: String,
        title: String,
        text: String
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la app al tocar la notificación
        val openIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción: Completado
        val completadoIntent = PendingIntent.getBroadcast(
            context, notifId * 10 + 1,
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_COMPLETADO
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_REMINDER_ID, reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción: Recordarme más tarde — abre diálogo de opciones via Activity
        val snoozeIntent = PendingIntent.getActivity(
            context, notifId * 10 + 2,
            Intent(context, SnoozeDialogActivity::class.java).apply {
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_REMINDER_ID, reminderId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_star_circle)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openIntent)
            .setAutoCancel(false)
            .addAction(0, "✓ Completado", completadoIntent)
            .addAction(0, "⏰ Recordarme más tarde", snoozeIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(notifId, notification)
    }

    /**
     * Notificación simple para Avisos Importantes (sin acciones).
     */
    fun showAvisoNotification(
        context: Context,
        notifId: Int,
        title: String,
        text: String
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = PendingIntent.getActivity(
            context, notifId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_AVISOS)
            .setSmallIcon(R.drawable.ic_star_circle)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        nm.notify(notifId, notification)
    }

    fun cancelNotification(context: Context, notifId: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notifId)
    }
}
