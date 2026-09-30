package br.com.vippela.data.auth.model

data class CadastroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val tipoConta: String,
)

data class LoginRequest(val email: String, val senha: String)

data class GoogleRequest(val idToken: String, val tipoConta: String, val nome: String? = null)

data class SessaoResponse(
    val id: String,
    val nome: String,
    val email: String,
    val tipoConta: String,
    val token: String? = null,
    val expiraEm: String? = null,
)

data class ExistenciaResponse(val existe: Boolean)

data class ErroResponse(val error: String? = null)
