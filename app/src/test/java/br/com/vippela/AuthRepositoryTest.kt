package br.com.vippela

import br.com.vippela.data.auth.AuthRepository
import br.com.vippela.data.auth.SessionStore
import br.com.vippela.data.auth.model.SessaoResponse
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AuthRepositoryTest {
    @Test fun logoutSendsOriginalTokenAfterClearingLocalSession() = runBlocking {
        val received = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/auth/logout") { exchange ->
            received.set(exchange.requestHeaders.getFirst("Authorization"))
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
        }
        server.start()
        val store = SessionStore(RuntimeEnvironment.getApplication())
        try {
            store.salvar(SessaoResponse("user", "Teste", "test@example.test", "FAMILIAR", "test-token", Instant.now().plusSeconds(3600).toString()))
            AuthRepository({ "http://127.0.0.1:${server.address.port}" }, store).sair()
            assertEquals("Bearer test-token", received.get())
            assertNull(store.atual())
        } finally { store.limpar(); server.stop(0) }
    }
}
