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
            store.salvar(SessaoResponse("user", "Teste", "test@example.test", "FAMILIAR", "test-token", Instant.now().plusSeconds(3600).toString()), "http://127.0.0.1:${server.address.port}")
            AuthRepository({ "http://127.0.0.1:${server.address.port}" }, store).sair()
            assertEquals("Bearer test-token", received.get())
            assertNull(store.atual())
        } finally { store.limpar(); server.stop(0) }
    }

    @Test fun changingServerNeverSendsThePreviousToken() = runBlocking {
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
            store.salvar(session(), "https://original.example")
            val repository = AuthRepository({ "http://127.0.0.1:${server.address.port}" }, store)
            assertNull(repository.restaurar())
            repository.sair()
            assertNull(received.get())
        } finally { store.limpar(); server.stop(0) }
    }

    @Test fun offlineRestorationKeepsValidSessionButUnauthorizedClearsIt() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/auth/me") { exchange -> exchange.sendResponseHeaders(401, -1); exchange.close() }
        val base = "http://127.0.0.1:${server.address.port}"
        val store = SessionStore(RuntimeEnvironment.getApplication())
        try {
            store.salvar(session(), base)
            server.start()
            val repository = AuthRepository({ base }, store)
            assertNull(repository.restaurar())
            assertNull(store.atual())
            server.stop(0)
            store.salvar(session(), base)
            assertEquals("test-token", repository.restaurar()?.token)
            assertNotNull(store.atual(base))
        } finally { store.limpar(); server.stop(0) }
    }

    private fun session() = SessaoResponse("user", "Teste", "test@example.test", "FAMILIAR", "test-token", Instant.now().plusSeconds(3600).toString())

    @Test fun registrationSendsBirthDateToTheApi() = runBlocking {
        val received = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/auth/register") { exchange ->
            received.set(exchange.requestBody.reader().readText())
            val bytes = com.google.gson.Gson().toJson(session()).toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(201, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val store = SessionStore(RuntimeEnvironment.getApplication())
        try {
            AuthRepository({ "http://127.0.0.1:${server.address.port}" }, store)
                .cadastrar("Teste", "test@example.test", "senha123", br.com.vippela.data.Role.FAMILIAR, "2000-02-29")
            assertEquals("2000-02-29", com.google.gson.JsonParser.parseString(received.get()).asJsonObject["dataNascimento"].asString)
        } finally { store.limpar(); server.stop(0) }
    }
}
