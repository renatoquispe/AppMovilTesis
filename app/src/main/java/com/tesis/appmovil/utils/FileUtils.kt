package com.tesis.appmovil.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String = "imagen"
): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}
