package br.com.vippela.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import br.com.vippela.R

val Purple = Color(0xFF160064)
val Lavender: Color
    @Composable get() = MaterialTheme.colorScheme.background
val Muted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val Violet = Color(0xFF8047BB)
val Orange = Color(0xFFEF883C)
val Green = Color(0xFF26864A)
val Amber = Color(0xFF9C7000)
val Red = Color(0xFFC94234)
val Poppins =
    FontFamily(Font(R.font.poppins_regular), Font(R.font.poppins_semibold, FontWeight.SemiBold))
private val typography =
    Typography(
        headlineLarge =
            TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
        headlineMedium =
            TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 28.sp),
        titleLarge =
            TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
        titleMedium =
            TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
        bodyLarge =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 23.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 20.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 17.sp,
            ),
        labelLarge =
            TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        labelMedium =
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
            ),
    )

@Composable
fun VippelaTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            if (dark)
                darkColorScheme(
                    primary = Color(0xFFCAB8FF),
                    onPrimary = Purple,
                    secondary = Color(0xFFBF8FE8),
                    background = Color(0xFF171322),
                    onBackground = Color(0xFFF0EAFB),
                    surface = Color(0xFF272133),
                    onSurface = Color(0xFFF0EAFB),
                    onSurfaceVariant = Color(0xFFCAC0DA),
                    surfaceVariant = Color(0xFF393044),
                    outline = Color(0xFFADA0C0),
                )
            else
                lightColorScheme(
                    primary = Purple,
                    onPrimary = Color.White,
                    secondary = Violet,
                    background = Color(0xFFD6D6E5),
                    onBackground = Purple,
                    surface = Color.White,
                    onSurface = Purple,
                    onSurfaceVariant = Color(0xFF5C5085),
                    surfaceVariant = Color(0xFFEBE8F4),
                    outline = Color(0xFF5C5085),
                    error = Red,
                ),
        typography = typography,
        content = content,
    )
}
