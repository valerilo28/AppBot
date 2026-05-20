package com.example.escom_appcelular

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AreaDetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_detalle)

        val nombreArea = intent.getStringExtra("area") ?: return
        val area = AreaRepository.getAll(this).find { it.area == nombreArea } ?: return

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Foto
        val ivFoto = findViewById<ImageView>(R.id.ivAreaDetalleFoto)
        val resId = resources.getIdentifier(area.foto, "drawable", packageName)
        if (resId != 0) ivFoto.setImageResource(resId)
        else ivFoto.setImageResource(R.drawable.ic_avatar)

        // Datos
        findViewById<TextView>(R.id.tvAreaDetalleTitulo).text = area.area
        findViewById<TextView>(R.id.tvAreaDetalleResponsable).text = area.responsable
        findViewById<TextView>(R.id.tvAreaDetalleCargo).text = area.cargo
        findViewById<TextView>(R.id.tvAreaDetalleCorreo).text = area.correo
        findViewById<TextView>(R.id.tvAreaDetalleExtension).text = "Ext. ${area.extension}"
        findViewById<TextView>(R.id.tvAreaDetalleUbicacion).text = area.ubicacion

        // Tap en correo abre cliente de email
        findViewById<TextView>(R.id.tvAreaDetalleCorreo).setOnClickListener {
            if (area.correo != "-----") {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${area.correo}")))
            }
        }

        // Bottom nav
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
