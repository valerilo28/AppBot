package com.example.escom_appcelular

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfesoresListaActivity : AppCompatActivity() {

    private lateinit var adapter: ProfesorAdapter
    private lateinit var tvResultados: TextView
    private lateinit var btnLimpiar: ImageButton
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profesores_lista)

        val carrera = intent.getStringExtra("carrera") ?: "Profesores"
        val titulo  = intent.getStringExtra("titulo") ?: carrera

        findViewById<TextView>(R.id.tvTituloCarrera).text = titulo
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        tvResultados = findViewById(R.id.tvResultados)
        btnLimpiar   = findViewById(R.id.btnLimpiar)

        // ── Construir secciones según carrera ─────────────────────
        val todos = ProfesorRepository.getAll(this)
        val sections = buildSections(carrera, todos)

        // ── RecyclerView ──────────────────────────────────────────
        adapter = ProfesorAdapter { profesor ->
            startActivity(
                Intent(this, ProfesorDetalleActivity::class.java)
                    .putExtra("nombre", profesor.nombre)
            )
        }
        adapter.setSections(sections)

        val recycler = findViewById<RecyclerView>(R.id.recyclerProfesores)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // ── Buscador ──────────────────────────────────────────────
        val etBuscar = findViewById<EditText>(R.id.etBuscar)

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString() ?: ""
                adapter.filter(currentQuery)
                btnLimpiar.visibility = if (currentQuery.isNotEmpty()) View.VISIBLE else View.GONE
                actualizarContador(currentQuery)
            }
        })

        btnLimpiar.setOnClickListener {
            etBuscar.text.clear()
            etBuscar.clearFocus()
        }
    }

    // ── Construir secciones por semestre ──────────────────────────

    private fun buildSections(
        carrera: String,
        todos: List<Profesor>
    ): List<Pair<String, List<Profesor>>> {
        return when (carrera) {
            "Ingeniería en Sistemas Computacionales" -> buildSectionsByCurriculum(todos, CurriculumData.ISC, CurriculumData.ISC_OPTATIVAS)
            "Ingeniería en Inteligencia Artificial"  -> buildSectionsByCurriculum(todos, CurriculumData.IIA, CurriculumData.IIA_OPTATIVAS)
            "Ciencia de Datos"                       -> buildSectionsByCurriculum(todos, CurriculumData.LCD, CurriculumData.LCD_OPTATIVAS)
            else -> listOf("Todos los profesores" to todos.sortedBy { it.nombre })
        }
    }

    private fun buildSectionsByCurriculum(
        todos: List<Profesor>,
        semestres: List<CurriculumData.Semestre>,
        optativas: List<String>
    ): List<Pair<String, List<Profesor>>> {
        val sections = mutableListOf<Pair<String, List<Profesor>>>()

        semestres.forEach { semestre ->
            val materiasNorm = semestre.materias.map { it.uppercase() }.toSet()
            val profs = todos.filter { p ->
                p.materias.any { m -> materiasNorm.contains(m.uppercase()) }
            }.sortedBy { it.nombre }
            if (profs.isNotEmpty()) sections.add("Semestre ${semestre.numero}" to profs)
        }

        val optNorm = optativas.map { it.uppercase() }.toSet()
        val profsOpt = todos.filter { p ->
            p.materias.any { m -> optNorm.contains(m.uppercase()) }
        }.sortedBy { it.nombre }
        if (profsOpt.isNotEmpty()) sections.add("Optativas" to profsOpt)

        return sections
    }

    private fun actualizarContador(query: String) {
        if (query.isEmpty()) {
            tvResultados.visibility = View.GONE
        } else {
            val count = adapter.getResultCount()
            tvResultados.text = if (count == 0)
                "Sin resultados para \"$query\""
            else
                "$count profesor${if (count != 1) "es" else ""} para \"$query\""
            tvResultados.visibility = View.VISIBLE
        }
    }
}
