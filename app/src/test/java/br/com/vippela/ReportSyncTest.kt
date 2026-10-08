package br.com.vippela

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import br.com.vippela.data.linking.*
import br.com.vippela.data.linking.model.*
import com.google.gson.Gson
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReportSyncTest {
    @Test fun permissionGrantRefreshesImmediatelyAndFailedUploadKeepsLocalStatistics() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val ops = shadowOf(context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        fun permission(mode: Int) = ops.setMode(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName, mode)
        val gson = Gson()
        val code = AtomicInteger(200)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val policy = DeviceLinkResponse("test", "1", "Ana", "Tutor", "device", "active", 0, 0, false, null, emptyList(), emptySet())
        server.createContext("/links/test/sync") { exchange ->
            exchange.requestBody.close()
            val bytes = gson.toJson(policy).toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.createContext("/links/test/report") { exchange ->
            val bytes = exchange.requestBody.readBytes()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(code.get(), bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val store = LinkStore(context)
        store.server = "http://127.0.0.1:${server.address.port}"
        val scope = store.scope("report-test")
        store.activate(scope)
        store.save(scope, policy)
        val repository = DeviceLinkRepository(store, scope)
        try {
            permission(AppOpsManager.MODE_IGNORED)
            repository.sync()
            withTimeout(5000) { while (store.cached(scope)?.report == null || store.cached(scope)?.reportError != null) delay(10) }
            assertFalse(store.cached(scope)!!.report!!.permission)
            permission(AppOpsManager.MODE_ALLOWED)
            code.set(404)
            repository.sync()
            withTimeout(5000) { while (store.cached(scope)?.report?.permission != true || store.cached(scope)?.reportError?.contains("backend") != true) delay(10) }
            assertTrue(store.cached(scope)!!.report!!.permission)
            assertTrue(store.cached(scope)!!.reportError!!.contains("Atualize"))
            code.set(200)
            repository.sync()
            withTimeout(5000) { while (store.cached(scope)?.reportError != null) delay(10) }
            assertTrue(store.cached(scope)!!.report!!.permission)
        } finally { store.activate(null); server.stop(0) }
    }
}
