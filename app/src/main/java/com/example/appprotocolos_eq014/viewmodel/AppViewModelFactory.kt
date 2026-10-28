package com.example.appprotocolos_eq014.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appprotocolos_eq014.data.InduztekDbHelper
import com.example.appprotocolos_eq014.data.SesionPreferencias
import com.example.appprotocolos_eq014.repository.CatalogoRepository
import com.example.appprotocolos_eq014.repository.FotoPerfilRepository
import com.example.appprotocolos_eq014.repository.InstrumentoRepository
import com.example.appprotocolos_eq014.repository.ProtocoloRepository
import com.example.appprotocolos_eq014.repository.UsuarioRepository
import com.example.appprotocolos_eq014.repository.VisitaRepository
import com.example.appprotocolos_eq014.repository.RegistroProtocoloRepository
import com.example.appprotocolos_eq014.repository.RegistroCatalogoRepository

const val ARG_ID_USUARIO = "idUsuario"
const val ARG_ID_EQUIPO = "idEquipo"
const val ARG_ID_VISITA = "idVisita"

object AppViewModelFactory {

    private var dbHelper: InduztekDbHelper? = null

    private fun obtenerDbHelper(context: Context): InduztekDbHelper =
        dbHelper ?: InduztekDbHelper(context.applicationContext).also { dbHelper = it }

    private fun CreationExtras.contexto(): Context = checkNotNull(this[APPLICATION_KEY])

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        // Registro de protocolos (formulario, listado y detalle)
        initializer {
            val contexto = contexto()
            FormularioProtocoloViewModel(
                sesion = SesionPreferencias(contexto),
                repository = RegistroProtocoloRepository(obtenerDbHelper(contexto)),
                contexto = contexto.applicationContext
            )
        }
        initializer {
            val contexto = contexto()
            ProtocolosViewModel(
                sesion = SesionPreferencias(contexto),
                repository = RegistroProtocoloRepository(obtenerDbHelper(contexto)),
                contexto = contexto.applicationContext
            )
        }
        // Registro de instalaciones y equipos (RF-01, RF-02)
        initializer {
            val contexto = contexto()
            FormularioEquipoViewModel(
                sesion = SesionPreferencias(contexto),
                repository = RegistroCatalogoRepository(obtenerDbHelper(contexto)),
                idInstalacionInicial = createSavedStateHandle().get<Long>(ARG_ID_INSTALACION) ?: SIN_INSTALACION
            )
        }
        initializer {
            val contexto = contexto()
            FormularioInstalacionViewModel(
                sesion = SesionPreferencias(contexto),
                repository = RegistroCatalogoRepository(obtenerDbHelper(contexto))
            )
        }
        initializer {
            val contexto = contexto()
            LoginViewModel(
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto)),
                sesionPreferencias = SesionPreferencias(contexto)
            )
        }
        initializer {
            val contexto = contexto()
            val db = obtenerDbHelper(contexto)
            InicioViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                catalogoRepository = CatalogoRepository(db),
                protocoloRepository = ProtocoloRepository(db),
                visitaRepository = VisitaRepository(db),
                usuarioRepository = UsuarioRepository(db),
                catalogoClienteRepository = RegistroCatalogoRepository(db)
            )
        }
        initializer {
            val contexto = contexto()
            HistorialViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                protocoloRepository = ProtocoloRepository(obtenerDbHelper(contexto)),
                catalogoClienteRepository = RegistroCatalogoRepository(obtenerDbHelper(contexto)),
                idEquipo = checkNotNull(createSavedStateHandle().get<Long>(ARG_ID_EQUIPO))
            )
        }
        initializer {
            val contexto = contexto()
            ActaViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                visitaRepository = VisitaRepository(obtenerDbHelper(contexto)),
                catalogoClienteRepository = RegistroCatalogoRepository(obtenerDbHelper(contexto)),
                contexto = contexto.applicationContext,
                idVisita = checkNotNull(createSavedStateHandle().get<Long>(ARG_ID_VISITA))
            )
        }
        initializer {
            InstrumentosViewModel(instrumentoRepository = InstrumentoRepository(obtenerDbHelper(contexto())))
        }
        initializer {
            RecuperarViewModel(usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto())))
        }
        initializer {
            val contexto = contexto()
            PerfilViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto)),
                fotoPerfilRepository = FotoPerfilRepository(contexto)
            )
        }
        initializer {
            val contexto = contexto()
            GestionUsuariosViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto)),
                fotoPerfilRepository = FotoPerfilRepository(contexto)
            )
        }
        initializer {
            val contexto = contexto()
            FichaUsuarioViewModel(
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto)),
                idUsuario = createSavedStateHandle().get<Long>(ARG_ID_USUARIO) ?: FichaUsuarioViewModel.NUEVO
            )
        }
        initializer {
            val contexto = contexto()
            EditarPerfilViewModel(
                sesionPreferencias = SesionPreferencias(contexto),
                usuarioRepository = UsuarioRepository(obtenerDbHelper(contexto))
            )
        }
    }
}
