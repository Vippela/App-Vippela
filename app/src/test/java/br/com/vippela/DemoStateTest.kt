package br.com.vippela

import br.com.vippela.data.DemoState
import br.com.vippela.data.Role
import org.junit.Assert.*
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

        state.loginWithGoogle("google-id", "Marina", "marina@example.com")
        state.completeGoogleRegistration(Role.FAMILIAR)

        assertEquals("Marina", state.selected.name)
        assertEquals("Cleber", state.displayName)
        assertEquals("google-id", state.googleUid)
    }

    @Test
    fun logoutClearsAuthenticationIdentityAndSelectedProfile() {
        val state = DemoState().apply {
            loginWithGoogle("google-id", "Cleber", "cleber@example.com")
            completeGoogleRegistration(Role.RESPONSAVEL)
            selectedId = 3
        }

        state.logout()

        assertNull(state.role)
        assertNull(state.googleUid)
        assertEquals(1, state.selectedId)
    }
    @Test fun registeredAccountReturnsWithSavedRoleAndChecksPassword() {
        val accounts = br.com.vippela.data.LocalAccounts()
        val first = DemoState(accounts)
        assertTrue(first.register("Ana", "ana@example.com", Role.FAMILIAR, "secret123"))
        first.logout()
        val next = DemoState(accounts)
        assertFalse(next.login("ana@example.com", "incorrect"))
        assertNull(next.role)
        assertTrue(next.login(" ANA@example.com ", "secret123"))
        assertEquals(Role.FAMILIAR, next.role)
        assertFalse(next.register("Other", "ana@example.com", Role.RESPONSAVEL, "other123"))
    }
    @Test fun newGoogleAccountNeedsRegistrationAndReturningUidKeepsRole() {
        val accounts = br.com.vippela.data.LocalAccounts()
        val state = DemoState(accounts)
        assertFalse(state.loginWithGoogle("uid", "Ana", "ana@example.com"))
        assertNull(state.role)
        state.completeGoogleRegistration(Role.FAMILIAR)
        state.logout()
        val next = DemoState(accounts)
        assertTrue(next.loginWithGoogle("uid", "Ana", "new-email@example.com"))
        assertEquals(Role.FAMILIAR, next.role)
        assertFalse(DemoState(accounts).loginWithGoogle("another-uid", "Ana", "ana@example.com"))
    }

}
