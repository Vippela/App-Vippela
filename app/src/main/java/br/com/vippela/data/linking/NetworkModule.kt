package br.com.vippela.data.linking

import br.com.vippela.BuildConfig
import br.com.vippela.data.auth.AuthApi
import java.net.URI
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    fun validUrl(value: String): Boolean {
        val url = runCatching { URI(value) }.getOrNull() ?: return false
        return (url.scheme == "https" || (BuildConfig.DEBUG && url.scheme == "http")) &&
            !url.host.isNullOrBlank() &&
            url.userInfo == null &&
            url.query == null &&
            url.fragment == null
    }

    fun api(url: String): DeviceLinkApi {
        require(validUrl(url)) { "Configure o endereço do servidor de vínculo." }
        return retrofit(url, null).create(DeviceLinkApi::class.java)
    }

    /**
     * Mesma instalação, outro recurso: cadastro e login. O interceptor
     * anexa o token de sessão em toda chamada, como o backend espera.
     */
    fun auth(url: String, token: () -> String?): AuthApi {
        require(validUrl(url)) { "Configure o endereço do servidor." }
        return retrofit(url, token).create(AuthApi::class.java)
    }

    private fun retrofit(url: String, token: (() -> String?)?): Retrofit {
        val client =
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .callTimeout(15, TimeUnit.SECONDS)
        if (token != null) {
            client.addInterceptor { chain ->
                val atual = token()
                val requisicao =
                    if (atual.isNullOrBlank()) chain.request()
                    else chain.request().newBuilder().header("Authorization", "Bearer $atual").build()
                chain.proceed(requisicao)
            }
        }
        return Retrofit.Builder()
            .baseUrl(url.trimEnd('/') + "/")
            .client(client.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
