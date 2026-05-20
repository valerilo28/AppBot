package com.example.escom_appcelular

import java.util.Calendar

/**
 * Centraliza todas las fechas clave para los recordatorios.
 * Cada ReminderEvent tiene un id único, título, descripción y fecha objetivo.
 */
data class ReminderEvent(
    val id: String,
    val category: ReminderCategory,
    val title: String,
    val description: String,
    val targetDate: Calendar,   // fecha del evento
    val offsetsDays: List<Int>  // días antes para notificar (0 = mismo día)
)

enum class ReminderCategory {
    BECA_GENERAL,
    SERVICIO_SOCIAL_INTERNO,
    SERVICIO_SOCIAL_EXTERNO,
    ESTANCIA_PROFESIONAL,
    AVISOS_IMPORTANTES
}

object ReminderData {

    /** Construye un Calendar para una fecha dada */
    private fun date(year: Int, month: Int, day: Int, hour: Int = 9, minute: Int = 0): Calendar =
        Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }

    // ─── OFFSETS por tipo ────────────────────────────────────────────────────
    private val BECA_OFFSETS = listOf(15, 7, 3, 1, 0)
    private val ESTANCIA_OFFSETS = listOf(30, 15, 7, 1)

    // ─── BECA GENERAL IPN (Nivel Superior) ──────────────────────────────────
    val becaGeneralEvents: List<ReminderEvent> = listOf(
        ReminderEvent(
            id = "beca_general_registro_1_inicio",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Inicio registro",
            description = "Inicia el 1er periodo de registro de solicitudes (06 mar). Entra a www.sibec.ipn.mx",
            targetDate = date(2026, 3, 6),
            offsetsDays = BECA_OFFSETS
        ),
        ReminderEvent(
            id = "beca_general_registro_1_fin",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Cierre registro (1er periodo)",
            description = "Último día del 1er periodo de registro (13 mar). Sube tus documentos en www.sibec.ipn.mx",
            targetDate = date(2026, 3, 13),
            offsetsDays = BECA_OFFSETS
        ),
        ReminderEvent(
            id = "beca_general_registro_2_inicio",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Inicio 2do registro",
            description = "Inicia el 2do periodo de registro (24 abr). Entra a www.sibec.ipn.mx",
            targetDate = date(2026, 4, 24),
            offsetsDays = BECA_OFFSETS
        ),
        ReminderEvent(
            id = "beca_general_registro_2_fin",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Cierre registro (2do periodo)",
            description = "Último día del 2do periodo de registro (29 abr). Sube tus documentos en www.sibec.ipn.mx",
            targetDate = date(2026, 4, 29),
            offsetsDays = BECA_OFFSETS
        ),
        ReminderEvent(
            id = "beca_general_resultados",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Publicación de resultados",
            description = "Hoy se publican los resultados de la Beca General IPN (08 may). Consulta en www.sibec.ipn.mx",
            targetDate = date(2026, 5, 8),
            offsetsDays = BECA_OFFSETS
        ),
        ReminderEvent(
            id = "beca_general_cuenta_bancaria_inicio",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Captura cuenta bancaria",
            description = "Inicia el periodo para capturar tu cuenta bancaria BBVA en SIBec (11 may). ¡No lo dejes para después!",
            targetDate = date(2026, 5, 11),
            offsetsDays = BECA_OFFSETS
        ),
        ReminderEvent(
            id = "beca_general_cuenta_bancaria_fin",
            category = ReminderCategory.BECA_GENERAL,
            title = "Beca General IPN — Último día cuenta bancaria",
            description = "Último día para capturar tu cuenta bancaria en SIBec (15 may). Si no lo haces se revocará la beca.",
            targetDate = date(2026, 5, 15),
            offsetsDays = BECA_OFFSETS
        )
    )

    // ─── ESTANCIA PROFESIONAL ────────────────────────────────────────────────
    val estanciaEvents: List<ReminderEvent> = listOf(
        ReminderEvent(
            id = "estancia_inicio",
            category = ReminderCategory.ESTANCIA_PROFESIONAL,
            title = "Estancia Profesional — Inicio recepción documentos",
            description = "Hoy inicia el periodo para entregar documentación de Estancia Profesional (10 feb).",
            targetDate = date(2026, 2, 10),
            offsetsDays = ESTANCIA_OFFSETS
        ),
        ReminderEvent(
            id = "estancia_fin",
            category = ReminderCategory.ESTANCIA_PROFESIONAL,
            title = "Estancia Profesional — Cierre recepción documentos",
            description = "Último día para entregar documentación de Estancia Profesional (22 may). ¡No lo dejes para después!",
            targetDate = date(2026, 5, 22),
            offsetsDays = ESTANCIA_OFFSETS
        )
    )

    // ─── SERVICIO SOCIAL INTERNO ─────────────────────────────────────────────
    // Cada entrada: (fechaInicio, fechaTermino, fechaLimiteExpediente)
    data class ServicioSocialPeriodo(
        val label: String,          // texto para mostrar en el spinner
        val fechaInicio: Calendar,
        val fechaTermino: Calendar,
        val fechaLimiteExpediente: Calendar
    )

    val servicioSocialInternosPeriodos: List<ServicioSocialPeriodo> = listOf(
        ServicioSocialPeriodo("01 oct 2025 – 30 abr 2026", date(2025,10,1),  date(2026,4,30),  date(2025,9,17)),
        ServicioSocialPeriodo("16 oct 2025 – 14 may 2026", date(2025,10,16), date(2026,5,14),  date(2025,10,1)),
        ServicioSocialPeriodo("03 nov 2025 – 03 jun 2026", date(2025,11,3),  date(2026,6,3),   date(2025,10,15)),
        ServicioSocialPeriodo("18 nov 2025 – 18 jun 2026", date(2025,11,18), date(2026,6,18),  date(2025,11,3)),
        ServicioSocialPeriodo("01 dic 2025 – 01 jul 2026", date(2025,12,1),  date(2026,7,1),   date(2025,11,18)),
        ServicioSocialPeriodo("16 dic 2025 – 16 jul 2026", date(2025,12,16), date(2026,7,16),  date(2025,12,1)),
        ServicioSocialPeriodo("05 ene 2026 – 17 jul 2026", date(2026,1,5),   date(2026,7,17),  date(2025,12,12)),
        ServicioSocialPeriodo("16 ene 2026 – 14 ago 2026", date(2026,1,16),  date(2026,8,14),  date(2025,1,6)),
        ServicioSocialPeriodo("03 feb 2026 – 03 sep 2026", date(2026,2,3),   date(2026,9,3),   date(2026,1,19)),
        ServicioSocialPeriodo("02 mar 2026 – 02 oct 2026", date(2026,3,2),   date(2026,10,2),  date(2026,2,16)),
        ServicioSocialPeriodo("17 mar 2026 – 16 oct 2026", date(2026,3,17),  date(2026,10,16), date(2026,3,6)),
        ServicioSocialPeriodo("16 abr 2026 – 16 nov 2026", date(2026,4,16),  date(2026,11,16), date(2025,3,24)),
        ServicioSocialPeriodo("04 may 2026 – 04 dic 2026", date(2026,5,4),   date(2026,12,4),  date(2026,4,24)),
        ServicioSocialPeriodo("18 may 2026 – 18 dic 2026", date(2026,5,18),  date(2026,12,18), date(2026,5,7)),
        ServicioSocialPeriodo("01 jun 2026 – 01 ene 2027", date(2026,6,1),   date(2027,1,1),   date(2026,5,18)),
        ServicioSocialPeriodo("16 jun 2026 – 15 ene 2027", date(2026,6,16),  date(2027,1,15),  date(2026,6,2)),
        ServicioSocialPeriodo("01 jul 2026 – 01 feb 2027", date(2026,7,1),   date(2027,2,1),   date(2026,6,23)),
        ServicioSocialPeriodo("16 jul 2026 – 16 feb 2027", date(2026,7,16),  date(2027,2,16),  date(2026,7,3)),
        ServicioSocialPeriodo("17 ago 2026 – 17 mar 2027", date(2026,8,17),  date(2027,3,17),  date(2026,7,15))
    )

    val servicioSocialExternosPeriodos: List<ServicioSocialPeriodo> = listOf(
        ServicioSocialPeriodo("01 oct 2025 – 01 abr 2026", date(2025,10,1), date(2026,4,1),   date(2025,9,17)),
        ServicioSocialPeriodo("16 oct 2025 – 16 abr 2026", date(2025,10,16), date(2026,4,16), date(2025,10,1)),
        ServicioSocialPeriodo("03 nov 2025 – 04 may 2026", date(2025,11,3), date(2026,5,4),   date(2025,10,15)),
        ServicioSocialPeriodo("18 nov 2025 – 18 may 2026", date(2025,11,18), date(2026,5,18), date(2025,11,3)),
        ServicioSocialPeriodo("01 dic 2025 – 01 jun 2026", date(2025,12,1), date(2026,6,1),   date(2025,11,18)),
        ServicioSocialPeriodo("16 dic 2025 – 16 jun 2026", date(2025,12,16), date(2026,6,16), date(2025,12,1)),
        ServicioSocialPeriodo("05 ene 2026 – 07 jul 2026", date(2026,1,5), date(2026,7,7),    date(2025,12,12)),
        ServicioSocialPeriodo("16 ene 2026 – 16 jul 2026", date(2026,1,16), date(2026,7,16),  date(2026,1,6)),
        ServicioSocialPeriodo("03 feb 2026 – 03 ago 2026", date(2026,2,3), date(2026,8,3),    date(2026,1,19)),
        ServicioSocialPeriodo("02 mar 2026 – 02 sep 2026", date(2026,3,2), date(2026,9,2),    date(2026,2,16)),
        ServicioSocialPeriodo("17 mar 2026 – 17 sep 2026", date(2026,3,17), date(2026,9,17),  date(2026,3,6)),
        ServicioSocialPeriodo("01 abr 2026 – 01 oct 2026", date(2026,4,1), date(2026,10,1),   date(2026,3,18)),
        ServicioSocialPeriodo("16 abr 2026 – 16 oct 2026", date(2026,4,16), date(2026,10,16), date(2026,3,24)),
        ServicioSocialPeriodo("04 may 2026 – 04 nov 2026", date(2026,5,4), date(2026,11,4),   date(2026,4,24)),
        ServicioSocialPeriodo("18 may 2026 – 18 nov 2026", date(2026,5,18), date(2026,11,18), date(2026,5,7)),
        ServicioSocialPeriodo("01 jun 2026 – 01 dic 2026", date(2026,6,1), date(2026,12,1),   date(2026,5,18)),
        ServicioSocialPeriodo("16 jun 2026 – 16 dic 2026", date(2026,6,16), date(2026,12,16), date(2026,6,2)),
        ServicioSocialPeriodo("01 jul 2026 – 04 ene 2027", date(2026,7,1), date(2027,1,4),    date(2026,6,23)),
        ServicioSocialPeriodo("16 jul 2026 – 18 ene 2027", date(2026,7,16), date(2027,1,18),  date(2026,7,3)),
        ServicioSocialPeriodo("03 ago 2026 – 03 feb 2027", date(2026,8,3), date(2027,2,3),    date(2026,7,10)),
        ServicioSocialPeriodo("17 ago 2026 – 17 feb 2027", date(2026,8,17), date(2027,2,17),  date(2026,7,15))
    )

    /** Genera el ReminderEvent de servicio social — solo fecha límite expediente */
    fun buildServicioSocialEvents(
        periodo: ServicioSocialPeriodo,
        isInterno: Boolean
    ): List<ReminderEvent> {
        val tipo   = if (isInterno) "Interno" else "Externo"
        val cat    = if (isInterno) ReminderCategory.SERVICIO_SOCIAL_INTERNO else ReminderCategory.SERVICIO_SOCIAL_EXTERNO
        val prefix = if (isInterno) "ss_int" else "ss_ext"
        val offsets = listOf(15, 7, 3, 1, 0)
        val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("es","MX"))

        return listOf(
            ReminderEvent(
                id = "${prefix}_expediente",
                category = cat,
                title = "Servicio Social $tipo — Fecha límite expediente",
                description = "Último día para entregar tu expediente antes de las 2pm: " +
                        "${fmt.format(periodo.fechaLimiteExpediente.time)}. " +
                        "Periodo: ${periodo.label}.",
                targetDate = periodo.fechaLimiteExpediente,
                offsetsDays = offsets
            )
        )
    }
}
