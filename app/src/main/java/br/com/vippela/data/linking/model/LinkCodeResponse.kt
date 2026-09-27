package br.com.vippela.data.linking.model

data class LinkCodeResponse(
    val id: String,
    val token: String,
    val expiresAt: String,
    val memberKey: String,
)
