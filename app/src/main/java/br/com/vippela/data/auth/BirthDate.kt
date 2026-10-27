package br.com.vippela.data.auth

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val birthDateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

fun parseBirthDate(value: String, today: LocalDate = LocalDate.now()): LocalDate? =
    runCatching { LocalDate.parse(value, birthDateFormat) }.getOrNull()
        ?.takeIf { it.year >= 1 && !it.isAfter(today) }

fun formatBirthDateInput(value: String): String {
    val digits = value.filter { it in '0'..'9' }.take(8)
    return listOf(digits.take(2), digits.drop(2).take(2), digits.drop(4))
        .filter { it.isNotEmpty() }.joinToString("/")
}
