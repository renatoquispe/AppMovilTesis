package com.tesis.appmovil.ui.auth

import androidx.compose.ui.tooling.preview.Preview


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón Cliente
            Button(
                onClick = onClient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = MaterialTheme.shapes.extraLarge, // redondeado tipo cápsula
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B1B5A), // vino
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.PersonSearch, contentDescription = "Cliente")
                Spacer(Modifier.width(8.dp))
                Text("Cliente",
                    style = MaterialTheme.typography.bodyLarge)
            }

            // Botón Profesional
            Button(
                onClick = onProfessional,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3C3B91), // azul
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = "Profesional")
                Spacer(Modifier.width(8.dp))
                Text("Profesional",
                    style = MaterialTheme.typography.bodyLarge)
            }
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

//@Composable
//fun ChooseRoleScreen(
//    onClient: () -> Unit,
//    onProfessional: () -> Unit
//) {
//    Box(Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .padding(24.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Elige tu perfil",
//                style = MaterialTheme.typography.headlineSmall)
//
//            Button(
//                onClick = onClient,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(60.dp)
//            ) { Text("Cliente") }
//
//            Button(
//                onClick = onProfessional,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(60.dp)
//            ) { Text("Profesional") }
//        }
//    }
//}
//@Preview(showBackground = true)
//@Composable
//fun PreviewChooseRoleScreen() {
//    ChooseRoleScreen(
//        onClient = {},
//        onProfessional = {}
//    )
//}
