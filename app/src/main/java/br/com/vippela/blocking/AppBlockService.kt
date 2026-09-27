package br.com.vippela.blocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
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

    override fun onServiceConnected() {
        store = LinkStore(this)
        connected = true
        syncJob?.cancel()
        syncJob =
            scope.launch {
                while (isActive) {
                    val account = store.activeScope
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
        if (
            !::store.isInitialized ||
                event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        )
            return
        foreground = event.packageName?.toString()
        blockIfNeeded(foreground)
    }

    private fun blockIfNeeded(packageName: String?) {
        val account = store.activeScope ?: return
        val policy = store.cached(account) ?: return
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - eligibilityTime > 30_000) {
            eligible = store.installedApps().map { it.packageName }.toSet()
            eligibilityTime = now
        }
        if (
            policy.status != "active" || !shouldBlock(packageName, policy.blockedPackages, eligible)
        )
            return
        if (now - lastBlock < 800) return
        if (performGlobalAction(GLOBAL_ACTION_HOME)) {
            lastBlock = now
            Toast.makeText(
                    this,
                    "Aplicativo bloqueado pelo responsável. Converse com sua família para liberar.",
                    Toast.LENGTH_LONG,
                )
                .show()
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        connected = false
        scope.cancel()
        super.onDestroy()
    }
}

internal fun shouldBlock(packageName: String?, blocked: Set<String>, eligible: Set<String>) =
    packageName != null &&
        packageName != "br.com.vippela" &&
        packageName in eligible &&
        packageName in blocked
