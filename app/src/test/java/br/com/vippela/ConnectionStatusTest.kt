package br.com.vippela

import br.com.vippela.data.linking.model.DeviceLinkResponse
import br.com.vippela.ui.components.recentContact
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class ConnectionStatusTest {
    @Test fun onlyRecentActiveLinksCountAsOnline() {
        val now = Instant.parse("2026-10-26T12:00:00Z")
        val link = DeviceLinkResponse("id", "1", "Ana", "Tutor", "device", "active", 0, 0,
            true, now.minusSeconds(5).toString(), emptyList(), emptySet())
        assertTrue(recentContact(link, now))
        assertFalse(recentContact(null, now))
        assertFalse(recentContact(link.copy(lastSeenAt = null), now))
        assertFalse(recentContact(link.copy(lastSeenAt = "inválido"), now))
        assertFalse(recentContact(link.copy(lastSeenAt = now.minusSeconds(21).toString()), now))
        assertFalse(recentContact(link.copy(lastSeenAt = now.plusSeconds(60).toString()), now))
        assertFalse(recentContact(link.copy(status = "revoked"), now))
    }
}
