package br.com.vippela.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class LocalProfile(val name: String, val email: String, val role: Role)

class LocalAccounts(context: Context? = null) {
    private val preferences =
        context?.applicationContext?.getSharedPreferences("local_accounts", Context.MODE_PRIVATE)
    private val memory = mutableMapOf<String, String>()

    private fun read(key: String) = preferences?.getString(key, null) ?: memory[key]

    private fun write(key: String, value: String) {
        memory[key] = value
        preferences?.edit()?.putString(key, value)?.apply()
    }

    private fun key(address: String) = "email:${address.trim().lowercase(Locale.ROOT)}"

    private fun profile(key: String): LocalProfile? {
        val role =
            read("$key.role")?.let { runCatching { Role.valueOf(it) }.getOrNull() } ?: return null
        return LocalProfile(read("$key.name").orEmpty(), read("$key.email").orEmpty(), role)
    }

    private fun save(key: String, profile: LocalProfile) {
        write("$key.name", profile.name)
        write("$key.email", profile.email.trim().lowercase(Locale.ROOT))
        write("$key.role", profile.role.name)
    }

    fun contains(email: String) = profile(key(email)) != null

    fun register(name: String, email: String, password: String, role: Role): Boolean {
        if (contains(email)) return false
        val key = key(email)
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        write("$key.salt", Base64.getEncoder().encodeToString(salt))
        write("$key.hash", hash(password, salt))
        save(key, LocalProfile(name.trim(), email, role))
        return true
    }

    fun authenticate(email: String, password: String): LocalProfile? {
        val key = key(email)
        val salt = read("$key.salt") ?: return null
        val expected = read("$key.hash") ?: return null
        val actual = hash(password, Base64.getDecoder().decode(salt))
        return if (MessageDigest.isEqual(actual.toByteArray(), expected.toByteArray())) profile(key)
        else null
    }

    fun google(id: String) = profile("google:$id")

    fun registerGoogle(id: String, name: String, email: String, role: Role) {
        if (google(id) == null) save("google:$id", LocalProfile(name, email, role))
    }

    private fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return try {
            Base64.getEncoder()
                .encodeToString(
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .encoded
                )
        } finally {
            spec.clearPassword()
        }
    }
}
