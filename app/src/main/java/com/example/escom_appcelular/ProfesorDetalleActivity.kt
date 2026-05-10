package com.example.escom_appcelular

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ProfesorDetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profesor_detalle)

        val nombre = intent.getStringExtra("nombre") ?: return
        val profesor = ProfesorRepository.getAll(this)
            .find { it.nombre == nombre } ?: return

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvHeaderNombre).text = nombre
            .split(" ").take(2).joinToString(" ") // Apellidos
        findViewById<TextView>(R.id.tvNombre).text = nombre

        // Materias
        val tvMaterias = findViewById<TextView>(R.id.tvMaterias)
        tvMaterias.text = profesor.materias.joinToString("\n") { "• $it" }

        // Horario — construir tabla dinámica
        val layoutHorario = findViewById<LinearLayout>(R.id.layoutHorario)
        layoutHorario.removeAllViews()

        val diasOrden = listOf("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado")
        val porDia = profesor.horario.groupBy { it.dia }

        for (dia in diasOrden) {
            val entradas = porDia[dia] ?: continue

            // Título del día
            val tvDia = TextView(this).apply {
                text = dia
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(0, 16, 0, 4)
            }
            layoutHorario.addView(tvDia)

            for (h in entradas) {
                val tvEntrada = TextView(this).apply {
                    text = "  ${h.entrada}–${h.salida}  •  ${h.materia}  [${h.salon}]"
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    setPadding(8, 2, 0, 2)
                }
                layoutHorario.addView(tvEntrada)
            }
        }
    }
}