package com.example.appprotocolos_eq014.model

// ---------------------------------------------------------------
// Registro de instalaciones (RF-01) y equipos (RF-02, HU-01)
// ---------------------------------------------------------------

data class OpcionCatalogo(
    val id: Long,
    val nombre: String
)

// HU-01: equipo que ya existe con el mismo TAG en la instalacion (RN-01)
data class EquipoExistente(
    val id: Long,
    val tag: String,
    val descripcion: String
)

data class FormularioEquipoUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val instalacionFija: Boolean = false,          // viene desde una visita: no se puede cambiar
    val instalaciones: List<OpcionCatalogo> = emptyList(),
    val tipos: List<OpcionCatalogo> = emptyList(),
    val instalacion: OpcionCatalogo? = null,
    val tipo: OpcionCatalogo? = null,
    val tag: String = "",
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val anio: String = "",
    val potenciaKva: String = "",
    val tensiones: String = "",
    val corriente: String = "",
    val maxContactos: String = "",
    val observaciones: String = "",
    val errores: Map<String, String> = emptyMap(), // campo -> mensaje
    val equipoExistente: EquipoExistente? = null,
    val error: String? = null,
    val equipoGuardadoId: Long? = null
) {
    // Datos de placa que pide cada tipo de equipo
    val pidePotencia: Boolean get() = tipo?.id == ReglasProtocolo.TIPO_TRANSFORMADOR
    val pideContactos: Boolean get() = tipo?.id == ReglasProtocolo.TIPO_INTERRUPTOR || tipo?.id == ReglasProtocolo.TIPO_CELDA
    val pideCorriente: Boolean get() = tipo != null
}

data class FormularioInstalacionUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val clientes: List<OpcionCatalogo> = emptyList(),
    val comunas: List<OpcionCatalogo> = emptyList(),
    val cliente: OpcionCatalogo? = null,
    val comuna: OpcionCatalogo? = null,
    val nombre: String = "",
    val ubicacion: String = "",
    val observacionesAcceso: String = "",
    val errores: Map<String, String> = emptyMap(),
    val error: String? = null,
    val instalacionGuardadaId: Long? = null
)
