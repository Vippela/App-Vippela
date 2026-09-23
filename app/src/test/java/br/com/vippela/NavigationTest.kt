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
    private val state = DemoState()

    private fun start(account: String) {
        compose.setContent { VippelaTheme { VippelaApp(state) } }
        repeat(4) { compose.onNodeWithText("Próximo  ›").performClick() }
        compose.onNodeWithText("Começar  ›").performClick()
        compose.onNodeWithText("Continuar com email").performScrollTo().performClick()
        compose.onNodeWithText("E-mail").performTextInput(account)
        compose.onNodeWithText("Senha").performTextInput("vippela123")
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
        start("responsavel@vippela.demo")
        compose.onNodeWithText("Bem-vindo Cleber").assertIsDisplayed()
        screenshot("responsavel")
        compose.onAllNodesWithText("Acessar  ›")[1].performScrollTo().performClick()
        compose.onNodeWithText("Liberar aplicativo").performScrollTo().performClick()
        compose.onNodeWithText("● Liberado").assertExists()
        assertTrue(state.apps.getValue(1).first { it.name == "YouTube" }.allowed)
    }

    @Test
    fun familyCompletesLessonAndCannotManageFamily() {
        start("familiar@vippela.demo")
        compose.onNodeWithText("Olá, Marina!").assertIsDisplayed()
        screenshot("familiar")
        compose.onNodeWithText("Trilhas").performClick()
        screenshot("trilhas")
        compose.onNodeWithText("O que esperar do curso?").performScrollTo().performClick()
        compose.onNodeWithText("Conferir a origem antes de abrir").performScrollTo().performClick()
        compose.onNodeWithText("Verificar resposta").performScrollTo().performClick()
        compose.onNodeWithText("Muito bem! Aula concluída.").assertExists()
        assertTrue(state.completed[state.lessonKey(0)] == true)
        compose.onNodeWithText("Voltar às trilhas").performScrollTo().performClick()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Minha família").assertDoesNotExist()
    }

    @Test
    fun logoutClearsNavigationAndInvalidLoginStaysOnLogin() {
        start("responsavel@vippela.demo")
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Sair da conta").performScrollTo().performClick()
        compose.onNodeWithText("Bem-vindo de volta!").assertExists()
        assertNull(state.role)
        compose.onNodeWithText("E-mail").performTextInput("invalid@example.com")
        compose.onNodeWithText("Senha").performTextInput("wrongpass")
        compose.onNodeWithText("Entrar", useUnmergedTree = true).performScrollTo().performClick()
        compose
            .onNodeWithText("Use uma das contas de demonstração e a senha vippela123.")
            .assertExists()
        assertNull(state.role)
    }

    @Test
    fun settingsUpdateContactsWithoutChangingLoginAndEnableDarkMode() {
        start("responsavel@vippela.demo")
        compose.onNodeWithContentDescription("Configurações").performClick()
        compose.onNodeWithText("E-mail e telefone").performScrollTo().performClick()
        compose.onNodeWithText("E-mail de contato").performTextReplacement("novo@example.com")
        compose.onNodeWithText("Telefone").performTextInput("11987654321")
        compose.onNodeWithText("Salvar contatos").performScrollTo().performClick()
        assertEquals("novo@example.com", state.contactEmail)
        assertEquals("responsavel@vippela.demo", state.email)
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
        assertTrue(state.login("responsavel@vippela.demo", "vippela123"))
    }

    @Test
    fun familyConnectionRequiresCorrectCodeAndShowsConfirmation() {
        start("familiar@vippela.demo")
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Vínculo familiar").performScrollTo().performClick()
        compose.onNode(hasSetTextAction()).performTextInput("111111")
        compose.onNodeWithText("Conectar").performScrollTo().performClick()
        compose.onNodeWithText("Confira o código e tente novamente.").assertExists()
        assertFalse(state.linked)
        compose.onNode(hasSetTextAction()).performTextReplacement("482619")
        compose.onNodeWithText("Conectar").performScrollTo().performClick()
        compose.onNodeWithText("Você está conectado\ncom sua família!").assertExists()
        assertTrue(state.linked)
        screenshot("vinculo-confirmado")
        compose.onNodeWithText("Começar").performScrollTo().performClick()
        compose.onNodeWithText("Olá, Marina!").assertExists()
    }

    @Test
    fun accessCardsOpenLoginAndRegistrationHasSeparateCenteredChoice() {
        compose.setContent { VippelaApp(state) }
        compose.onNodeWithText("Seja\nbem-vindo!").assertExists()
        screenshot("onboarding")
        repeat(4) { step ->
            compose.onNodeWithText("Próximo  ›").performClick()
            screenshot("onboarding-${step + 2}")
        }
        compose.onNodeWithText("Começar  ›").performClick()
        compose.onNodeWithText("Acessar como").assertExists()
        screenshot("acesso")
        compose.onNodeWithText("Entrar com Google").performScrollTo().performClick()
        compose.onNode(hasText("Responsável") and hasAnyAncestor(isDialog())).performClick()
        compose
            .onNodeWithText(
                "O login Google precisa ser ativado pela equipe do aplicativo. Por enquanto, entre com e-mail."
            )
            .assertExists()
        assertNull(state.role)
        compose.onNodeWithText("Familiar", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("Acessar como familiar").assertExists()
        compose.onNodeWithText("Senha").assertExists()
        compose.onNodeWithText("Criar conta").performScrollTo().performClick()
        compose.onNodeWithText("Cadastrar-se como").assertExists()
        compose.onNodeWithText("Entrar com Google").assertDoesNotExist()
        compose.onNodeWithText("Continuar com email").assertDoesNotExist()
        screenshot("escolha-cadastro")
        compose.onNodeWithText("Familiar", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Familiar").assertIsSelected()
    }
}
