package com.example.appprotocolos_eq014.ui.screens.perfil

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R
import com.example.appprotocolos_eq014.ui.components.BotonPrincipal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaOpcionesFoto(
    tieneFoto: Boolean,
    onTomarFoto: () -> Unit,
    onElegirGaleria: () -> Unit,
    onEliminarFoto: () -> Unit,
    onCerrar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.foto_titulo),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OpcionFoto(
                icono = R.drawable.ic_camara,
                titulo = stringResource(R.string.foto_tomar),
                detalle = stringResource(R.string.foto_tomar_detalle),
                onClick = onTomarFoto
            )
            OpcionFoto(
                icono = R.drawable.ic_galeria,
                titulo = stringResource(R.string.foto_galeria),
                detalle = stringResource(R.string.foto_galeria_detalle),
                onClick = onElegirGaleria
            )
            if (tieneFoto) {
                OpcionFoto(
                    icono = R.drawable.ic_eliminar,
                    titulo = stringResource(R.string.foto_eliminar),
                    detalle = stringResource(R.string.foto_eliminar_detalle),
                    onClick = onEliminarFoto,
                    colorPrincipal = MaterialTheme.colorScheme.error,
                    colorFondoIcono = MaterialTheme.colorScheme.errorContainer
                )
            }
        }
    }
}

@Composable
private fun OpcionFoto(
    @DrawableRes icono: Int,
    titulo: String,
    detalle: String,
    onClick: () -> Unit,
    colorPrincipal: Color = MaterialTheme.colorScheme.primary,
    colorFondoIcono: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(colorFondoIcono),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = colorPrincipal,
                modifier = Modifier.size(22.dp)
            )
        }
        Column {
            Text(
                text = titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (colorPrincipal == MaterialTheme.colorScheme.error) colorPrincipal else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detalle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DialogoVistaPreviaFoto(
    foto: Bitmap,
    guardando: Boolean,
    onUsar: () -> Unit,
    onCancelar: () -> Unit
) {
    val imagen = remember(foto) { foto.asImageBitmap() }
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.foto_vista_previa),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = imagen,
                    contentDescription = stringResource(R.string.foto_vista_previa),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                )
            }
        },
        confirmButton = {
            BotonPrincipal(
                texto = stringResource(R.string.foto_usar),
                onClick = onUsar,
                habilitado = !guardando
            )
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.dialogo_cancelar))
            }
        }
    )
}
