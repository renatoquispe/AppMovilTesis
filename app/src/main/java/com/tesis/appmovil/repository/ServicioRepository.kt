package com.tesis.appmovil.repository

import android.util.Log
import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.data.remote.dto.ServicioCreate
import com.tesis.appmovil.data.remote.dto.ServicioUpdate
import com.tesis.appmovil.data.remote.ApiResponse
import com.tesis.appmovil.models.Servicio
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException

private const val TAG = "ServicioRepository"

class ServicioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    /** Lista servicios; opcionalmente por negocio */
    suspend fun listar(idNegocio: Int? = null): List<Servicio> {
        try {
            println("🔍 DEBUG ServicioRepository - listar con idNegocio: $idNegocio")
            println("🔍 DEBUG ServicioRepository - llamando a api.getServicios($idNegocio)")

            val resp = api.getServicios(idNegocio)

            println("🔍 DEBUG ServicioRepository - respuesta HTTP: ${resp.code()}")

            if (resp.isSuccessful) {
                val body = resp.body()
                println("🔍 DEBUG ServicioRepository - body success: ${body?.success}")
                println("🔍 DEBUG ServicioRepository - body message: ${body?.message}")

                if (body != null && body.success && body.data != null) {
                    @Suppress("UNCHECKED_CAST")
                    val servicios = body.data as List<Servicio>
                    println("🔍 DEBUG ServicioRepository - servicios obtenidos: ${servicios.size}")
                    servicios.forEach { servicio ->
                        println("   - Servicio: ${servicio.nombre} (Negocio ID: ${servicio.idNegocio})")
                    }
                    return servicios
                }
                throw Exception(body?.message ?: "Lista de servicios vacía o fallida")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "listar error", e)
            throw e
        }
    }
//    suspend fun listar(idNegocio: Int? = null): List<Servicio> {
//        try {
//            val resp = api.getServicios(idNegocio)
//            if (resp.isSuccessful) {
//                val body = resp.body()
//                if (body != null && (body as ApiResponse<*>).success && (body as ApiResponse<List<Servicio>>).data != null) {
//                    @Suppress("UNCHECKED_CAST")
//                    return (body as ApiResponse<List<Servicio>>).data as List<Servicio>
//                }
//                throw Exception(body?.let { (it as? ApiResponse<*>)?.message } ?: "Lista de servicios vacía o fallida")
//            } else {
//                throw httpException(resp)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "listar error", e)
//            throw e
//        }
//    }

    /** Crea y devuelve el servicio creado */
    // En ServicioRepository.kt - AGREGAR si no existe
    suspend fun crear(body: ServicioCreate): Servicio {
        try {
            println("🔍 ServicioRepository - Creando servicio: ${body.nombre}")

            val resp = api.createServicio(body)

            println("🔍 ServicioRepository - Respuesta HTTP: ${resp.code()}")

            if (resp.isSuccessful) {
                val apiResp = resp.body()
                println("🔍 ServicioRepository - body success: ${apiResp?.success}")

                if (apiResp != null && apiResp.success && apiResp.data != null) {
                    return apiResp.data
                }
                throw Exception(apiResp?.message ?: "Error al crear servicio")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "crear error", e)
            throw e
        }
    }
    // En ServicioRepository.kt - AGREGA ESTE MÉTODO
    suspend fun obtenerOfertas(): List<Servicio> {
        try {
            println("🔍 DEBUG ServicioRepository - obteniendo ofertas")

            val resp = api.getOfertas()
            println("🔍 DEBUG ServicioRepository - respuesta ofertas HTTP: ${resp.code()}")

            if (resp.isSuccessful) {
                val body = resp.body()
                println("🔍 DEBUG ServicioRepository - ofertas body success: ${body?.success}")
                println("🔍 DEBUG ServicioRepository - ofertas body message: ${body?.message}")

                if (body != null && body.success && body.data != null) {
                    @Suppress("UNCHECKED_CAST")
                    val ofertas = body.data as List<Servicio>
                    println("🔍 DEBUG ServicioRepository - ofertas obtenidas: ${ofertas.size}")
                    ofertas.forEach { servicio ->
                        println("   - Oferta: ${servicio.nombre} - Descuento: ${servicio.descuento}%")
                    }
                    return ofertas
                }
                throw Exception(body?.message ?: "Lista de ofertas vacía o fallida")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "obtenerOfertas error", e)
            throw e
        }
    }
