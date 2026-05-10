package com.example.escom_appcelular

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BecaDetalleActivity : AppCompatActivity() {

    data class BecaInfo(
        val requisitos: String,
        val fechas: String,
        val montos: String
    )

    private val becas = mapOf(
        "Institucional" to BecaInfo(
            requisitos = "• Promedio mínimo 8.0\n• No adeudar materias\n• Ser alumno regular\n• Carta de no beca vigente",
            fechas = "• Publicación de convocatoria: Febrero\n• Registro: Marzo\n• Resultados: Abril",
            montos = "• Hasta \$3,000 MXN mensuales"
        ),
        "BEIFI" to BecaInfo(
            requisitos = "• Promedio mínimo 8.5\n• Participar en proyecto de investigación\n• Carta del investigador responsable\n• No tener otra beca activa",
            fechas = "• Publicación de convocatoria: Enero / Agosto\n• Registro: Febrero / Septiembre\n• Resultados: Marzo / Octubre",
            montos = "• \$2,000 – \$4,000 MXN mensuales\n• Según nivel de participación"
        ),
        "Bécalos" to BecaInfo(
            requisitos = "• Promedio mínimo 8.0\n• Situación económica comprobable\n• Documentos socioeconómicos\n• Ser alumno de tiempo completo",
            fechas = "• Publicación de convocatoria: Marzo\n• Registro: Abril\n• Resultados: Mayo",
            montos = "• \$8,000 MXN por semestre\n• Apoyo único por ciclo escolar"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beca_detalle)

        val tipo = intent.getStringExtra("tipo") ?: return
        val info = becas[tipo] ?: return

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvTituloBeca).text = tipo
        findViewById<TextView>(R.id.tvRequisitos).text = info.requisitos
        findViewById<TextView>(R.id.tvFechas).text = info.fechas
        findViewById<TextView>(R.id.tvMontos).text = info.montos

        findViewById<Button>(R.id.btnInicio).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)); finish()
        }
        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            startActivity(Intent(this, Mapa::class.java)); finish()
        }
        findViewById<Button>(R.id.btnConsultar).setOnClickListener {
            startActivity(Intent(this, ConsultarActivity::class.java)); finish()
        }
    }
}