package com.tesis.appmovil.ui.business

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tesis.appmovil.R
import com.tesis.appmovil.viewmodel.NegocioViewModel

@Composable
fun BusinessReadyScreen(
    negocioViewModel: NegocioViewModel, // ← Agregar este parámetro
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 👇 Asegúrate de tener tu ilustración en res/drawable con este nombre
        Image(
            painter = painterResource(id = R.drawable.business_ready),
            contentDescription = "Negocio listo",
            modifier = Modifier
                .size(150.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Tu negocio está listo",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPublish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                "PUBLICAR NEGOCIO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