//    suspend fun crear(body: ServicioCreate): Servicio {
//        try {
//            val resp = api.createServicio(body)
//            if (resp.isSuccessful) {
//                val apiResp = resp.body()
//                if (apiResp != null && apiResp.success && apiResp.data != null) {
//                    @Suppress("UNCHECKED_CAST")
//                    return apiResp.data as Servicio
//                }
//                throw Exception(apiResp?.message ?: "Error al crear servicio")
//            } else {
//                throw httpException(resp)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "crear error", e)
//            throw e
//        }
//    }

    /**
     * Crea servicio y (opcional) sube imagen en un solo flujo.
     * Retorna el Servicio creado.
     * - imagenPart puede ser null si no quieres subir imagen.
     */
    suspend fun crearConImagen(body: ServicioCreate, imagenPart: MultipartBody.Part? = null): Servicio {
        val servicio = crear(body) // lanzará excepción si falla
        if (imagenPart != null) {
            try {
                subirImagen(servicio.idServicio, imagenPart)
            } catch (e: Exception) {
                Log.e(TAG, "crearConImagen: subida de imagen falló para id=${servicio.idServicio}", e)
                throw e
            }
        }
        return servicio
    }

    /** Obtiene un servicio por id */
    suspend fun obtener(id: Int): Servicio {
        try {
            val resp = api.getServicio(id)
            if (resp.isSuccessful) {
                val apiResp = resp.body()
                if (apiResp != null && apiResp.success && apiResp.data != null) {
                    @Suppress("UNCHECKED_CAST")
                    return apiResp.data as Servicio
                }
                throw Exception(apiResp?.message ?: "Error al obtener servicio")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "obtener error", e)
            throw e
        }
    }

    /** Actualiza y devuelve el servicio actualizado */
    suspend fun actualizar(id: Int, body: ServicioUpdate): Servicio {
        try {
            val resp = api.updateServicio(id, body)
            if (resp.isSuccessful) {
                val apiResp = resp.body()
                // 🔧 Cambio clave:
                // Algunas APIs devuelven success=true y SOLO un mensaje "Servicio actualizado" (sin data).
                // En ese caso NO lanzamos excepción; hacemos un GET para traer el objeto actualizado.
                if (apiResp != null && apiResp.success) {
                    val data = apiResp.data
                    @Suppress("UNCHECKED_CAST")
                    return if (data != null) {
                        data as Servicio
                    } else {
                        Log.i(TAG, "actualizar: success sin data; reconsultando id=$id")
                        obtener(id) // trae el servicio actualizado
                    }
                }
                throw Exception(apiResp?.message ?: "Error al actualizar servicio")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "actualizar error", e)
            throw e
        }
    }

    /** Elimina un servicio (lanza excepción si falla) */
    suspend fun eliminar(id: Int) {
        try {
            val resp = api.deleteServicio(id)
            if (!resp.isSuccessful) {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "eliminar error", e)
            throw e
        }
    }

    /**
     * Sube la imagen para un servicio existente.
     *
     * Tu ApiService debe declarar:
     * @Multipart
     * @POST("servicios/{id}/imagen")
     * suspend fun uploadServiceImage(
     *   @Path("id") id: Int,
     *   @Part imagen: MultipartBody.Part
     * ): Response<ApiResponse<Servicio>>  // <- idealmente devuelve el servicio con la nueva URL
     */
    suspend fun subirImagen(id: Int, imagen: MultipartBody.Part): Servicio {
        try {
            Log.d(TAG, "subirImagen: iniciando upload para id=$id")
            val resp = api.uploadServiceImage(id, imagen)
            if (resp.isSuccessful) {
                val apiResp = resp.body()
                if (apiResp != null && apiResp.success && apiResp.data != null) {
                    return apiResp.data
                } else if (apiResp != null && apiResp.success && apiResp.data == null) {
                    // Si no retorna data, reconsultar
                    return obtener(id)
                } else {
                    throw Exception(apiResp?.message ?: "Error al subir imagen")
                }
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "subirImagen error para id=$id", e)
            throw e
        }
    }

    // En ServicioRepository.kt - AGREGAR ESTAS FUNCIONES

    suspend fun subirImagenServicio(idServicio: Int, imagenPart: MultipartBody.Part): Servicio {
        try {
            println("🔍 ServicioRepository - Subiendo imagen para servicio: $idServicio")

            val resp = api.uploadServiceImage(idServicio, imagenPart)

            println("🔍 ServicioRepository - Respuesta HTTP: ${resp.code()}")

            if (resp.isSuccessful) {
                val apiResp = resp.body()
                println("🔍 ServicioRepository - body success: ${apiResp?.success}")

                if (apiResp != null && apiResp.success && apiResp.data != null) {
                    return apiResp.data
                }
                throw Exception(apiResp?.message ?: "Error al subir imagen")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "subirImagenServicio error para id=$idServicio", e)
            throw e
        }
    }

    suspend fun eliminarImagenServicio(idServicio: Int): Servicio {
        try {
            println("🔍 ServicioRepository - Eliminando imagen de servicio: $idServicio")

            val resp = api.deleteServiceImage(idServicio)

            println("🔍 ServicioRepository - Respuesta HTTP: ${resp.code()}")

            if (resp.isSuccessful) {
                val apiResp = resp.body()
                println("🔍 ServicioRepository - body success: ${apiResp?.success}")

                if (apiResp != null && apiResp.success && apiResp.data != null) {
                    return apiResp.data
                }
                throw Exception(apiResp?.message ?: "Error al eliminar imagen")
            } else {
                throw httpException(resp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "eliminarImagenServicio error para id=$idServicio", e)
            throw e
        }
    }

    // -----------------------
    // Helpers
    // -----------------------

    private fun <T> httpException(resp: Response<T>): Exception {
        val code = resp.code()
        return try {
            val errorBody = resp.errorBody()?.string()
            Log.e(TAG, "HTTP $code - errorBody: $errorBody")
            Exception("HTTP $code - ${errorBody ?: "Sin cuerpo de error"}")
        } catch (ioe: IOException) {
            Log.e(TAG, "httpException: fallo parse errorBody", ioe)
            Exception("HTTP $code - (no se pudo leer body)")
        }
    }
}
