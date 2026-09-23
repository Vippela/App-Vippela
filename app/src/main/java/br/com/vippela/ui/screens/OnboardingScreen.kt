package br.com.vippela.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phonelink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import br.com.vippela.R
import br.com.vippela.ui.theme.*

@Composable
fun WelcomeScreen(enter: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val titles =
        listOf(
            "Seja\nbem-vindo!",
            "Nosso\nvalor",
            "O problema",
            "A solução",
            "Pronto para\ncomeçar?",
        )
    val descriptions =
        listOf(
            "Vamos começar uma jornada digital mais saudável.",
            "“Segurança e bem-estar digital para a sua família”",
            "O excesso de tempo de tela está impactando o que mais importa.",
            "Menos tempo de tela mais tempo para o que realmente importa.",
            "Ajude sua família a construir uma relação mais saudável com a tecnologia.",
        )
    androidx.activity.compose.BackHandler(step > 0) { step-- }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(top = 24.dp, bottom = 28.dp)
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                "0${step + 1}",
                Modifier.background(Violet.copy(alpha = .25f), RoundedCornerShape(16.dp))
                    .padding(6.dp),
                style = MaterialTheme.typography.headlineLarge,
                color = Violet,
            )
            Spacer(Modifier.height(26.dp))
            Text(titles[step], style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(18.dp))
            Text(descriptions[step], color = Muted, style = MaterialTheme.typography.bodyLarge)
            Box(
                Modifier.fillMaxWidth().height(300.dp).padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (step) {
                    0 ->
                        Image(
                            painterResource(R.drawable.onboarding_welcome),
                            "Criança no balanço",
                            Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    1 ->
                        Image(
                            painterResource(R.drawable.onboarding_family),
                            "Família de mãos dadas",
                            Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    2 -> OnboardingApps()
                    3 -> ScreenTimeIllustration()
                    4 -> FriendlyPhone()
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            { if (step < 4) step++ else enter() },
            Modifier.align(Alignment.End),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (step == 4) "Começar  ›" else "Próximo  ›")
        }
        Spacer(Modifier.height(38.dp))
        StepIndicator(step, 5, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun OnboardingApps() {
    val icons =
        listOf(
            R.drawable.intro_instagram to "Instagram",
            R.drawable.intro_netflix to "Netflix",
            R.drawable.intro_notion to "Notion",
            R.drawable.intro_spotify to "Spotify",
            R.drawable.intro_telegram to "Telegram",
            R.drawable.intro_x to "X",
            R.drawable.intro_tiktok to "TikTok",
            R.drawable.intro_youtube to "YouTube",
            R.drawable.intro_shop to "Loja",
        )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        icons.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                row.forEach { (res, label) ->
                    Box(
                        Modifier.size(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF7F7F7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painterResource(res),
                            label,
                            Modifier.size(48.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenTimeIllustration() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                Modifier.size(150.dp).semantics { contentDescription = "60 por cento de redução" },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val width = 20.dp.toPx()
                    val inset = width / 2
                    val arcSize = Size(size.width - width, size.height - width)
                    drawArc(
                        Color.White,
                        0f,
                        360f,
                        false,
                        Offset(inset, inset),
                        arcSize,
                        style = Stroke(width),
                    )
                    drawArc(
                        Brush.linearGradient(listOf(Color(0xFFBA88C3), Color(0xFF6445A1))),
                        -90f,
                        216f,
                        false,
                        Offset(inset, inset),
                        arcSize,
                        style = Stroke(width),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("60%", style = MaterialTheme.typography.titleLarge)
                    Text("de redução", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Hoje:", style = MaterialTheme.typography.bodySmall)
                Text("6h 15min", color = Orange, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(14.dp))
                Text("Futuro:", style = MaterialTheme.typography.bodySmall)
                Text("2h 24min", color = Green, style = MaterialTheme.typography.bodySmall)
            }
        }
        Surface(
            Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = .55f),
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Outlined.Phonelink,
                    null,
                    Modifier.size(32.dp)
                        .background(Violet.copy(alpha = .15f), RoundedCornerShape(6.dp))
                        .padding(5.dp),
                    tint = Violet,
                )
                Text("3h a menos de tela no seu dia", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FriendlyPhone() {
    Canvas(
        Modifier.size(112.dp, 222.dp).semantics { contentDescription = "Celular azul sorrindo" }
    ) {
        val outline = Color(0xFF15121F)
        drawRoundRect(outline, cornerRadius = CornerRadius(14.dp.toPx()))
        drawRoundRect(
            Color(0xFF34BDF2),
            Offset(3.dp.toPx(), 3.dp.toPx()),
            Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
            CornerRadius(12.dp.toPx()),
        )
        drawRoundRect(
            outline,
            Offset(size.width * .35f, 2.dp.toPx()),
            Size(size.width * .3f, 5.dp.toPx()),
            CornerRadius(3.dp.toPx()),
        )
        listOf(.36f, .64f).forEach { x ->
            drawLine(
                Color.White,
                Offset(size.width * x, size.height * .33f),
                Offset(size.width * x, size.height * .46f),
                4.dp.toPx(),
                StrokeCap.Round,
            )
        }
        drawArc(
            Color.White,
            15f,
            150f,
            false,
            Offset(size.width * .28f, size.height * .45f),
            Size(size.width * .44f, size.height * .14f),
            style = Stroke(3.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
fun StepIndicator(step: Int, count: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) { i ->
            Box(
                Modifier.size(28.dp, 6.dp)
                    .background(
                        if (i == step) Muted else Muted.copy(alpha = .2f),
                        RoundedCornerShape(4.dp),
                    )
            )
        }
    }
}
