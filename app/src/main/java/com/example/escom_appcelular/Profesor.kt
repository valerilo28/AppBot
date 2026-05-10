package com.example.escom_appcelular

data class HorarioEntry(
    val dia: String,
    val entrada: String,
    val salida: String,
    val materia: String,
    val salon: String
)

data class Profesor(
    val nombre: String,
    val materias: List<String>,
    val horario: List<HorarioEntry>
)