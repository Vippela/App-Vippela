package com.example.vippela.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vippela.R
import com.example.vippela.ui.animation.VippelaMotion
import com.example.vippela.ui.animation.onboardingTransition
import com.example.vippela.ui.animation.rememberAnimationsEnabled
import com.example.vippela.ui.theme.VippelaTheme
import kotlinx.coroutines.delay

internal const val SPLASH = 0
internal const val WELCOME = 1
internal const val VALUE = 2
internal const val PROBLEM = 3
internal const val SOLUTION = 4
internal const val READY = 5
internal const val CONNECT = 6
internal const val CONNECTED = 7
internal fun hasAmbientArtwork(page: Int): Boolean = page != PROBLEM && page != SOLUTION

enum class OnboardingAudience { FAMILIAR, RESPONSAVEL }
private val DeepPurple = Color(0xFF21006B)
private val Orange = Color(0xFFFF7627)
private val Cream = Color(0xFFFFF8F2)
private val Ink = Color(0xFF241B3B)
private val Muted = Color(0xFF746E82)
private data class Page(val eyebrow: String, val title: String, val body: String, val icon: ImageVector, val accent: Color)

@Composable
fun VippelaOnboarding(audience: OnboardingAudience = OnboardingAudience.FAMILIAR, onFinished: () -> Unit = {}) {
    var screen by rememberSaveable { mutableIntStateOf(SPLASH) }
    val animated = rememberAnimationsEnabled()
    val pages = remember(audience) { onboardingPages(audience) }
    LaunchedEffect(screen) { if (screen == SPLASH) { delay(VippelaMotion.SplashDelayMillis); screen = WELCOME } }
    AnimatedContent(targetState = screen, transitionSpec = {
        onboardingTransition(VippelaMotion.duration(animated, VippelaMotion.OnboardingSlideMillis), VippelaMotion.duration(animated, VippelaMotion.OnboardingFadeInMillis), VippelaMotion.duration(animated, VippelaMotion.OnboardingFadeOutMillis))
    }, label = "onboarding") { current ->
        if (current == SPLASH) Splash(audience, animated) else {
            val index = (current - WELCOME).coerceIn(pages.indices)
            OnboardingPage(pages[index], index, pages.size, audience, onFinished) {
                if (index == pages.lastIndex) onFinished() else screen++
            }
        }
    }
}

private fun onboardingPages(audience: OnboardingAudience) = if (audience == OnboardingAudience.RESPONSAVEL) listOf(
    Page("BEM-VINDO AO VIPPELA", "Cuidar também é acompanhar", "Tenha uma visão clara da rotina digital da sua família, sem perder a proximidade.", Icons.Rounded.Groups, Orange),
    Page("ROTINA SAUDÁVEL", "Equilíbrio que cabe no dia a dia", "Acompanhe o tempo de tela e transforme hábitos em combinados leves e possíveis.", Icons.Rounded.AutoGraph, Color(0xFF7257D5)),
    Page("PROTEÇÃO COM RESPEITO", "Mais segurança, mais confiança", "Receba informações importantes e ajude sua família a navegar com tranquilidade.", Icons.Rounded.Security, Color(0xFF2AA876)),
    Page("TUDO PRONTO", "Comece uma nova jornada", "Crie seu perfil de responsável e conecte quem você ama ao Vippela.", Icons.Rounded.Favorite, Orange),
) else listOf(
    Page("OLÁ, FAMÍLIA!", "Seu tempo pode valer muito mais", "O Vippela ajuda você a encontrar equilíbrio e aproveitar melhor cada momento.", Icons.Rounded.PhoneAndroid, Orange),
    Page("PEQUENOS PASSOS", "Hábitos leves, todos os dias", "Complete trilhas, descubra novas atividades e acompanhe sua evolução.", Icons.Rounded.AutoGraph, Color(0xFF7257D5)),
    Page("JUNTOS É MELHOR", "Conecte-se com quem cuida de você", "Compartilhe seu progresso e construa uma rotina digital mais saudável em família.", Icons.Rounded.Groups, Color(0xFF2AA876)),
    Page("VAMOS COMEÇAR?", "Uma jornada feita para você", "Entre no Vippela e transforme seu tempo de tela em experiências que fazem bem.", Icons.Rounded.Favorite, Orange),
)

@Composable
private fun Splash(audience: OnboardingAudience, animated: Boolean) {
    val motion = rememberInfiniteTransition(label = "logo-motion")
    val offset by motion.animateFloat(if (animated) -5f else 0f, if (animated) 5f else 0f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "logo-float")
    Box(Modifier.fillMaxSize().background(DeepPurple), contentAlignment = Alignment.Center) {
        Box(Modifier.size(260.dp).background(Color.White.copy(alpha = .055f), CircleShape))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.launcher_logo), "Vippela", Modifier.size(148.dp).graphicsLayer { translationY = offset.dp.toPx() })
            Spacer(Modifier.height(22.dp)); Text("VIPPELA", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
            Text(if (audience == OnboardingAudience.RESPONSAVEL) "PARA RESPONSÁVEIS" else "PARA FAMÍLIAS", color = Color.White.copy(alpha = .72f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun OnboardingPage(page: Page, index: Int, total: Int, audience: OnboardingAudience, onSkip: () -> Unit, onNext: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Cream)) {
        Box(Modifier.size(290.dp).offset(x = 210.dp, y = (-100).dp).background(page.accent.copy(alpha = .10f), CircleShape))
        Column(Modifier.fillMaxSize().padding(horizontal = 26.dp).padding(top = 22.dp, bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.launcher_logo), "Vippela", Modifier.size(52.dp))
                Text(if (audience == OnboardingAudience.RESPONSAVEL) "RESPONSÁVEL" else "FAMILIAR", color = DeepPurple, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 8.dp))
                Spacer(Modifier.weight(1f)); if (index < total - 1) TextButton(onClick = onSkip) { Text("Pular", color = Muted, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.weight(.7f)); Illustration(page.icon, page.accent, index); Spacer(Modifier.height(42.dp))
            Text(page.eyebrow, color = page.accent, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(12.dp)); Text(page.title, color = Ink, fontSize = 32.sp, lineHeight = 37.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp)); Text(page.body, color = Muted, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(Modifier.weight(1f)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { repeat(total) { dot -> Box(Modifier.size(if (dot == index) 26.dp else 8.dp, 8.dp).clip(CircleShape).background(if (dot == index) page.accent else Color(0xFFD8D3DF))) } }
            Spacer(Modifier.height(24.dp)); Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepPurple), elevation = ButtonDefaults.buttonElevation(5.dp)) {
                Text(if (index == total - 1) "Começar" else "Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.size(10.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(20.dp))
            }
        }
    }
}

@Composable private fun Illustration(icon: ImageVector, accent: Color, index: Int) {
    Box(Modifier.size(226.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(216.dp).background(accent.copy(alpha = .09f), CircleShape))
        Box(Modifier.size(166.dp).background(Color.White, RoundedCornerShape(48.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(88.dp)) }
        Box(Modifier.align(Alignment.TopStart).offset(x = 10.dp, y = 24.dp).size(42.dp).background(Orange, CircleShape), contentAlignment = Alignment.Center) { Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Black) }
        Box(Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-18).dp).size(28.dp).background(DeepPurple, CircleShape))
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable private fun PreviewOnboarding() { VippelaTheme { VippelaOnboarding(OnboardingAudience.RESPONSAVEL) } }
