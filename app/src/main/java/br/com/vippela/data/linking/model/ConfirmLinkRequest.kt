package br.com.vippela.data.linking.model

data class ConfirmLinkRequest(
    val token: String,
    val deviceId: String
)