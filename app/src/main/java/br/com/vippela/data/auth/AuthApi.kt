package br.com.vippela.data.auth

import br.com.vippela.data.auth.model.*
import retrofit2.http.*

interface AuthApi {
    @POST("auth/register")
    suspend fun cadastrar(@Body request: CadastroRequest): SessaoResponse

    @POST("auth/login")
    suspend fun entrar(@Body request: LoginRequest): SessaoResponse

    @POST("auth/google")
    suspend fun entrarComGoogle(@Body request: GoogleRequest): SessaoResponse

    @GET("auth/me")
    suspend fun eu(): SessaoResponse

    @POST("auth/logout")
    suspend fun sair()

    @GET("auth/existe")
    suspend fun existe(@Query("email") email: String): ExistenciaResponse
}
