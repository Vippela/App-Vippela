package br.com.vippela.data.linking
import br.com.vippela.data.linking.model.LinkCodeResponse
import br.com.vippela.data.linking.model.ConfirmLinkRequest
import br.com.vippela.data.linking.model.DeviceLinkResponse
import retrofit2.http.*

interface DeviceLinkApi {
    @POST("/links/generate")
    suspend fun generateLink(): LinkCodeResponse

    @POST("/links/confirm")
    suspend fun confirmLink(@Body request: ConfirmLinkRequest): DeviceLinkResponse
}