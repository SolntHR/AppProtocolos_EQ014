package com.example.appprotocolos_eq014.model

data class Prueba(
    val id: Long = 0,
    val protocoloId: String = "",
    val tipo: TipoPrueba,
    val elemento: String,
    val momento: String = "UNICA",
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null,
    val instrumentoId: Long? = null,
    val resultado: ResultadoPrueba? = null,
    val comentario: String? = null,
    val ingresadoManualmente: Boolean = false,
    val mediciones: List<Medicion> = emptyList(),

    val instrumentoNombre: String? = null,

    val calculo: String? = null,
    val comparaciones: List<Comparacion> = emptyList()
){
    val valores: Map<String, Double>
        get() = mediciones.associate { it.parametroCodigo to it.valor }

    companion object{
        fun medicionesDesde( valores: Map<String, Double> ): List<Medicion> =
            valores.mapNotNull { (codigo, valor)  ->
                    Parametros.porCodigo(codigo)?.let {campo ->
                        Medicion(
                            parametroId = campo.idParametro ?:0,
                            parametroCodigo = codigo,
                            parametroNombre = campo.etiqueta,
                            valor = valor,
                            unidad = campo.unidad
                        )
                    }
            }
    }
}