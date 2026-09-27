package br.com.vippela.data.linking

import br.com.vippela.data.linking.model.*
import retrofit2.http.*

interface DeviceLinkApi {
    @POST("links/generate")
    suspend fun generateLink(
        @Header("X-Owner-Key") key: String,
        @Body request: GenerateLinkRequest,
    ): LinkCodeResponse

    @POST("links/confirm")
    suspend fun confirmLink(
        @Header("X-Device-Key") key: String,
        @Body request: ConfirmLinkRequest,
    ): DeviceLinkResponse

    @GET("links") suspend fun links(@Header("X-Owner-Key") key: String): List<DeviceLinkResponse>

    @GET("links/{id}")
    suspend fun status(
        @Path("id") id: String,
        @Header("X-Owner-Key") key: String,
    ): DeviceLinkResponse

    @PUT("links/{id}/rule")
    suspend fun rule(
        @Path("id") id: String,
        @Header("X-Owner-Key") key: String,
        @Body request: RuleRequest,
    ): DeviceLinkResponse

    @POST("links/{id}/sync")
    suspend fun sync(
        @Path("id") id: String,
        @Header("X-Device-Key") key: String,
        @Body request: SyncRequest,
    ): DeviceLinkResponse
}
