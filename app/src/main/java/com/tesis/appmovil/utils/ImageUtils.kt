package com.tesis.appmovil.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "ImageUtils"

/**
 * Convierte un Uri (content://) a MultipartBody.Part listo para enviar con Retrofit.
 * - partName: el nombre del campo que espera tu backend (ej. "imagen")
 * - mimeType: por defecto "image/jpeg" para mayor compatibilidad
 *
 * Uso: val part = imageUri.toMultipart(context, "imagen")
 */
suspend fun Uri.toMultipart(
    context: Context,
    partName: String,
    mimeType: String = "image/jpeg"
): MultipartBody.Part = withContext(Dispatchers.IO) {
    val input = context.contentResolver.openInputStream(this@toMultipart)
        ?: throw IllegalArgumentException("No se pudo abrir URI")

    // Archivo temporal con extensión .jpg (ayuda al backend a reconocer el tipo)
    val tempFile = File.createTempFile("upload_${System.currentTimeMillis()}", ".jpg", context.cacheDir)

    // Copiamos el contenido del URI al archivo temporal
    tempFile.outputStream().use { out ->
        input.use { inp ->
            inp.copyTo(out)
        }
    }

    val size = tempFile.length()
    Log.d(TAG, "Temp file creado: ${tempFile.absolutePath}, size=$size")
    if (size <= 0L) {
        // si está vacío, borramos y fallamos
        try { tempFile.delete() } catch (_: Exception) {}
        throw IllegalArgumentException("El archivo temporal está vacío")
    }

    // Construir RequestBody y MultipartBody.Part
    val reqBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
    MultipartBody.Part.createFormData(partName, tempFile.name, reqBody)
}
