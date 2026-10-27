package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository

object AppViewModelFactory {

    private var dbHelper: InduztekDbHelper? = null

    private fun obtenerDbHelper(context: Context): InduztekDbHelper =
        dbHelper ?: InduztekDbHelper(context.applicationContext).also { dbHelper = it }

    private fun CreationExtras.contexto(): Context = checkNotNull(this[APPLICATION_KEY])

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val contexto = contexto()
            LoginViewModel(
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto)),
                sesionPreferencias = SesionPreferencias(contexto)
            )
        }
        initializer {
            val contexto = contexto()
            InicioViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                catalogoRepository = CatalogoRepository(obtenerDbHelper(contexto))
            )
        }
        initializer {
            val contexto = contexto()
            PerfilViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto))
            )
        }
    }
}
