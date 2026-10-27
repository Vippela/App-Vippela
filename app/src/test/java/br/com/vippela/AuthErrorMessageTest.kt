package br.com.vippela

import br.com.vippela.data.auth.AuthRepository
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthErrorMessageTest {
    private fun message(status: Int, body: String) = AuthRepository.mensagemDeErro(
        HttpException(Response.error<Any>(status, ResponseBody.create(MediaType.parse("application/json"), body)))
    )

    @Test fun validationHighlightsTheFieldWithoutDisplayingUntrustedServerDetails() {
        val message = message(400, """{"dataNascimento":"private SQL detail","error":"private"}""")
        assertTrue(message.contains("data de nascimento"))
        assertFalse(message.contains("private"))
        assertEquals("Confira os dados informados.", message(400, "not JSON"))
    }

    @Test fun databaseUnavailableIsDistinguishedFromAnOptionalFeature() {
        assertTrue(message(503, """{"code":"DATABASE_UNAVAILABLE"}""").contains("Cadastro e login"))
        assertEquals("Este recurso não está disponível no servidor agora.", message(503, """{"code":{}}"""))
        assertEquals("Este recurso não está disponível no servidor agora.", message(503, "{}"))
    }
}
