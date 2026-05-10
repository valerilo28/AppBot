package com.example.escom_appcelular

import android.content.Context
import org.json.JSONArray

object ProfesorRepository {

    private var profesores: List<Profesor>? = null

    fun getAll(context: Context): List<Profesor> {
        if (profesores != null) return profesores!!

        val json = context.assets.open("profesores.json")
            .bufferedReader().use { it.readText() }

        val array = JSONArray(json)
        val lista = mutableListOf<Profesor>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val nombre = obj.getString("nombre")

            val materias = mutableListOf<String>()
            val materiasArr = obj.getJSONArray("materias")
            for (j in 0 until materiasArr.length())
                materias.add(materiasArr.getString(j))

            val horario = mutableListOf<HorarioEntry>()
            val horarioArr = obj.getJSONArray("horario")
            for (j in 0 until horarioArr.length()) {
                val h = horarioArr.getJSONObject(j)
                horario.add(HorarioEntry(
                    dia = h.getString("dia"),
                    entrada = h.getString("entrada"),
                    salida = h.getString("salida"),
                    materia = h.getString("materia"),
                    salon = h.getString("salon")
                ))
            }
            lista.add(Profesor(nombre, materias, horario))
        }
        profesores = lista
        return lista
    }
}