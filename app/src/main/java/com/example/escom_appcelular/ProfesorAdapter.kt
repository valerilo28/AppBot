package com.example.escom_appcelular

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter con dos tipos de item: encabezado de sección y fila de profesor.
 * Soporta filtrado por nombre y materia.
 */
class ProfesorAdapter(
    private val onClick: (Profesor) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER  = 0
        const val TYPE_PROFESOR = 1
    }

    /** Item sellado: puede ser encabezado o profesor */
    sealed class ListItem {
        data class Header(val titulo: String, val conteo: Int) : ListItem()
        data class ProfesorItem(val profesor: Profesor, val materiaDestacada: String = "") : ListItem()
    }

    private var displayItems: List<ListItem> = emptyList()

    // ── Fuente de datos completa (secciones sin filtrar) ──────────
    private var allSections: List<Pair<String, List<Profesor>>> = emptyList()

    fun setSections(sections: List<Pair<String, List<Profesor>>>) {
        allSections = sections
        applyFilter("")
    }

    fun filter(query: String) {
        applyFilter(query.trim())
    }

    private fun applyFilter(query: String) {
        val q = query.lowercase()
        val items = mutableListOf<ListItem>()

        allSections.forEach { (titulo, profesores) ->
            val filtrados = if (q.isEmpty()) {
                profesores
            } else {
                profesores.filter { p ->
                    p.nombre.lowercase().contains(q) ||
                    p.materias.any { it.lowercase().contains(q) }
                }
            }
            if (filtrados.isNotEmpty()) {
                items.add(ListItem.Header(titulo, filtrados.size))
                filtrados.forEach { p ->
                    // Materia que coincide con la búsqueda (para resaltar)
                    val match = if (q.isNotEmpty())
                        p.materias.firstOrNull { it.lowercase().contains(q) } ?: ""
                    else ""
                    items.add(ListItem.ProfesorItem(p, match))
                }
            }
        }

        displayItems = items
        notifyDataSetChanged()
    }

    fun getResultCount(): Int = displayItems.filterIsInstance<ListItem.ProfesorItem>().size

    // ── RecyclerView ──────────────────────────────────────────────

    override fun getItemViewType(position: Int) =
        if (displayItems[position] is ListItem.Header) TYPE_HEADER else TYPE_PROFESOR

    override fun getItemCount() = displayItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(inflater.inflate(R.layout.item_seccion_semestre, parent, false))
        } else {
            ProfesorViewHolder(inflater.inflate(R.layout.item_profesor, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.ProfesorItem -> (holder as ProfesorViewHolder).bind(item)
        }
    }

    // ── ViewHolders ───────────────────────────────────────────────

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo  = itemView.findViewById<TextView>(R.id.tvSeccionTitulo)
        private val tvConteo  = itemView.findViewById<TextView>(R.id.tvSeccionConteo)
        fun bind(item: ListItem.Header) {
            tvTitulo.text = item.titulo
            tvConteo.text = "${item.conteo} prof${if (item.conteo != 1) "s" else "."}"
        }
    }

    inner class ProfesorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre  = itemView.findViewById<TextView>(R.id.tvNombreProfesor)
        private val tvMateria = itemView.findViewById<TextView>(R.id.tvMateriaProfesor)

        fun bind(item: ListItem.ProfesorItem) {
            tvNombre.text = item.profesor.nombre
                .split(" ")
                .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }

            // Mostrar materias: si hay búsqueda activa, destacar la que coincide primero
            val materias = item.profesor.materias
            tvMateria.text = if (item.materiaDestacada.isNotEmpty()) {
                item.materiaDestacada.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                materias.joinToString(" · ") { m ->
                    m.lowercase().replaceFirstChar { it.uppercase() }
                }
            }
            tvMateria.visibility = if (materias.isNotEmpty()) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onClick(item.profesor) }
        }
    }
}
