package br.com.vippela.blocking

import java.net.IDN
import java.net.URI
import java.util.Locale

internal val browserAddressBarIds =
    mapOf(
        "com.android.chrome" to listOf("com.android.chrome:id/url_bar"),
        "com.chrome.beta" to listOf("com.chrome.beta:id/url_bar"),
        "com.chrome.dev" to listOf("com.chrome.dev:id/url_bar"),
        "com.chrome.canary" to listOf("com.chrome.canary:id/url_bar"),
        "com.brave.browser" to listOf("com.brave.browser:id/url_bar"),
        "com.microsoft.emmx" to listOf("com.microsoft.emmx:id/url_bar"),
        "com.sec.android.app.sbrowser" to
            listOf("com.sec.android.app.sbrowser:id/location_bar_edit_text"),
        "org.mozilla.firefox" to
            listOf(
                "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
                "org.mozilla.firefox:id/mozac_browser_toolbar_edit_url_view",
            ),
    )

/** Converte o texto visível da barra de endereço em um domínio comparável. */
internal fun normalizeDomain(value: String): String? {
    val input = value.trim()
    if (input.isEmpty() || input.any(Char::isWhitespace) || input.length > 2048) return null
    val candidate = if ("://" in input) input else "https://$input"
    val host = runCatching { URI(candidate).host }.getOrNull()?.trimEnd('.') ?: return null
    val ascii = runCatching { IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES) }.getOrNull()
        ?.lowercase(Locale.ROOT)?.removePrefix("www.") ?: return null
    if (ascii.length !in 1..253 || '.' !in ascii) return null
    if (ascii.split('.').any { it.isEmpty() || it.length > 63 }) return null
    return ascii
}

internal fun shouldBlockDomain(host: String?, blockedDomains: Set<String>): Boolean {
    val normalized = host?.let(::normalizeDomain) ?: return false
    return blockedDomains.any { rule ->
        val domain = normalizeDomain(rule) ?: return@any false
        normalized == domain || normalized.endsWith(".$domain")
    }
}
