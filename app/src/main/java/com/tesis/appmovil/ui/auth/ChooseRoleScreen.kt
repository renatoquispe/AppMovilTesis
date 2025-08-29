package com.tesis.appmovil.ui.auth

import androidx.compose.ui.tooling.preview.Preview


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChooseRoleScreen(
    onClient: () -> Unit,
    onProfessional: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Elige tu perfil",
                style = MaterialTheme.typography.headlineSmall)

            Button(
                onClick = onClient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) { Text("Cliente") }

            Button(
                onClick = onProfessional,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) { Text("Profesional") }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewChooseRoleScreen() {
    ChooseRoleScreen(
        onClient = {},
        onProfessional = {}
    )
}
