package br.com.vippela

import br.com.vippela.data.DemoState
import br.com.vippela.data.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DemoStateTest {
    @Test
    fun familyRegistrationResetsSelectionWithoutReplacingCaregiverName() {
        val state = DemoState(FakeAuthGateway()).apply { selectedId = 3 }

        state.register("Ana", "ana@example.com", "secret123", Role.FAMILIAR)

        assertEquals(Role.FAMILIAR, state.role)
        assertEquals(1, state.selectedId)
        assertEquals("Ana", state.selected.name)
        assertEquals("Cleber", state.displayName)
        assertEquals("ana@example.com", state.email)
    }

    @Test
    fun googleFamilyLoginKeepsCaregiverIdentitySeparate() {
        val state = DemoState(FakeAuthGateway())

        state.loginWithGoogle("google-id", "Marina", "marina@example.com")
        state.completeGoogleRegistration(Role.FAMILIAR)

        assertEquals("Marina", state.selected.name)
        assertEquals("Cleber", state.displayName)
        assertNotNull(state.usuarioId)
    }

    @Test
    fun logoutClearsAuthenticationIdentityAndSelectedProfile() {
        val state =
            DemoState(FakeAuthGateway()).apply {
                loginWithGoogle("google-id", "Cleber", "cleber@example.com")
                completeGoogleRegistration(Role.RESPONSAVEL)
                selectedId = 3
            }

        state.logout()

        assertNull(state.role)
        assertNull(state.usuarioId)
        assertEquals(1, state.selectedId)
    }

    @Test
    fun registeredAccountReturnsWithSavedRoleAndChecksPassword() {
        val server = FakeAuthGateway()
        val first = DemoState(server)
        first.register("Ana", "ana@example.com", "secret123", Role.FAMILIAR)
        first.logout()

        val next = DemoState(server)
        next.login("ana@example.com", "errada")
        assertNull(next.role)
        assertEquals("E-mail ou senha incorretos.", next.authError)

        next.login(" ANA@example.com ", "secret123")
        assertEquals(Role.FAMILIAR, next.role)

        next.logout()
        next.register("Outra", "ana@example.com", "outra123", Role.RESPONSAVEL)
        assertNull(next.role)
        assertEquals("Este e-mail já está cadastrado.", next.authError)
    }

    @Test
    fun newGoogleAccountNeedsRegistrationAndReturningUidKeepsRole() {
        val server = FakeAuthGateway()
        val state = DemoState(server)

        assertTrue(!state.loginWithGoogle("uid", "Ana", "ana@example.com"))
        assertNull(state.role)

        state.completeGoogleRegistration(Role.FAMILIAR)
        assertEquals(Role.FAMILIAR, state.role)

        state.logout()
        val next = DemoState(server)
        next.loginWithGoogle("uid", "Ana", "novo-email@example.com")
        next.completeGoogleRegistration(Role.RESPONSAVEL)
        assertEquals(Role.FAMILIAR, next.role)
    }

    @Test
    fun withoutServerTheAppExplainsInsteadOfPretendingToLogIn() {
        val state = DemoState()

        state.login("ana@example.com", "secret123")

        assertNull(state.role)
        assertNotNull(state.authError)
        assertTrue(state.authError!!.isNotBlank())
    }
}
