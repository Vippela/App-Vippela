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
    val erro = state.authError
    Page {
        Brand()
        Text("Bem-vindo de volta!", style = MaterialTheme.typography.titleLarge)
        Text("Entre para cuidar da sua vida digital.", color = Muted)
        ServerConfiguration(state)
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
        )
        erro?.let { Text(it, color = Red) }
        TextButton(recover, Modifier.align(Alignment.End)) { Text("Esqueci minha senha") }
        PrimaryButton(
            if (state.authBusy) "Entrando…" else "Entrar",
            email.isNotBlank() && password.isNotBlank() && state.servidorPronto && !state.authBusy,
        ) {
            state.login(email, password) { if (it) enter() }
        }
        SecondaryButton("Criar conta") {
            state.registrationEmail = email.trim()
            register()
        }
        Text(
            "A conta fica guardada no servidor da sua família.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
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
    var invalido by rememberSaveable { mutableStateOf(false) }
    val erro = state.authError
    val completo =
        name.trim().length >= 2 &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 8
    Page {
        Text("Vamos nos conhecer", style = MaterialTheme.typography.headlineMedium)
        Text("Escolha como você vai usar a Vippela.", color = Muted)
        ServerConfiguration(state)
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
        if (invalido)
            Text(
                "Use um e-mail válido, nome e senha de pelo menos 8 caracteres.",
                color = Red,
            )
        erro?.let { Text(it, color = Red) }
        PrimaryButton(
            if (state.authBusy) "Criando conta…" else "Criar conta",
            completo && state.servidorPronto && !state.authBusy,
        ) {
            invalido = !completo
            if (completo) {
                state.register(
                    name,
                    email,
                    password,
                    if (role == 0) Role.RESPONSAVEL else Role.FAMILIAR,
                ) { if (it) done() }
            }
        }
        Text(
            "Sua conta é criada no servidor configurado acima.",
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
                Text("Recuperação em breve", style = MaterialTheme.typography.titleLarge)
                Text(
                    "O envio de e-mails ainda não está disponível. Fale com quem administra a sua família ou crie uma nova conta."
                )
            }
            PrimaryButton("Voltar ao login", onClick = back)
        } else {
            Text("A recuperação por e-mail ainda não está disponível.")
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
