package br.com.vippela

import br.com.vippela.data.auth.formatBirthDateInput
import br.com.vippela.data.auth.parseBirthDate
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class BirthDateTest {
    @Test fun validatesCalendarDatesAndRejectsFutureBirthdays() {
        val today = LocalDate.of(2026, 10, 26)
        assertEquals("2000-02-29", parseBirthDate("29/02/2000", today)?.toString())
        assertEquals(today, parseBirthDate("26/10/2026", today))
        listOf("", "29/02/2001", "31/04/2000", "27/10/2026", "01/13/2000", "01012000", "01/01/0000")
            .forEach { assertNull(it, parseBirthDate(it, today)) }
    }

    @Test fun formatsNumericInputForTheRegistrationField() {
        assertEquals("29/02/2000", formatBirthDateInput("29022000"))
        assertEquals("29/02/2000", formatBirthDateInput("29/02/2000"))
        assertEquals("29/0", formatBirthDateInput("290"))
        assertEquals("", formatBirthDateInput(""))
    }
}
