package com.example.escom_appcelular

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AreaAdapter(
    private val allAreas: List<Area>,
    private val onClick: (Area) -> Unit
) : RecyclerView.Adapter<AreaAdapter.ViewHolder>(), Filterable {

    // Lista que se muestra (puede ser filtrada)
    private var filteredAreas: List<Area> = allAreas.toList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvArea: TextView = itemView.findViewById(R.id.tvAreaNombre)
        val tvCargo: TextView = itemView.findViewById(R.id.tvAreaCargo)
        val ivFoto: ImageView = itemView.findViewById(R.id.ivAreaFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_area, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val area = filteredAreas[position]
        holder.tvArea.text = area.area
        holder.tvCargo.text = area.responsable

        val resId = holder.itemView.context.resources.getIdentifier(
            area.foto, "drawable", holder.itemView.context.packageName
        )
        holder.ivFoto.setImageResource(if (resId != 0) resId else R.drawable.ic_avatar)
        holder.itemView.setOnClickListener { onClick(area) }
    }

    override fun getItemCount(): Int = filteredAreas.size

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val query = constraint?.toString()?.trim()?.lowercase() ?: ""
            val result = if (query.isEmpty()) {
                allAreas.toList()
            } else {
                allAreas.filter { area ->
                    area.area.lowercase().contains(query) ||
                    area.responsable.lowercase().contains(query) ||
                    area.cargo.lowercase().contains(query)
                }
            }
            return FilterResults().apply { values = result }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredAreas = (results?.values as? List<Area>) ?: emptyList()
            notifyDataSetChanged()
        }
    }
}
