package com.tesis.appmovil.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tesis.appmovil.ui.account.ChangePasswordScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBusinessScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var nombreNegocio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var distrito by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Registra tu negocio",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(Modifier.height(4.dp))

        // Subtítulo
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = "Completa los datos principales de tu negocio.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(24.dp))

        // Nombre del negocio
        OutlinedTextField(
            value = nombreNegocio,
            onValueChange = { nombreNegocio = it },
            placeholder = { Text("Ej. Spa Relax") },
            label = { Text("Nombre del negocio") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(Modifier.height(12.dp))

        // Categoria
        ExposedDropdownMenuBox(
            expanded = false, // luego puedes manejar el estado
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = categoria,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elije una opción") },
                label = { Text("Categoría") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Ciudad
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = ciudad,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elije una opción") },
                label = { Text("Ciudad") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Distrito
        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = distrito,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elije una opción") },
                label = { Text("Distrito") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Dirección
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            placeholder = { Text("Ej: Av. Larco 1234, Miraflores") },
            label = { Text("Dirección exacta") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(24.dp))

        // Botón continuar
        Button(
            onClick = { onContinue() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C1349)) // tu morado
        ) {
            Text("CONTINUAR →")
        }
    }
}
