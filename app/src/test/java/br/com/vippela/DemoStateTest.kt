package br.com.vippela

import br.com.vippela.data.DemoState
import br.com.vippela.data.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DemoStateTest {
    @Test
    fun familyRegistrationResetsSelectionWithoutReplacingCaregiverName() {
        val state = DemoState().apply { selectedId = 3 }

        state.register("Ana", "ana@example.com", Role.FAMILIAR)

        assertEquals(Role.FAMILIAR, state.role)
        assertEquals(1, state.selectedId)
        assertEquals("Ana", state.selected.name)
        assertEquals("Cleber", state.displayName)
    }

    @Test
    fun googleFamilyLoginKeepsCaregiverIdentitySeparate() {
        val state = DemoState()

        state.loginWithGoogle("google-id", "Marina", "marina@example.com", Role.FAMILIAR)

        assertEquals("Marina", state.selected.name)
        assertEquals("Cleber", state.displayName)
        assertEquals("google-id", state.googleUid)
    }

    @Test
    fun logoutClearsAuthenticationIdentityAndSelectedProfile() {
        val state = DemoState().apply {
            loginWithGoogle("google-id", "Cleber", "cleber@example.com", Role.RESPONSAVEL)
            selectedId = 3
        }

        state.logout()

        assertNull(state.role)
        assertNull(state.googleUid)
        assertEquals(1, state.selectedId)
    }
}
