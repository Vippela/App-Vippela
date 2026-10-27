package br.com.vippela

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import br.com.vippela.data.DemoState
import br.com.vippela.ui.navigation.VippelaApp
import br.com.vippela.ui.theme.VippelaTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h873dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val state = DemoState(FakeAuthGateway())

    @Before
    fun keepUiTestsOffline() {
        compose.activity
            .getSharedPreferences("family_links", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("server", "")
            .commit()
    }

    @Test
    fun officialServerIsShownAsAutomaticBeforeLogin() {
        compose.activity
            .getSharedPreferences("family_links", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        val initial = DemoState(FakeAuthGateway())
        initial.attachLinks(br.com.vippela.data.linking.LinkStore(compose.activity))

        compose.setContent {
            VippelaTheme { br.com.vippela.ui.screens.ServerConfiguration(initial) }
        }

        compose.onNodeWithText("Servidor Vippela").assertIsDisplayed()
        compose
            .onNodeWithText(
                "Conexão automática com o servidor oficial. A primeira conexão pode levar até dois minutos."
            )
            .assertIsDisplayed()
        compose.onNodeWithText("Alterar servidor").assertIsDisplayed()
    }

    private fun start(account: String) {
        compose.setContent { VippelaTheme { VippelaApp(state) } }
        repeat(4) { compose.onNodeWithText("Próximo  ›").performClick() }
        compose.onNodeWithText("Começar  ›").performClick()
        compose.onNodeWithText("Continuar com email").performScrollTo().performClick()
        compose.onNodeWithText("E-mail").performTextInput(account)
        compose.onNodeWithText("Senha").performTextInput("senha123")
        compose.onNodeWithText("Entrar", useUnmergedTree = true).performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun screenshot(name: String) {
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        val dir = File("build/screenshots").apply { mkdirs() }
        compose.runOnUiThread {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(dir, "$name.png").outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    @Test
    fun parentApprovesRequestAndSeesUpdatedWhitelist() {
        start("cleber@example.com")
        compose.runOnIdle { state.requestApp("YouTube", "Para estudar") }
        compose.onNodeWithText("Bem-vindo Cleber").assertIsDisplayed()
        screenshot("responsavel")
        compose.onAllNodesWithText("Acessar  ›")[1].performScrollTo().performClick()
        compose.onNodeWithText("Liberar aplicativo").performScrollTo().performClick()
        compose.onNodeWithText("● Liberado").assertExists()
        assertTrue(state.apps.getValue(1).first { it.name == "YouTube" }.allowed)
    }

    @Test
    fun familyCompletesLessonAndCannotManageFamily() {
        start("marina@example.com")
        compose.onNodeWithText("Olá, Marina!").assertIsDisplayed()
        screenshot("familiar")
        compose.onNodeWithText("Trilhas").performClick()
        screenshot("trilhas")
        compose
            .onAllNodesWithText("Contas e senhas: a chave da sua casa digital")[1]
            .performScrollTo()
            .performClick()
        compose.onNodeWithText("Por que contas são roubadas").performScrollTo().performClick()
        compose.onNodeWithText("Fazer o quiz").performScrollTo().performClick()
        compose
            .onNodeWithText("Pedir dinheiro aos seus contatos e mudar sua senha")
            .performScrollTo()
            .performClick()
        compose.onNodeWithText("Conferir resposta").performScrollTo().performClick()
        compose.onNodeWithText("Próxima pergunta").performScrollTo().performClick()
        compose.onNodeWithText("Repetir a mesma senha em vários sites").performScrollTo().performClick()
        compose.onNodeWithText("Conferir resposta").performScrollTo().performClick()
        compose.onNodeWithText("Próxima pergunta").performScrollTo().performClick()
        compose
            .onNodeWithText("O código de verificação que chegou por SMS")
            .performScrollTo()
            .performClick()
        compose.onNodeWithText("Conferir resposta").performScrollTo().performClick()
        compose.onNodeWithText("Ver resultado").performScrollTo().performClick()
        compose.onNodeWithText("Muito bem!").assertExists()
        compose.onNodeWithText("Voltar à trilha").performScrollTo().performClick()
        compose.onNodeWithText("1 de 3 lições · 10 min").assertExists()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Minha família").assertDoesNotExist()
    }

    @Test
    fun logoutClearsNavigationAndInvalidLoginStaysOnLogin() {
        start("cleber@example.com")
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Sair da conta").performScrollTo().performClick()
        compose.onNodeWithText("Bem-vindo de volta!").assertExists()
        assertNull(state.role)
        compose.onNodeWithText("E-mail").performTextInput("cleber@example.com")
        compose.onNodeWithText("Senha").performTextInput("errada123")
        compose.onNodeWithText("Entrar", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("E-mail ou senha incorretos.").assertExists()
        assertNull(state.role)
    }

    @Test
    fun settingsUpdateContactsWithoutChangingLoginAndEnableDarkMode() {
        start("cleber@example.com")
        compose.onNodeWithContentDescription("Configurações").performClick()
        compose.onNodeWithText("E-mail e telefone").performScrollTo().performClick()
        compose.onNodeWithText("E-mail de contato").performTextReplacement("novo@example.com")
        compose.onNodeWithText("Telefone").performTextInput("11987654321")
        compose.onNodeWithText("Salvar contatos").performScrollTo().performClick()
        assertEquals("novo@example.com", state.contactEmail)
        assertEquals("cleber@example.com", state.email)
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Tema").performScrollTo().performClick()
        compose.onNodeWithText("Escuro").performClick()
        assertTrue(state.darkMode)
        screenshot("tema-escuro")
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithContentDescription("Voltar").performClick()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("novo@example.com").assertExists()
        compose.onNodeWithText("11987654321").assertExists()
        screenshot("perfil-escuro")
        compose.onNodeWithText("Relatório").performClick()
        screenshot("relatorio-escuro")
        state.login("cleber@example.com", "senha123")
    }

    @Test
    fun familyConnectionNeedsAServerBeforeAcceptingACode() {
        start("marina@example.com")
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Vínculo familiar").performScrollTo().performClick()
        compose.onNodeWithText("Servidor da família").assertExists()
        assertFalse(state.linked)
        compose.runOnIdle { state.confirmLinkCode("482619") }
        compose.onNodeWithText("Configure o servidor do vínculo nos dois celulares.").assertExists()
        screenshot("vinculo-servidor")
        assertFalse(state.linked)
    }

    @Test
    fun universalAccessHasNoRoleCardsAndRegistrationKeepsChoice() {
        compose.setContent { VippelaApp(state) }
        compose.onNodeWithText("Seja\nbem-vindo!").assertExists()
        screenshot("onboarding")
        repeat(4) { step ->
            compose.onNodeWithText("Próximo  ›").performClick()
            screenshot("onboarding-${step + 2}")
        }
        compose.onNodeWithText("Começar  ›").performClick()
        compose.onNodeWithText("Entrar na Vippela").assertExists()
        screenshot("acesso")
        // Google desligado por padrão: o botão some e o acesso é só por e-mail.
        compose.onNodeWithText("Entrar com Google").assertDoesNotExist()
        compose
            .onNodeWithText("O acesso por Google ainda não está disponível. Use seu e-mail e senha.")
            .assertExists()
        compose.onNodeWithText("Responsável").assertDoesNotExist()
        compose.onNodeWithText("Familiar").assertDoesNotExist()
        compose.onNodeWithText("Facebook").assertDoesNotExist()
        assertNull(state.role)
        compose.onNodeWithText("Continuar com email").performScrollTo().performClick()
        compose.onNodeWithText("Senha").assertExists()
        compose.onNodeWithText("Criar conta").performScrollTo().performClick()
        compose.onNodeWithText("Cadastrar-se como").assertExists()
        compose.onNodeWithText("Entrar com Google").assertDoesNotExist()
        compose.onNodeWithText("Continuar com email").assertDoesNotExist()
        screenshot("escolha-cadastro")
        compose.onNodeWithText("Familiar", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Familiar").assertIsSelected()
    }

    @Test
    fun serverSessionSurvivesStoreRecreationAndExpires() {
        val context = compose.activity
        val store = br.com.vippela.data.auth.SessionStore(context)
        store.limpar()
        store.salvar(
            br.com.vippela.data.auth.model.SessaoResponse(
                id = "id-cleber",
                nome = "Cleber",
                email = "cleber@example.com",
                tipoConta = "RESPONSAVEL",
                token = "token-cleber",
                expiraEm = "2099-01-01T00:00:00Z",
            )
        )

        val restored = br.com.vippela.data.auth.SessionStore(context).atual()
        assertEquals("token-cleber", restored?.token)
        assertEquals(br.com.vippela.data.Role.RESPONSAVEL, restored?.tipo)

        store.salvar(
            br.com.vippela.data.auth.model.SessaoResponse(
                id = "id-cleber",
                nome = "Cleber",
                email = "cleber@example.com",
                tipoConta = "RESPONSAVEL",
                token = "token-cleber",
                expiraEm = "2000-01-01T00:00:00Z",
            )
        )
        assertNull(br.com.vippela.data.auth.SessionStore(context).atual())
        store.limpar()
    }

    @Test fun registrationRequiresAValidBirthDateAndSubmitsIsoDate() {
        val gateway = FakeAuthGateway()
        val registration = DemoState(gateway)
        compose.setContent {
            VippelaTheme { br.com.vippela.ui.screens.RegisterScreen(registration, {}) }
        }
        compose.onNodeWithText("Nome").performTextInput("Ana")
        compose.onNodeWithText("E-mail").performTextInput("ana.nova@example.test")
        compose.onNodeWithText("Senha (mínimo 8 caracteres)").performTextInput("senha123")
        compose.onNodeWithText("Criar conta").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Data de nascimento").performScrollTo().performTextInput("31022000")
        compose.onNodeWithText("Criar conta").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Data de nascimento").performScrollTo().performTextReplacement("29022000")
        compose.onNodeWithText("Nome").performScrollTo().performTextReplacement("a".repeat(51))
        compose.onNodeWithText("Criar conta").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Nome").performScrollTo().performTextReplacement("Ana")
        compose.onNodeWithText("Senha (mínimo 8 caracteres)").performScrollTo().performTextReplacement("á".repeat(37))
        compose.onNodeWithText("Criar conta").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Senha (mínimo 8 caracteres)").performScrollTo().performTextReplacement("senha123")
        compose.onNodeWithText("Criar conta").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("2000-02-29", gateway.ultimaDataNascimento) }
    }
}
