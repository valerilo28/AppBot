package com.example.escom_appcelular

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfesoresListaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profesores_lista)

        val titulo = intent.getStringExtra("titulo") ?: "Profesores"
        findViewById<TextView>(R.id.tvTituloCarrera).text = titulo
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Cargar todos los profesores del JSON
        val todos = ProfesorRepository.getAll(this)

        val recycler = findViewById<RecyclerView>(R.id.recyclerProfesores)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        recycler.adapter = ProfesorAdapter(todos) { profesor ->
            val intent = Intent(this, ProfesorDetalleActivity::class.java)
            intent.putExtra("nombre", profesor.nombre)
            startActivity(intent)
        }
    }
}