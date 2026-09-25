package br.com.vippela.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*

@Composable
fun LoginScreen(state: DemoState, enter: () -> Unit, register: () -> Unit, recover: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    Page {
        Brand()
        Text("Bem-vindo de volta!", style = MaterialTheme.typography.titleLarge)
        Text("Entre para cuidar da sua vida digital.", color = Muted)
        OutlinedTextField(
            email,
            {
                email = it
                error = false
            },
            Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = error,
        )
        OutlinedTextField(
            password,
            {
                password = it
                error = false
            },
            Modifier.fillMaxWidth(),
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton({ visible = !visible }) {
                    Icon(
                        if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        "Mostrar ou ocultar senha",
                    )
                }
            },
            isError = error,
        )
        if (error) Text("E-mail ou senha incorretos.", color = Red)
        TextButton(recover, Modifier.align(Alignment.End)) { Text("Esqueci minha senha") }
        PrimaryButton("Entrar", email.isNotBlank() && password.isNotBlank()) {
            if (!state.hasLocalAccount(email)) {
                state.registrationEmail = email.trim()
                register()
            } else if (state.login(email, password)) enter() else error = true
        }
        SecondaryButton("Criar conta", register)
        Panel {
            Text("Experimente o aplicativo", style = MaterialTheme.typography.titleMedium)
            Text("Dados fictícios • senha: vippela123", style = MaterialTheme.typography.bodySmall)
            TextButton({
                email = "responsavel@vippela.demo"
                password = "vippela123"
            }) {
                Text("Preencher conta do responsável")
            }
            TextButton({
                email = "familiar@vippela.demo"
                password = "vippela123"
            }) {
                Text("Preencher conta do familiar")
            }
        }
    }
}

@Composable
fun RegisterScreen(state: DemoState, done: () -> Unit, initialRole: Role = Role.RESPONSAVEL) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(state.registrationEmail) }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable {
        mutableIntStateOf(if (initialRole == Role.RESPONSAVEL) 0 else 1)
    }
    var error by rememberSaveable { mutableStateOf(false) }
    Page {
        Text("Vamos nos conhecer", style = MaterialTheme.typography.headlineMedium)
        Text("Escolha como você vai usar a Vippela.", color = Muted)
        Tabs(listOf("Responsável", "Familiar"), role) { role = it }
        OutlinedTextField(
            name,
            { name = it },
            Modifier.fillMaxWidth(),
            label = { Text("Nome") },
            singleLine = true,
        )
        OutlinedTextField(
            email,
            { email = it },
            Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            password,
            { password = it },
            Modifier.fillMaxWidth(),
            label = { Text("Senha (mínimo 8 caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        if (error)
            Text(
                "Use um e-mail ainda não cadastrado, nome e senha de pelo menos 8 caracteres.",
                color = Red,
            )
        PrimaryButton("Criar conta de demonstração") {
            error =
                name.trim().length < 2 ||
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                    password.length < 8
            if (!error) {
                val created =
                    state.register(
                        name,
                        email,
                        if (role == 0) Role.RESPONSAVEL else Role.FAMILIAR,
                        password,
                    )
                if (created) done() else error = true
            }
        }
        Text(
            "Cadastro de demonstração salvo somente neste aparelho.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}

@Composable
fun RecoverScreen(back: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableStateOf(false) }
    Page {
        Heading("Recuperar acesso")
        if (sent) {
            Panel {
                Icon(Icons.Outlined.MarkEmailRead, null, tint = Violet)
                Text("Solicitação simulada", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Nenhum e-mail foi enviado. Para experimentar, use uma conta de demonstração com a senha vippela123."
                )
            }
            PrimaryButton("Voltar ao login", onClick = back)
        } else {
            Text("Informe seu e-mail para continuar.")
            OutlinedTextField(
                email,
                { email = it },
                Modifier.fillMaxWidth(),
                label = { Text("E-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            PrimaryButton(
                "Continuar",
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            ) {
                sent = true
            }
        }
    }
}
