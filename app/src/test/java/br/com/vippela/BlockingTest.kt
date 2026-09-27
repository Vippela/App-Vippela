package br.com.vippela

import android.content.Context
import br.com.vippela.blocking.shouldBlock
import br.com.vippela.data.linking.*
import br.com.vippela.data.linking.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BlockingTest {
    private val context
        get() = RuntimeEnvironment.getApplication()

    @Before
    fun clear() {
        context.getSharedPreferences("family_links", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun policy() =
        DeviceLinkResponse(
            "link-1",
            "1",
            "Ana",
            "Cleber",
            "device",
            "active",
            3,
            2,
            true,
            null,
            listOf(LinkedApp("com.instagram.android", "Instagram")),
            setOf("com.instagram.android"),
        )

    @Test
    fun onlyEligibleBlockedPackagesTriggerProtection() {
        val blocked = setOf("com.instagram.android", "com.android.settings", "br.com.vippela")
        val eligible =
            setOf("com.instagram.android", "com.google.android.youtube", "br.com.vippela")
        assertTrue(shouldBlock("com.instagram.android", blocked, eligible))
        assertFalse(shouldBlock("com.google.android.youtube", blocked, eligible))
        assertFalse(shouldBlock("com.android.settings", blocked, eligible))
        assertFalse(shouldBlock("br.com.vippela", blocked, eligible))
        assertFalse(shouldBlock(null, blocked, eligible))
    }

    @Test
    fun credentialsAndPoliciesSurviveRestartWithoutCrossingAccounts() {
        val first = LinkStore(context)
        first.server = "https://example.com"
        val ana = first.scope("ana")
        val bia = first.scope("bia")
        first.activate(ana)
        first.save(ana, policy())
        val next = LinkStore(context)
        assertEquals(first.key(ana), next.key(ana))
        assertNotEquals(next.key(ana), next.key(bia))
        assertEquals(setOf("com.instagram.android"), next.cached(ana)!!.blockedPackages)
        assertNull(next.cached(bia))
        next.activate(bia)
        assertNull(next.cached(next.activeScope!!))
        next.activate(null)
        assertNull(next.activeScope)
    }

    @Test
    fun changingServerDoesNotSendThePreviousServerCredentials() {
        val store = LinkStore(context)
        store.server = "https://one.example.com"
        val previousScope = store.scope("ana")
        store.activate(previousScope)
        val previousKey = store.key(previousScope)
        store.server = "https://two.example.com"
        assertNull(store.activeScope)
        assertNotEquals(previousKey, store.key(store.scope("ana")))
        assertFalse(NetworkModule.validUrl("ftp://example.com"))
        assertFalse(NetworkModule.validUrl("https://user:password@example.com"))
    }

    @Test
    fun remoteSyncPersistsPolicyBeforeAcknowledgingItsRevision() =
        kotlinx.coroutines.runBlocking {
            val server =
                com.sun.net.httpserver.HttpServer.create(
                    java.net.InetSocketAddress("127.0.0.1", 0),
                    0,
                )
            val requests = java.util.concurrent.CopyOnWriteArrayList<SyncRequest>()
            val keys = java.util.concurrent.CopyOnWriteArrayList<String>()
            val gson = com.google.gson.Gson()
            server.createContext("/links/link-1/sync") { exchange ->
                requests.add(gson.fromJson(exchange.requestBody.reader(), SyncRequest::class.java))
                keys.add(exchange.requestHeaders.getFirst("X-Device-Key"))
                val bytes = gson.toJson(policy()).toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
            try {
                val store = LinkStore(context)
                store.server = "http://127.0.0.1:${server.address.port}"
                val account = store.scope("ana")
                store.activate(account)
                store.save(account, policy().copy(revision = 0, blockedPackages = emptySet()))
                val repository = DeviceLinkRepository(store, account)
                repository.sync()
                assertEquals(
                    setOf("com.instagram.android"),
                    LinkStore(context).cached(account)!!.blockedPackages,
                )
                repository.sync()
                assertEquals(listOf(0L, 3L), requests.map { it.appliedRevision })
                assertTrue(requests.none { it.protectionEnabled })
                assertEquals(listOf(store.key(account), store.key(account)), keys.toList())
            } finally {
                server.stop(0)
            }
        }

    @Test
    fun oldResponseCannotReplaceANewerOfflinePolicy() {
        val store = LinkStore(context)
        val account = store.scope("ana")
        store.save(account, policy())
        store.save(account, policy().copy(revision = 1, blockedPackages = emptySet()))
        assertEquals(setOf("com.instagram.android"), store.cached(account)!!.blockedPackages)
    }

    @Test
    fun accessibilityEventReturnsHomeForBlockedAppAndStopsAfterLogout() {
        val info =
            android.content.pm.ResolveInfo().apply {
                activityInfo =
                    android.content.pm.ActivityInfo().apply {
                        packageName = "com.instagram.android"
                        name = "MainActivity"
                        applicationInfo =
                            android.content.pm.ApplicationInfo().apply {
                                packageName = "com.instagram.android"
                                flags = 0
                            }
                    }
                nonLocalizedLabel = "Instagram"
            }
        val pm = org.robolectric.Shadows.shadowOf(context.packageManager)
        pm.addResolveInfoForIntent(
            android.content
                .Intent(android.content.Intent.ACTION_MAIN)
                .addCategory(android.content.Intent.CATEGORY_LAUNCHER),
            info,
        )
        val store = LinkStore(context)
        val account = store.scope("ana")
        store.save(account, policy())
        store.activate(account)
        val controller =
            org.robolectric.Robolectric.buildService(
                    br.com.vippela.blocking.AppBlockService::class.java
                )
                .create()
        val service = controller.get()
        try {
            service.javaClass
                .getDeclaredMethod("onServiceConnected")
                .apply { isAccessible = true }
                .invoke(service)
            val event =
                android.view.accessibility.AccessibilityEvent.obtain(
                    android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                )
            event.packageName = "com.instagram.android"
            service.onAccessibilityEvent(event)
            val shadow = org.robolectric.Shadows.shadowOf(service)
            assertEquals(
                listOf(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME),
                shadow.globalActionsPerformed,
            )
            store.activate(null)
            service.onAccessibilityEvent(event)
            assertEquals(1, shadow.globalActionsPerformed.size)
        } finally {
            controller.destroy()
        }
        assertFalse(br.com.vippela.blocking.AppBlockService.connected)
    }
}
