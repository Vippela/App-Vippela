package br.com.vippela.data.linking

import android.util.Log
import br.com.vippela.data.linking.model.ConfirmLinkRequest
import br.com.vippela.data.linking.model.DeviceLinkResponse
import br.com.vippela.data.linking.model.LinkCodeResponse

class DeviceLinkRepository(private val api: DeviceLinkApi) {

    suspend fun generateCode(): Result<LinkCodeResponse> =
        try {
            Result.success(api.generateLink())
        } catch (e: Exception) {
            Log.e("DeviceLinkRepository", "Erro ao gerar código", e)
            Result.failure(e)
        }

    suspend fun confirmCode(token: String, deviceId: String): Result<DeviceLinkResponse> =
        try {
            Result.success(api.confirmLink(ConfirmLinkRequest(token, deviceId)))
        } catch (e: Exception) {
            Log.e("DeviceLinkRepository", "Erro ao confirmar código", e)
            Result.failure(e)
        }
}