//package com.tesis.appmovil.repository
//
//import com.tesis.appmovil.data.remote.ApiService
//import com.tesis.appmovil.data.remote.RetrofitClient
//import com.tesis.appmovil.data.remote.dto.HorarioCreate
//import com.tesis.appmovil.data.remote.dto.HorarioUpdate
//import com.tesis.appmovil.models.Horario
//import retrofit2.HttpException
//import retrofit2.Response
//
//class HorarioRepository(
//    private val api: ApiService = RetrofitClient.api
//) {
//    suspend fun listar(idNegocio: Int? = null): List<Horario> =
//        api.getHorarios(idNegocio).bodyOrThrow()
//
//    suspend fun obtener(id: Int): Horario =
//        api.getHorario(id).bodyOrThrow()
//
//    suspend fun crear(body: HorarioCreate): Horario =
//        api.createHorario(body).bodyOrThrow()
//
//    suspend fun actualizar(id: Int, body: HorarioUpdate): Horario =
//        api.updateHorario(id, body).bodyOrThrow()
//
//    suspend fun eliminar(id: Int) {
//        val resp = api.deleteHorario(id)
//        if (!resp.isSuccessful) throw HttpException(resp)
//    }
//}
//
//private fun <T> Response<T>.bodyOrThrow(): T {
//    if (isSuccessful) {
//        val b = body()
//        if (b != null) return b
//    }
//    throw HttpException(this)
//}
// repository/HorarioRepository.kt
package com.tesis.appmovil.repository

import HorarioCreate
import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.models.Horario

class HorarioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun crearHorario(horario: HorarioCreate): Horario {
        val response = api.createHorario(horario)
        if (response.isSuccessful && response.body()?.success == true) {
            return response.body()?.data ?: throw IllegalStateException("Horario creado pero sin datos de retorno")
        } else {
            throw Exception(response.body()?.message ?: "Error al crear horario")
        }
    }

    suspend fun crearHorariosLote(horarios: List<HorarioCreate>): List<Horario> {
        val resultados = mutableListOf<Horario>()
        for (horario in horarios) {
            try {
                val resultado = crearHorario(horario)
                resultados.add(resultado)
            } catch (e: Exception) {
                println("❌ Error creando horario para ${horario.diaSemana}: ${e.message}")
                throw e // O puedes continuar con los demás: // continue
            }
        }
        return resultados
    }

    suspend fun obtenerHorariosPorNegocio(idNegocio: Int): List<Horario> {
        val response = api.getHorariosByNegocio(idNegocio)
        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Error al obtener horarios")
        }
    }
}