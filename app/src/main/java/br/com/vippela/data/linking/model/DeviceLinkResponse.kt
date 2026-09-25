package br.com.vippela.data.linking.model

data class DeviceLinkResponse(
    val id: String,
    val ownerUserId: String,
    val linkedDeviceId: String,
    val status: String
)