package br.com.vippela.data.linking

import br.com.vippela.BuildConfig
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
        return Retrofit.Builder()
            .baseUrl(url.trimEnd('/') + "/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .callTimeout(15, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeviceLinkApi::class.java)
    }
}
