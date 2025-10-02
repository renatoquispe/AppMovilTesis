package com.tesis.appmovil.repository

import HorarioCreate
import HorarioUpdate
import com.tesis.appmovil.data.remote.ApiService
import com.tesis.appmovil.data.remote.RetrofitClient
import com.tesis.appmovil.models.Horario
import com.tesis.appmovil.viewmodel.HorarioUi

class HorarioRepository(
    private val api: ApiService = RetrofitClient.api
) {
    private fun Any?.toBool01(): Boolean = when (this) {
        is Boolean -> this
        is Number  -> this.toInt() == 1
        is String  -> this == "1" || this.equals("true", ignoreCase = true)
        else       -> false
    }
    /** Crea un horario (una fila) */
    suspend fun crearHorario(horario: HorarioCreate): Horario {
        val resp = api.createHorario(horario)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al crear horario (HTTP ${resp.code()})")
        }
        return resp.body()?.data
            ?: throw IllegalStateException("Horario creado pero sin datos de retorno")
    }

    /** Crea varios horarios en lote (Lunes..Domingo, por ejemplo) */
    suspend fun crearHorariosLote(horarios: List<HorarioCreate>): List<Horario> {
        val creados = mutableListOf<Horario>()
        for (h in horarios) {
            val r = crearHorario(h)
            creados += r
        }
        return creados
    }

    /** Devuelve la lista de Horario (modelo de datos) de un negocio */
    suspend fun obtenerHorariosPorNegocio(idNegocio: Int): List<Horario> {
        val resp = api.getHorariosByNegocio(idNegocio)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al obtener horarios (HTTP ${resp.code()})")
        }
        return resp.body()?.data ?: emptyList()
    }

    /** Devuelve la lista ya mapeada al modelo de UI */
    suspend fun getHorariosByNegocio(idNegocio: Int): List<HorarioUi> {
        val resp = api.getHorariosByNegocio(idNegocio)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al obtener horarios (HTTP ${resp.code()})")
        }
        val data: List<Horario> = resp.body()?.data ?: emptyList()
        return data.map { h ->
            HorarioUi(
                id           = h.idHorario ?: 0,
                diaSemana    = h.diaSemana ?: "",
                horaApertura = (h.horaApertura ?: "").take(5),
                horaCierre   = (h.horaCierre ?: "").take(5),
                // 👇 FIX: mapea estado_auditoria
                habilitado   = (h.estado ?: h.estado ?: 0).toBool01()
            )
        }
    }

    suspend fun updateHorario(
        id: Int,
        horaApertura: String,
        horaCierre: String
    ): HorarioUi {
        val body = HorarioUpdate(
            hora_apertura = horaApertura,
            hora_cierre   = horaCierre
        )

        val resp = api.updateHorario(id = id, body = body)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al actualizar horario (HTTP ${resp.code()})")
        }

        val h: Horario? = resp.body()?.data
        return if (h != null) {
            HorarioUi(
                id           = h.idHorario ?: id,
                diaSemana    = h.diaSemana ?: "",
                horaApertura = (h.horaApertura ?: horaApertura).take(5),
                horaCierre   = (h.horaCierre ?: horaCierre).take(5),
                habilitado   = (h.estado ?: h.estado ?: 0).toBool01()
            )
        } else {
            HorarioUi(
                id           = id,
                diaSemana    = "",
                horaApertura = horaApertura.take(5),
                horaCierre   = horaCierre.take(5),
                habilitado   = true // fallback
            )
        }
    }


    suspend fun crearHorario(
        negocioId: Int,
        diaSemana: String,
        horaApertura: String,
        horaCierre: String
    ): HorarioUi {
        val body = HorarioCreate(
            idNegocio = negocioId,
            diaSemana = diaSemana,
            horaApertura = horaApertura,
            horaCierre = horaCierre
        )

        val resp = api.postHorario(body)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al crear horario (HTTP ${resp.code()})")
        }

        val h: Horario = resp.body()?.data
            ?: throw IllegalStateException("Respuesta sin dato de horario creado")

        return HorarioUi(
            id           = h.idHorario ?: 0,
            diaSemana    = h.diaSemana ?: "",
            horaApertura = (h.horaApertura ?: horaApertura).take(5),
            horaCierre   = (h.horaCierre ?: horaCierre).take(5),
            habilitado   = (h.estado ?: h.estado ?: 1).toBool01()
        )
    }

    suspend fun desactivarHorario(id: Int) {
        val resp = api.desactivarHorario(id)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al desactivar horario")
        }
    }

    suspend fun activarHorario(id: Int) {
        val resp = api.activarHorario(id)
        if (!resp.isSuccessful || resp.body()?.success != true) {
            throw Exception(resp.body()?.message ?: "Error al activar horario")
        }
    }
}
