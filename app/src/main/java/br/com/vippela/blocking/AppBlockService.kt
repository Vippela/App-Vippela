package br.com.vippela.blocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import br.com.vippela.data.linking.DeviceLinkRepository
import br.com.vippela.data.linking.LinkStore
import kotlinx.coroutines.*

class AppBlockService : AccessibilityService() {
    companion object {
        @Volatile
        var connected = false
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var store: LinkStore
    private var foreground: String? = null
    private var lastBlock = -1000L
    private var eligible = emptySet<String>()
    private var eligibilityTime = -60_000L
    private var syncJob: Job? = null
    private lateinit var overlay: BlockedAppOverlay
    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (::overlay.isInitialized) overlay.dismiss() }
    }
    override fun onCreate() {
        super.onCreate()
        overlay = BlockedAppOverlay(this) { performGlobalAction(GLOBAL_ACTION_HOME) }
        registerReceiver(screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onServiceConnected() {
        store = LinkStore(this)
        connected = true
        syncJob?.cancel()
        syncJob =
            scope.launch {
                while (isActive) {
                    val account = store.activeScope
                    val visible = overlay.packageName
                    val visibleDomain = overlay.domain
                    val policy = account?.let(store::cached)
                    if (visible != null && (policy == null ||
                        (visibleDomain == null && visible !in policy.blockedPackages) ||
                        (visibleDomain != null && !shouldBlockDomain(visibleDomain, policy.blockedDomains.orEmpty())))) overlay.dismiss()
                    if (account != null && store.configured) {
                        try {
                            DeviceLinkRepository(store, account).sync()
                            blockIfNeeded(foreground)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            /* Mantém a última regra recebida quando está offline. */
                        }
                    }
                    delay(5000)
                }
            }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::store.isInitialized || event == null) return
        foreground = event.packageName?.toString()
        if (!blockIfNeeded(foreground)) blockSiteIfNeeded(foreground)
    }

    private fun blockIfNeeded(packageName: String?): Boolean {
        val account = store.activeScope ?: return false
        val policy = store.cached(account) ?: return false
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - eligibilityTime > 30_000) {
            eligible = store.installedApps().map { it.packageName }.toSet()
            eligibilityTime = now
        }
        if (
            policy.status != "active" || !shouldBlock(packageName, policy.blockedPackages, eligible)
        )
            return false
        if (now - lastBlock < 800) return true
        if (performGlobalAction(GLOBAL_ACTION_HOME)) {
            lastBlock = now
            overlay.show(packageName!!)
            return true
        }
        return false
    }

    private fun blockSiteIfNeeded(packageName: String?) {
        val ids = browserAddressBarIds[packageName] ?: return
        val account = store.activeScope ?: return
        val policy = store.cached(account) ?: return
        if (policy.status != "active" || policy.blockedDomains.isNullOrEmpty()) return
        val root = rootInActiveWindow ?: return
        val address = findAddress(root, ids) ?: return
        val domain = normalizeDomain(address) ?: return
        if (!shouldBlockDomain(domain, policy.blockedDomains.orEmpty())) return
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastBlock < 800) return
        if (performGlobalAction(GLOBAL_ACTION_HOME)) {
            lastBlock = now
            overlay.show(packageName!!, domain)
        }
    }

    override fun onInterrupt() { if (::overlay.isInitialized) overlay.dismiss() }

    override fun onDestroy() {
        connected = false
        scope.cancel()
        overlay.dismiss()
        unregisterReceiver(screenOff)
        super.onDestroy()
    }
}

private fun findAddress(root: AccessibilityNodeInfo, ids: List<String>): String? =
    ids.asSequence()
        .flatMap { id -> runCatching { root.findAccessibilityNodeInfosByViewId(id) }.getOrDefault(emptyList()).asSequence() }
        .mapNotNull { it.text?.toString() }
        .firstOrNull { normalizeDomain(it) != null }

internal fun shouldBlock(packageName: String?, blocked: Set<String>, eligible: Set<String>) =
    packageName != null &&
        packageName != "br.com.vippela" &&
        packageName in eligible &&
        packageName in blocked
