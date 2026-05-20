package com.example.escom_appcelular

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AreasListaActivity : AppCompatActivity() {

    private lateinit var adapter: AreaAdapter
    private lateinit var tvResultados: TextView
    private lateinit var btnLimpiar: ImageButton
    private val totalAreas get() = AreaRepository.getAll(this).size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_areas_lista)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val areas = AreaRepository.getAll(this)
        tvResultados = findViewById(R.id.tvResultados)
        btnLimpiar   = findViewById(R.id.btnLimpiar)

        val recycler = findViewById<RecyclerView>(R.id.recyclerAreas)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter = AreaAdapter(areas) { area ->
            val intent = Intent(this, AreaDetalleActivity::class.java)
            intent.putExtra("area", area.area)
            startActivity(intent)
        }
        recycler.adapter = adapter

        // ── Búsqueda ──────────────────────────────────────────────
        val etBuscar = findViewById<EditText>(R.id.etBuscar)

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                adapter.filter.filter(query)
                btnLimpiar.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                actualizarContador(query)
            }
        })

        btnLimpiar.setOnClickListener {
            etBuscar.text.clear()
            etBuscar.clearFocus()
        }

        // ── Nav inferior ──────────────────────────────────────────
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

    private fun actualizarContador(query: String) {
        if (query.isEmpty()) {
            tvResultados.visibility = View.GONE
        } else {
            val count = adapter.itemCount
            tvResultados.text = if (count == 0)
                "Sin resultados para \"$query\""
            else
                "$count resultado${if (count != 1) "s" else ""} para \"$query\""
            tvResultados.visibility = View.VISIBLE
        }
    }
}
