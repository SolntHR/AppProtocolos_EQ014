package com.example.appprotocolos_eq014.model

data class Instrumento(
    val id: Long = 0,
    val nombreTipo: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String
)
