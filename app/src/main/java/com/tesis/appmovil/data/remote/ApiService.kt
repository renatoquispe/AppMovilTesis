package com.tesis.appmovil.data.remote
import HorarioCreate
import HorarioUpdate
import com.tesis.appmovil.data.remote.dto.*
import com.tesis.appmovil.data.remote.request.GoogleLoginRequest
import com.tesis.appmovil.data.remote.request.LoginRequest
import com.tesis.appmovil.data.remote.request.LoginResponse
import com.tesis.appmovil.data.remote.request.NegocioResponse
import com.tesis.appmovil.data.remote.request.PagedResponse
import com.tesis.appmovil.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.HttpException

interface ApiService {

    // ---------- AUTH ----------
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body body: GoogleLoginRequest): Response<LoginResponse>

    // ---------- USUARIOS ----------
    @GET("usuarios")
    suspend fun getUsuarios(): Response<List<Usuario>>

    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): Response<Usuario>

    @POST("usuarios")
    suspend fun createUsuario(@Body body: UsuarioCreate): Response<Usuario>

    @PUT("usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Int,
        @Body body: UsuarioUpdate
    ): Response<Usuario>

    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<Unit>

    @GET("usuarios/{id}/negocios")
    suspend fun getNegociosPorUsuario(@Path("id") idUsuario: Int): Response<ApiResponse<List<Negocio>>>

    // ---------- CATEGORIAS ----------
    @GET("categorias")
    suspend fun getCategorias(): Response<ApiResponse<List<Categoria>>>

    @GET("categorias/{id}")
    suspend fun getCategoria(@Path("id") id: Int): Response<ApiResponse<Categoria>>

    @POST("categorias")
    suspend fun createCategoria(@Body body: CategoriaCreate): Response<ApiResponse<Categoria>>

    @PUT("categorias/{id}")
    suspend fun updateCategoria(
        @Path("id") id: Int,
        @Body body: CategoriaUpdate
    ): Response<ApiResponse<Categoria>>

    @DELETE("categorias/{id}")
    suspend fun deleteCategoria(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------- NEGOCIOS ----------
    @GET("negocios")
    suspend fun getNegocios(
        // usa los nombres que tu backend espera
        @Query("idCategoria") idCategoria: Int? = null,
        @Query("idUbicacion") idUbicacion: Int? = null,

        // nuevos filtros
        @Query("q") q: String? = null,
        @Query("distrito") distrito: String? = null,
        @Query("ciudad") ciudad: String? = null,
        @Query("activos") activos: Boolean? = null,

        // paginación (tu back espera pageSize)
        @Query("page") page: Int? = 1,
        @Query("pageSize") pageSize: Int? = 20
    ): Response<ApiResponse<PagedResponse<Negocio>>>

    @GET("negocios/{id}")
    suspend fun getNegocio(
        @Path("id") id: Int
    ): Response<ApiResponse<Negocio>>

    @POST("negocios")
    suspend fun createNegocio(@Body body: NegocioCreate): Response<ApiResponse<Negocio>>

    @PUT("negocios/{id}")
    suspend fun updateNegocio(
        @Path("id") id: Int,
        @Body body: NegocioUpdate
    ): Response<ApiResponse<Negocio>>

    @DELETE("negocios/{id}")
    suspend fun deleteNegocio(@Path("id") id: Int): Response<Unit>

    // ---------- NEGOCIO-IMAGENES (subir/borrar) ----------
    @GET("negocio-imagenes")
    suspend fun listarNegocioImagenes(
        @Query("negocioId") negocioId: Long
    ): Response<List<NegocioImagen>>

    @GET("negocios/{id}")
    suspend fun getNegocioDetalle(
        @Path("id") id: Int
    ): Response<ApiResponse<NegocioResponse>>

//    @Multipart
//    @POST("negocio-imagenes")
//    suspend fun subirNegocioImagen(
//        @Part("negocioId") negocioId: RequestBody,
//        @Part imagen: MultipartBody.Part,
//        @Part("descripcion") descripcion: RequestBody? = null
//    ): Response<NegocioImagen>

    @Multipart
    @POST("negocio-imagenes/upload")
    suspend fun subirNegocioImagen(
        @Part("idNegocio") idNegocio: RequestBody,
        @Part imagen: MultipartBody.Part,
        @Part("descripcion") descripcion: RequestBody? = null
    ): Response<NegocioImagen>

    @DELETE("negocio-imagenes/{id}")
    suspend fun eliminarNegocioImagen(@Path("id") id: Long): Response<Unit>

    // ---------- SERVICIOS ----------
//    @GET("servicios")
//    suspend fun getServicios(
//        @Query("id_negocio") idNegocio: Int? = null
//    ): Response<ApiResponse<List<Servicio>>>
    @GET("servicios")
    suspend fun getServicios(
        @Query("idNegocio") idNegocio: Int? = null  // 🔄 Cambiado a camelCase
    ): Response<ApiResponse<List<Servicio>>>

    @GET("servicios/{id}")
    suspend fun getServicio(
        @Path("id") id: Int
    ): Response<ApiResponse<Servicio>>

    @POST("servicios")
    suspend fun createServicio(
        @Body body: ServicioCreate
    ): Response<ApiResponse<Servicio>>

    @PUT("servicios/{id}")
    suspend fun updateServicio(
        @Path("id") id: Int,
        @Body body: ServicioUpdate
    ): Response<ApiResponse<Servicio>>

    @DELETE("servicios/{id}")
    suspend fun deleteServicio(
        @Path("id") id: Int
    ): Response<Unit>


    @Multipart
    @POST("servicios/{id}/imagen")
    suspend fun uploadServiceImage(
        @Path("id") id: Int,
        @Part imagen: MultipartBody.Part
    ): Response<ApiResponse<Servicio>>

    // 👇 Para eliminar imagen de servicio
    @DELETE("servicios/{id}/imagen")
    suspend fun deleteServiceImage(
        @Path("id") id: Int
    ): Response<ApiResponse<Servicio>>

    // ---------- HORARIOS ----------
    @GET("horarios")
    suspend fun getHorarios(
        @Query("id_negocio") idNegocio: Int? = null
    ): Response<List<Horario>>

    @GET("horarios/{id}")
    suspend fun getHorario(@Path("id") id: Int): Response<Horario>

    @POST("horarios")
    suspend fun createHorario(@Body horario: HorarioCreate): Response<ApiResponse<Horario>>

    @POST("horarios/lote")
    suspend fun createHorariosLote(@Body horarios: List<HorarioCreate>): Response<ApiResponse<List<Horario>>>

    @GET("negocios/{id}/horarios")
    suspend fun getHorariosByNegocio(@Path("id") idNegocio: Int): Response<ApiResponse<List<Horario>>>

    @PUT("horarios/{id}")
    suspend fun updateHorario(
        @Path("id") id: Int,
        @Body body: HorarioUpdate
    ): Response<Horario>

    @DELETE("horarios/{id}")
    suspend fun deleteHorario(@Path("id") id: Int): Response<Unit>

    // ---------- UBICACIONES ----------
    @GET("ubicaciones")
    suspend fun getUbicaciones(): Response<ApiResponse<List<Ubicacion>>>

    @GET("ubicaciones/{id}")
    suspend fun getUbicacion(@Path("id") id: Int): Response<ApiResponse<Ubicacion>>

    @POST("ubicaciones")
    suspend fun createUbicacion(@Body body: UbicacionCreate): Response<ApiResponse<Ubicacion>>

    @PUT("ubicaciones/{id}")
    suspend fun updateUbicacion(
        @Path("id") id: Int,
        @Body body: UbicacionUpdate
    ): Response<ApiResponse<Ubicacion>>

    @DELETE("ubicaciones/{id}")
    suspend fun deleteUbicacion(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------- RESEÑAS ----------
    @GET("resenas")
    suspend fun getResenas(
        @Query("id_negocio") idNegocio: Int? = null,
        @Query("id_usuario") idUsuario: Int? = null
    ): Response<List<Resena>>

    @GET("resenas/{id}")
    suspend fun getResena(@Path("id") id: Int): Response<Resena>

    @POST("resenas")
    suspend fun createResena(@Body body: ResenaCreate): Response<Resena>

    @PUT("resenas/{id}")
    suspend fun updateResena(
        @Path("id") id: Int,
        @Body body: ResenaUpdate
    ): Response<Resena>

    @DELETE("resenas/{id}")
    suspend fun deleteResena(@Path("id") id: Int): Response<Unit>

    // ---------- IMAGENES-RESENAS ----------
    @GET("imagenes-resenas")
    suspend fun getImagenesResena(
        @Query("id_resena") idResena: Int? = null
    ): Response<List<ImagenResena>>

    @GET("imagenes-resenas/{id}")
    suspend fun getImagenResena(@Path("id") id: Int): Response<ImagenResena>

    @POST("imagenes-resenas")
    suspend fun createImagenResena(@Body body: ImagenResenaCreate): Response<ImagenResena>

    @PUT("imagenes-resenas/{id}")
    suspend fun updateImagenResena(
        @Path("id") id: Int,
        @Body body: ImagenResenaUpdate
    ): Response<ImagenResena>

    @DELETE("imagenes-resenas/{id}")
    suspend fun deleteImagenResena(@Path("id") id: Int): Response<Unit>

    // ---------- NEGOCIO-IMAGENES ----------
    @GET("negocio-imagenes")
    suspend fun getNegocioImagenes(
        @Query("id_negocio") idNegocio: Int? = null
    ): Response<List<NegocioImagen>>

    @GET("negocio-imagenes/{id}")
    suspend fun getNegocioImagen(@Path("id") id: Int): Response<NegocioImagen>


    @POST("negocio-imagenes")
    suspend fun createNegocioImagen(@Body body: NegocioImagenCreate): Response<NegocioImagen>

    @PUT("negocio-imagenes/{id}")
    suspend fun updateNegocioImagen(
        @Path("id") id: Int,
        @Body body: NegocioImagenUpdate
    ): Response<NegocioImagen>

    @DELETE("negocio-imagenes/{id}")
    suspend fun deleteNegocioImagen(@Path("id") id: Int): Response<Unit>

    // ---------- MENSAJES ----------
    @GET("mensajes")
    suspend fun getMensajes(
        @Query("id_usuario") idUsuario: Int? = null
    ): Response<List<Mensaje>>

    @GET("mensajes/{id}")
    suspend fun getMensaje(@Path("id") id: Int): Response<Mensaje>

    @POST("mensajes")
    suspend fun createMensaje(@Body body: MensajeCreate): Response<Mensaje>

    @PUT("mensajes/{id}")
    suspend fun updateMensaje(
        @Path("id") id: Int,
        @Body body: MensajeUpdate
    ): Response<Mensaje>

    @DELETE("mensajes/{id}")
    suspend fun deleteMensaje(@Path("id") id: Int): Response<Unit>

    // ---------- SERVICIOS - SUBIR IMAGEN ----------
//    @Multipart
//    @POST("servicios/{id}/imagen")
//    suspend fun uploadServiceImage(
//        @Path("id") id: Int,
//        @Part imagen: MultipartBody.Part
//    ): Response<ApiResponse<Servicio>>

    @GET("negocios/mio")
    suspend fun getMiNegocio(): Response<ApiResponse<Negocio>>

    // ---------- FILTRO DE SERVICIOS (chatbot) ----------
    @POST("filtrar-servicios")
    suspend fun filterServicios(@Body body: com.tesis.appmovil.data.remote.ServicioFilterRequest): Response<com.tesis.appmovil.data.remote.ApiResponse<List<com.tesis.appmovil.models.Servicio>>>
}
