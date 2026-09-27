package br.com.vippela.data.linking

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import br.com.vippela.data.linking.model.*
import com.google.gson.Gson
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class LinkStore(context: Context) {
    companion object {
        private val lock = Any()
    }

    val context = context.applicationContext
    private val prefs = this.context.getSharedPreferences("family_links", Context.MODE_PRIVATE)
    private val gson = Gson()
    var server: String
        get() = prefs.getString("server", "").orEmpty()
        set(value) {
            prefs
                .edit()
                .putString("server", value.trim().trimEnd('/') + "/")
                .remove("active")
                .commit()
        }

    val configured
        get() = NetworkModule.validUrl(server)

    fun scope(account: String) = hash(server + "|" + account)

    private fun hash(value: String) =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") {
            "%02x".format(it)
        }

    fun key(scope: String): String =
        synchronized(lock) {
            val previous = prefs.getString("$scope.key", null)
            if (previous != null) return@synchronized previous
            val key =
                Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) })
            prefs.edit().putString("$scope.key", key).commit()
            key
        }

    fun deviceId(scope: String) = hash(key(scope))

    fun activate(scope: String?) {
        prefs.edit().putString("active", scope).commit()
    }

    val activeScope
        get() = prefs.getString("active", null)

    fun cached(scope: String): DeviceLinkResponse? =
        runCatching {
                gson.fromJson(prefs.getString("$scope.link", null), DeviceLinkResponse::class.java)
            }
            .getOrNull()

    fun save(scope: String, link: DeviceLinkResponse) =
        synchronized(lock) {
            val previous = cached(scope)
            if (previous == null || previous.id != link.id || previous.revision <= link.revision) {
                val oldValue = prefs.getString("$scope.link", null)
                if (!prefs.edit().putString("$scope.link", gson.toJson(link)).commit()) {
                    prefs.edit().putString("$scope.link", oldValue).apply()
                    error("Não foi possível salvar as regras neste aparelho")
                }
            }
        }

    fun pending(scope: String): LinkCodeResponse? =
        runCatching {
                gson.fromJson(prefs.getString("$scope.pending", null), LinkCodeResponse::class.java)
            }
            .getOrNull()

    fun savePending(scope: String, link: LinkCodeResponse?) {
        prefs.edit().putString("$scope.pending", link?.let(gson::toJson)).commit()
    }

    fun installedApps(): List<LinkedApp> {
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val essential =
            pm.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                    0,
                )
                .map { it.activityInfo.packageName }
                .toSet() +
                setOfNotNull(
                    context.packageName,
                    "com.android.settings",
                    "com.android.systemui",
                    android.provider.Telephony.Sms.getDefaultSmsPackage(context),
                    (context.getSystemService(Context.TELECOM_SERVICE)
                            as? android.telecom.TelecomManager)
                        ?.defaultDialerPackage,
                )
        val optionalSystemApps =
            setOf(
                "com.google.android.youtube",
                "com.instagram.android",
                "com.zhiliaoapp.musically",
                "org.khanacademy.android",
            )
        return pm.queryIntentActivities(launcher, 0)
            .filter {
                val app = it.activityInfo.applicationInfo
                app.packageName !in essential &&
                    (app.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                        app.packageName in optionalSystemApps)
            }
            .map { LinkedApp(it.activityInfo.packageName, it.loadLabel(pm).toString().take(100)) }
            .distinctBy { it.packageName }
            .sortedBy { it.label }
            .take(500)
    }
}
