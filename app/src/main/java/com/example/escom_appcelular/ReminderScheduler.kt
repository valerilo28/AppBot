package com.example.escom_appcelular

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    /**
     * Programa todas las notificaciones para un ReminderEvent.
     * Por cada offset en offsetsDays programa un OneTimeWorkRequest.
     * También programa la notificación del mismo día a las 9am y a las 2 horas antes (hora del evento).
     */
    fun scheduleEvent(context: Context, event: ReminderEvent) {
        val wm = WorkManager.getInstance(context)
        val now = System.currentTimeMillis()

        event.offsetsDays.forEach { daysBefore ->
            val triggerCal = event.targetDate.clone() as Calendar
            if (daysBefore > 0) {
                triggerCal.add(Calendar.DAY_OF_YEAR, -daysBefore)
                triggerCal.set(Calendar.HOUR_OF_DAY, 9)
                triggerCal.set(Calendar.MINUTE, 0)
            }
            // daysBefore == 0 → mismo día a las 9am (ya está en targetDate con hora 9)

            val delay = triggerCal.timeInMillis - now
            if (delay <= 0) return@forEach // fecha ya pasó, no programar

            val label = when (daysBefore) {
                0    -> "hoy"
                1    -> "mañana"
                else -> "en $daysBefore días"
            }

            val workId = "${event.id}_${daysBefore}d"
            val data = workDataOf(
                "reminderId" to event.id,
                "notifId"    to workId.hashCode(),
                "title"      to event.title,
                "text"       to "📅 $label — ${event.description}",
                "isAviso"    to false
            )

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(event.id)
                .addTag(event.category.name)
                .build()

            wm.enqueueUniqueWork(workId, ExistingWorkPolicy.REPLACE, request)
        }

        // Notificación extra: 2 horas antes del evento (mismo día)
        val twoHoursBefore = (event.targetDate.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, -2)
        }
        val delay2h = twoHoursBefore.timeInMillis - now
        if (delay2h > 0) {
            val workId2h = "${event.id}_2h"
            val data2h = workDataOf(
                "reminderId" to event.id,
                "notifId"    to workId2h.hashCode(),
                "title"      to event.title,
                "text"       to "⏰ ¡En 2 horas! — ${event.description}",
                "isAviso"    to false
            )
            val req2h = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay2h, TimeUnit.MILLISECONDS)
                .setInputData(data2h)
                .addTag(event.id)
                .addTag(event.category.name)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(workId2h, ExistingWorkPolicy.REPLACE, req2h)
        }
    }

    /** Cancela todos los workers de un evento */
    fun cancelEvent(context: Context, eventId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(eventId)
    }

    /** Cancela todos los workers de una categoría */
    fun cancelCategory(context: Context, category: ReminderCategory) {
        WorkManager.getInstance(context).cancelAllWorkByTag(category.name)
    }

    /**
     * Programa un aviso periódico (Avisos Importantes).
     * intervalDays: cada cuántos días repetir.
     */
    fun schedulePeriodicAviso(context: Context, intervalDays: Int) {
        cancelPeriodicAviso(context)
        val wm = WorkManager.getInstance(context)
        val delayMs = TimeUnit.DAYS.toMillis(intervalDays.toLong())
        val data = workDataOf(
            "reminderId" to "aviso_importante",
            "notifId"    to "aviso_importante".hashCode(),
            "title"      to "📢 Avisos ESCOM",
            "text"       to "Hay novedades en ESCOM. Abre la app para ver las últimas publicaciones.",
            "isAviso"    to true
        )
        // Usamos PeriodicWorkRequest para repetición
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalDays.toLong(), TimeUnit.DAYS
        )
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("aviso_importante")
            .build()

        wm.enqueueUniquePeriodicWork(
            "aviso_importante",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelPeriodicAviso(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("aviso_importante")
    }

    /**
     * Reprograma una notificación pospuesta (snooze).
     */
    fun snoozeReminder(
        context: Context,
        reminderId: String,
        notifId: Int,
        title: String,
        text: String,
        snoozeOption: String
    ) {
        val now = Calendar.getInstance()
        val triggerCal = now.clone() as Calendar

        when (snoozeOption) {
            NotificationHelper.SNOOZE_1H -> triggerCal.add(Calendar.HOUR_OF_DAY, 1)
            NotificationHelper.SNOOZE_TARDE -> {
                triggerCal.set(Calendar.HOUR_OF_DAY, 18)
                triggerCal.set(Calendar.MINUTE, 0)
                if (triggerCal.before(now)) triggerCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            NotificationHelper.SNOOZE_MANANA -> {
                triggerCal.add(Calendar.DAY_OF_YEAR, 1)
                triggerCal.set(Calendar.HOUR_OF_DAY, 9)
                triggerCal.set(Calendar.MINUTE, 0)
            }
            NotificationHelper.SNOOZE_NEXT -> {
                // Próxima fecha preestablecida: en 3 días a las 9am
                triggerCal.add(Calendar.DAY_OF_YEAR, 3)
                triggerCal.set(Calendar.HOUR_OF_DAY, 9)
                triggerCal.set(Calendar.MINUTE, 0)
            }
        }

        val delay = triggerCal.timeInMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val workId = "${reminderId}_snooze_${System.currentTimeMillis()}"
        val data = workDataOf(
            "reminderId" to reminderId,
            "notifId"    to notifId,
            "title"      to title,
            "text"       to text,
            "isAviso"    to false
        )
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(reminderId)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(workId, ExistingWorkPolicy.REPLACE, request)
    }
}
