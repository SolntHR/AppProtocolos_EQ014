package com.example.appprotocolos_eq014.model

import android.graphics.Bitmap
import androidx.annotation.StringRes

data class DatoPerfil(
    val etiqueta: String,
    val valor: String
)

data class PerfilUiState(
    val isLoading: Boolean = true,
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val esCliente: Boolean = false,
    val esJefeTecnico: Boolean = false,
    val datos: List<DatoPerfil> = emptyList(),
    val foto: Bitmap? = null,
    val fotoPendiente: Bitmap? = null,
    val procesandoFoto: Boolean = false,
    @StringRes val mensaje: Int? = null,
    val error: String? = null,
    val sesionCerrada: Boolean = false
)
