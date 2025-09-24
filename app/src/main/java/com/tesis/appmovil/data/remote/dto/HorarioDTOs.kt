import com.google.gson.annotations.SerializedName

data class HorarioCreate(
    @SerializedName("idNegocio")
    val idNegocio: Int,

    @SerializedName("diaSemana")
    val diaSemana: String,

    @SerializedName("horaApertura")
    val horaApertura: String,

    @SerializedName("horaCierre")
    val horaCierre: String
)

data class HorarioUpdate(
    val dia_semana: String? = null,
    val hora_apertura: String? = null,
    val hora_cierre: String? = null
    // estado_auditoria no va en Create; en Update solo si tu back lo usa
)
