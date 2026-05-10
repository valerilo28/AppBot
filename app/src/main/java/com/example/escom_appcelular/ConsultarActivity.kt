package com.example.escom_appcelular

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ConsultarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultar)

        findViewById<ImageView>(R.id.logo_escom)
            .setImageResource(R.drawable.logo_escom)

        // Bottom nav
        findViewById<Button>(R.id.btnInicio).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)); finish()
        }
        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            startActivity(Intent(this, Mapa::class.java)); finish()
        }

        // Dirección General → lista de áreas
        findViewById<LinearLayout>(R.id.btnDireccionGeneral).setOnClickListener {
            startActivity(Intent(this, AreasListaActivity::class.java))
        }

        // Profesores → navegar a lista
        fun abrirProfesores(carrera: String) {
            val intent = Intent(this, ProfesoresListaActivity::class.java)
            intent.putExtra("carrera", carrera)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.btnCienciasBasicas).setOnClickListener {
            abrirProfesores("Ciencias Básicas")
        }
        findViewById<LinearLayout>(R.id.btnISC).setOnClickListener {
            abrirProfesores("Ingeniería en Sistemas Computacionales")
        }
        findViewById<LinearLayout>(R.id.btnIIA).setOnClickListener {
            abrirProfesores("Ingeniería en Inteligencia Artificial")
        }
        findViewById<LinearLayout>(R.id.btnCD).setOnClickListener {
            abrirProfesores("Ciencia de Datos")
        }

        // Trámites → abrir URL
        fun abrirUrl(url: String) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // En ConsultarActivity, cambia el click de tvBecas:
        findViewById<LinearLayout>(R.id.tvBecas).setOnClickListener {
            startActivity(Intent(this, BecasListaActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.tvServicioSocial).setOnClickListener {
            abrirUrl("https://www.escom.ipn.mx/SSEIS/apoyoseducativos/servicios/servicioSocial.php")
        }
        findViewById<LinearLayout>(R.id.tvEstanciaProfesional).setOnClickListener {
            abrirUrl("https://www.escom.ipn.mx/SSEIS/vinculacion/servicios/estancia.php")
        }
    }
}