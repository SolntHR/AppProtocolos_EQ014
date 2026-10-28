package com.example.appprotocolos_eq014.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appprotocolos_eq014.model.EstadoProtocolo
import com.example.appprotocolos_eq014.model.ProtocoloListado
import com.example.appprotocolos_eq014.model.ResultadoPrueba
import com.example.appprotocolos_eq014.ui.styles.EstilosCampo
import com.example.appprotocolos_eq014.ui.theme.AmarilloAviso
import com.example.appprotocolos_eq014.ui.theme.AmarilloSuave
import com.example.appprotocolos_eq014.ui.theme.RojoError
import com.example.appprotocolos_eq014.ui.theme.RojoSuave

fun tonoDeEstado(estado: EstadoProtocolo): TonoEstado = when (estado) {
    EstadoProtocolo.FORMALIZADO -> TonoEstado.EXITO
    EstadoProtocolo.REGISTRADO_TERRENO -> TonoEstado.AVISO
    EstadoProtocolo.OBSERVADO -> TonoEstado.ERROR
    EstadoProtocolo.BORRADOR -> TonoEstado.AZUL
    EstadoProtocolo.ANULADO -> TonoEstado.NEUTRO
}

@Composable
fun EtiquetaEstadoProtocolo(estado: EstadoProtocolo, modifier: Modifier = Modifier) {
    EtiquetaEstado(texto = estado.etiqueta, tono = tonoDeEstado(estado), modifier = modifier)
}

@Composable
fun EtiquetaProtocoloListado(protocolo: ProtocoloListado, modifier: Modifier = Modifier) {
    val reemplazo = protocolo.reemplazadoPorNumero
    if (reemplazo != null) {
        EtiquetaEstado(texto = "Reemplazado por nº $reemplazo", tono = TonoEstado.NEUTRO, modifier = modifier)
    } else {
        EtiquetaEstadoProtocolo(protocolo.estado, modifier)
    }
}

@Composable
fun EtiquetaResultadoProtocolo(resultado: ResultadoPrueba?, modifier: Modifier = Modifier) {
    if (resultado == null) EtiquetaEstado(texto = "Pendiente", tono = TonoEstado.NEUTRO, modifier = modifier)
    else EtiquetaResultado(resultado, modifier)
}

@Composable
fun MensajeAvisoProtocolo(texto: String, esError: Boolean = true, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (esError) RojoSuave else AmarilloSuave)
            .padding(12.dp)
    ) {
        Text(texto, color = if (esError) RojoError else AmarilloAviso, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MensajeCentradoProtocolo(texto: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccion: T?,
    textoDe: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    error: String? = null,
    textoVacio: String = "Selecciona una opción"
) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = abierto && habilitado,
        onExpandedChange = { if (habilitado) abierto = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccion?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            placeholder = { Text(if (opciones.isEmpty()) "Sin opciones disponibles" else textoVacio) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
            isError = error != null,
            supportingText = if (error != null) ({ Text(error) }) else null,
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, habilitado)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = abierto && habilitado, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        abierto = false
                        onSeleccion(opcion)
                    }
                )
            }
        }
    }
}
