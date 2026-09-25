package br.com.vippela.data.linking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // Emulador: 10.0.2.2 aponta pro localhost da sua máquina.
    // Celular físico: troca pelo IP da sua máquina na mesma rede.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val deviceLinkApi: DeviceLinkApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeviceLinkApi::class.java)
    }
}