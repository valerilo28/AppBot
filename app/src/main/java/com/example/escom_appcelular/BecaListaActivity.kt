package com.example.escom_appcelular

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class BecasListaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_becas_lista)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        fun abrirBeca(tipo: String) {
            startActivity(Intent(this, BecaDetalleActivity::class.java)
                .putExtra("tipo", tipo))
        }

        findViewById<LinearLayout>(R.id.btnInstitucional)
            .setOnClickListener { abrirBeca("Institucional") }
        findViewById<LinearLayout>(R.id.btnBEIFI)
            .setOnClickListener { abrirBeca("BEIFI") }
        findViewById<LinearLayout>(R.id.btnBecalos)
            .setOnClickListener { abrirBeca("Bécalos") }

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