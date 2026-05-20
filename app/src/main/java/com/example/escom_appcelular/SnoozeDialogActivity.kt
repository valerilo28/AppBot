package com.example.escom_appcelular

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity transparente que muestra el diálogo "Recordarme más tarde".
 * Se lanza desde la acción de la notificación.
 */
class SnoozeDialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notifId    = intent.getIntExtra(NotificationHelper.EXTRA_NOTIF_ID, -1)
        val reminderId = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: run { finish(); return }

        // Calcular las horas/fechas para mostrar en el diálogo
        val cal1h = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
        val calTarde = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        val calManana = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0)
        }
        val calNext = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0)
        }

        val fmt = SimpleDateFormat("EEE d MMM, HH:mm", Locale("es", "MX"))

        val options = arrayOf(
            "En 1 hora  (${fmt.format(cal1h.time)})",
            "Esta tarde  (${fmt.format(calTarde.time)})",
            "Mañana  (${fmt.format(calManana.time)})",
            "Próxima fecha programada  (${fmt.format(calNext.time)})"
        )
        val snoozeKeys = arrayOf(
            NotificationHelper.SNOOZE_1H,
            NotificationHelper.SNOOZE_TARDE,
            NotificationHelper.SNOOZE_MANANA,
            NotificationHelper.SNOOZE_NEXT
        )

        AlertDialog.Builder(this)
            .setTitle("¿Cuándo te recuerdo?")
            .setItems(options) { _, which ->
                val snoozeOption = snoozeKeys[which]
                val title = "Recordatorio ESCOM"
                val text  = "Tienes un recordatorio pendiente."

                // Cancelar notificación actual
                if (notifId != -1) NotificationHelper.cancelNotification(this, notifId)

                // Programar snooze
                ReminderScheduler.snoozeReminder(
                    this, reminderId, notifId, title, text, snoozeOption
                )
                finish()
            }
            .setNegativeButton("Cancelar") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }
}
