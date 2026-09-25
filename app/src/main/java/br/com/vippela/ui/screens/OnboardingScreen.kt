package br.com.vippela.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import br.com.vippela.R
import br.com.vippela.ui.theme.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

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
            "Vamos começar uma\njornada digital mais\nsaudável.",
            "“Segurança e bem-estar\ndigital para a sua família”",
            "O excesso de tempo de\ntela está impactando o\nque mais importa.",
            "Menos tempo de tela\nmais tempo para o que\nrealmente importa.",
            "Ajude sua família a\nconstruir uma relação\nmais saudável com a\ntecnologia.",
        )
    val titleStyle =
        MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp, lineHeight = 44.sp)
    val bodyStyle =
        MaterialTheme.typography.bodyLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            lineHeight = 26.sp,
        )
    androidx.activity.compose.BackHandler(step > 0) { step-- }
    Box(Modifier.fillMaxSize()) {
        BeeJourneyRoute(step = step, modifier = Modifier.fillMaxWidth().height(122.dp).zIndex(1f))
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(top = 46.dp, bottom = 28.dp)
        ) {
            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (fadeIn(tween(320)) +
                        slideInHorizontally(tween(320)) { direction * it / 5 }) togetherWith
                        (fadeOut(tween(220)) +
                            slideOutHorizontally(tween(320)) { -direction * it / 5 })
                },
                label = "Páginas do onboarding",
            ) { page ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text(
                        "0${page + 1}",
                        Modifier.background(Violet.copy(alpha = .25f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 7.dp, vertical = 5.dp),
                        style = titleStyle.copy(fontSize = 36.sp, lineHeight = 42.sp),
                        color = Violet,
                    )
                    Spacer(Modifier.height(26.dp))
                    Text(titles[page], style = titleStyle)
                    Spacer(Modifier.height(18.dp))
                    Text(descriptions[page], color = Muted, style = bodyStyle)
                    Spacer(Modifier.height(if (page == 4) 34.dp else 56.dp))
                    Box(
                        Modifier.fillMaxWidth()
                            .height(if (page == 2) 320.dp else 300.dp)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (page) {
                            0 ->
                                Image(
                                    painterResource(R.drawable.onboarding_welcome),
                                    "Criança no balanço",
                                    Modifier.fillMaxSize(.9f),
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
            }
            Spacer(Modifier.height(20.dp))
            Button(
                { if (step < 4) step++ else enter() },
                Modifier.align(Alignment.End).widthIn(min = 170.dp).height(56.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (step == 4) "Começar  ›" else "Próximo  ›", fontSize = 18.sp)
            }
            Spacer(Modifier.height(38.dp))
            StepIndicator(step, 5, Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun BeeJourneyRoute(step: Int, modifier: Modifier = Modifier) {
    val progress =
        key(step) {
            val flight = rememberInfiniteTransition(label = "Trajetória da abelha")
            flight.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(4_200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "Posição da abelha",
            )
        }

    Canvas(
        modifier.clearAndSetSemantics {
            contentDescription = "Abelha percorrendo a etapa ${step + 1} do onboarding"
        }
    ) {
        val route = onboardingRoute(step)
        val routeMeasure = PathMeasure().apply { setPath(route, false) }
        val routeProgress = progress.value.coerceIn(0f, 1f)
        val distance = routeMeasure.length * routeProgress
        val pathPosition = routeMeasure.getPosition(distance)
        val tangent = routeMeasure.getTangent(distance)
        val tangentLength = sqrt(tangent.x * tangent.x + tangent.y * tangent.y).coerceAtLeast(1f)
        val flightSway = sin(routeProgress * PI.toFloat() * 12f) * 1.5.dp.toPx()
        val beePosition =
            pathPosition +
                Offset(
                    x = -tangent.y / tangentLength * flightSway,
                    y = tangent.x / tangentLength * flightSway,
                )
        val beeAngle = Math.toDegrees(atan2(tangent.y.toDouble(), tangent.x.toDouble())).toFloat()

        val visibleRoute = Path()
        routeMeasure.getSegment(
            startDistance = 0f,
            stopDistance = distance,
            destination = visibleRoute,
            startWithMoveTo = true,
        )
        drawPath(
            path = visibleRoute,
            color = Color(0xFF383641),
            style =
                Stroke(
                    width = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect =
                        PathEffect.dashPathEffect(
                            intervals = floatArrayOf(5.dp.toPx(), 7.dp.toPx()),
                            phase = -distance * .08f,
                        ),
                ),
        )
        drawJourneyBee(beePosition, beeAngle)
    }
}

private fun DrawScope.onboardingRoute(step: Int): Path {
    fun y(value: Float) = value.dp.toPx()
    val w = size.width
    val inset = 18.dp.toPx()

    return Path().apply {
        when (step) {
            0 -> {
                moveTo(inset, y(8f))
                cubicTo(w * .05f, y(36f), w * .13f, y(4f), w * .25f, y(17f))
                cubicTo(w * .37f, y(34f), w * .45f, y(2f), w * .57f, y(17f))
                cubicTo(w * .69f, y(36f), w * .78f, y(34f), w * .88f, y(31f))
                cubicTo(w * .95f, y(29f), w, y(30f), w - inset, y(27f))
            }
            1 -> {
                // A jiboia digerindo o elefante de O Pequeno Príncipe: uma base baixa
                // e uma elevação assimétrica, que também pode ser vista como um chapéu.
                moveTo(inset, y(27f))
                cubicTo(w * .04f, y(29f), w * .06f, y(58f), w * .16f, y(65f))
                cubicTo(w * .27f, y(73f), w * .38f, y(69f), w * .44f, y(63f))
                cubicTo(w * .49f, y(57f), w * .50f, y(22f), w * .57f, y(19f))
                cubicTo(w * .63f, y(16f), w * .66f, y(38f), w * .71f, y(40f))
                cubicTo(w * .76f, y(42f), w * .79f, y(38f), w * .83f, y(47f))
                cubicTo(w * .88f, y(59f), w * .88f, y(67f), w - inset, y(67f))
            }
            2 -> {
                moveTo(inset, y(52f))
                cubicTo(w * .08f, y(82f), w * .16f, y(57f), w * .26f, y(50f))
                cubicTo(w * .37f, y(44f), w * .39f, y(72f), w * .50f, y(75f))
                cubicTo(w * .63f, y(80f), w * .63f, y(59f), w * .58f, y(48f))
                cubicTo(w * .51f, y(28f), w * .60f, y(4f), w * .70f, y(13f))
                cubicTo(w * .82f, y(24f), w * .70f, y(43f), w * .72f, y(55f))
                cubicTo(w * .75f, y(79f), w * .80f, y(80f), w * .85f, y(53f))
                cubicTo(w * .90f, y(31f), w * .96f, y(39f), w - inset, y(52f))
            }
            3 -> {
                // Loop completo em torno do cartão 04 antes de a rota seguir adiante.
                moveTo(inset, y(72f))
                cubicTo(w * .03f, y(78f), w * .06f, y(76f), w * .08f, y(68f))
                cubicTo(w * .02f, y(50f), w * .04f, y(18f), w * .13f, y(8f))
                cubicTo(w * .23f, y(-2f), w * .30f, y(17f), w * .28f, y(43f))
                cubicTo(w * .27f, y(66f), w * .21f, y(82f), w * .23f, y(99f))
                cubicTo(w * .25f, y(116f), w * .34f, y(113f), w * .41f, y(103f))
                cubicTo(w * .54f, y(86f), w * .60f, y(70f), w * .70f, y(78f))
                cubicTo(w * .82f, y(91f), w * .84f, y(66f), w * .94f, y(60f))
                cubicTo(w, y(56f), w - inset, y(48f), w - inset, y(48f))
            }
            else -> {
                moveTo(inset, y(28f))
                cubicTo(w * .10f, y(32f), w * .15f, y(43f), w * .22f, y(21f))
                cubicTo(w * .31f, y(-3f), w * .37f, y(4f), w * .38f, y(16f))
                cubicTo(w * .41f, y(31f), w * .37f, y(48f), w * .45f, y(51f))
                cubicTo(w * .55f, y(57f), w * .62f, y(36f), w * .69f, y(31f))
                cubicTo(w * .77f, y(25f), w * .79f, y(44f), w * .88f, y(43f))
            }
        }
    }
}

private fun DrawScope.drawJourneyBee(center: Offset, angle: Float) {
    val outline = Color(0xFF17151E)
    val wingFill = Color(0xFFF9F9FB)
    val yellow = Color(0xFFF6C544)
    val wingSize = Size(10.dp.toPx(), 12.dp.toPx())
    val wingStroke = Stroke(1.3.dp.toPx())

    rotate(degrees = angle, pivot = center) {
        translate(left = center.x, top = center.y) {
            drawOval(
                color = wingFill,
                topLeft = Offset(-7.dp.toPx(), -10.dp.toPx()),
                size = wingSize,
            )
            drawOval(
                color = outline,
                topLeft = Offset(-7.dp.toPx(), -10.dp.toPx()),
                size = wingSize,
                style = wingStroke,
            )
            drawOval(
                color = wingFill,
                topLeft = Offset(-7.dp.toPx(), -2.dp.toPx()),
                size = wingSize,
            )
            drawOval(
                color = outline,
                topLeft = Offset(-7.dp.toPx(), -2.dp.toPx()),
                size = wingSize,
                style = wingStroke,
            )
            drawOval(
                color = yellow,
                topLeft = Offset(-8.dp.toPx(), -5.dp.toPx()),
                size = Size(17.dp.toPx(), 10.dp.toPx()),
            )
            drawLine(
                color = outline,
                start = Offset(-3.dp.toPx(), -4.dp.toPx()),
                end = Offset(-3.dp.toPx(), 4.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
            drawLine(
                color = outline,
                start = Offset(2.dp.toPx(), -4.dp.toPx()),
                end = Offset(2.dp.toPx(), 4.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
            drawOval(
                color = outline,
                topLeft = Offset(-8.dp.toPx(), -5.dp.toPx()),
                size = Size(17.dp.toPx(), 10.dp.toPx()),
                style = Stroke(1.4.dp.toPx()),
            )
            drawCircle(outline, radius = 4.dp.toPx(), center = Offset(9.dp.toPx(), 0f))
            drawLine(
                color = outline,
                start = Offset(11.dp.toPx(), -2.dp.toPx()),
                end = Offset(14.dp.toPx(), -6.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = outline,
                start = Offset(11.dp.toPx(), 2.dp.toPx()),
                end = Offset(14.dp.toPx(), 6.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
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
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        icons.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { (res, label) ->
                    Box(
                        Modifier.size(86.dp)
                            .background(Color(0xFFF7F7FA), RoundedCornerShape(8.dp)),
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
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Box(
                Modifier.size(180.dp).semantics { contentDescription = "60 por cento de redução" },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val width = 15.dp.toPx()
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
                        Brush.linearGradient(listOf(Color(0xFF9862BA), Color(0xFFFF7043))),
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
                Text("8h 31min", color = Orange, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(14.dp))
                Text("Futuro:", style = MaterialTheme.typography.bodySmall)
                Text("3h 24min", color = Green, style = MaterialTheme.typography.bodySmall)
            }
        }
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = .55f),
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FamilyMetricIcon()
                Text(
                    "O tempo de tela do seu filho\n60% menor",
                    style =
                        MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
    }
}

@Composable
private fun FamilyMetricIcon() {
    Canvas(
        Modifier.size(34.dp)
            .background(Violet.copy(alpha = .15f), RoundedCornerShape(7.dp))
            .padding(5.dp)
    ) {
        val petal = Color(0xFF9B8BBE)
        val center = Offset(size.width / 2, size.height / 2)
        val radius = 4.dp.toPx()
        listOf(
                Offset(center.x, center.y - radius),
                Offset(center.x + radius, center.y),
                Offset(center.x, center.y + radius),
                Offset(center.x - radius, center.y),
            )
            .forEach { drawCircle(petal, radius = 3.6.dp.toPx(), center = it) }
        drawCircle(Color.White, radius = 3.dp.toPx(), center = center)
        drawCircle(Violet, radius = 2.dp.toPx(), center = center)
    }
}

@Composable
private fun FriendlyPhone() {
    Canvas(
        Modifier.size(128.dp, 232.dp).semantics { contentDescription = "Celular azul sorrindo" }
    ) {
        val outline = Color(0xFF15121F)
        val phoneLeft = 8.dp.toPx()
        val phoneSize = Size(112.dp.toPx(), 222.dp.toPx())
        drawOval(
            color = Color(0xFF15121F).copy(alpha = .24f),
            topLeft = Offset(20.dp.toPx(), 222.dp.toPx()),
            size = Size(88.dp.toPx(), 8.dp.toPx()),
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(phoneLeft, 0f),
            size = phoneSize,
            cornerRadius = CornerRadius(14.dp.toPx()),
        )
        drawRoundRect(
            Color(0xFF34BDF2),
            Offset(phoneLeft + 3.dp.toPx(), 3.dp.toPx()),
            Size(phoneSize.width - 6.dp.toPx(), phoneSize.height - 6.dp.toPx()),
            CornerRadius(12.dp.toPx()),
        )
        drawRoundRect(
            outline,
            Offset(phoneLeft + phoneSize.width * .35f, 2.dp.toPx()),
            Size(phoneSize.width * .3f, 5.dp.toPx()),
            CornerRadius(3.dp.toPx()),
        )
        listOf(.36f, .64f).forEach { x ->
            drawLine(
                Color.White,
                Offset(phoneLeft + phoneSize.width * x, phoneSize.height * .33f),
                Offset(phoneLeft + phoneSize.width * x, phoneSize.height * .46f),
                4.dp.toPx(),
                StrokeCap.Round,
            )
        }
        drawArc(
            Color.White,
            15f,
            150f,
            false,
            Offset(phoneLeft + phoneSize.width * .28f, phoneSize.height * .45f),
            Size(phoneSize.width * .44f, phoneSize.height * .14f),
            style = Stroke(3.dp.toPx(), cap = StrokeCap.Round),
        )
        listOf(54f to 63f, 72f to 88f).forEach { (start, end) ->
            drawLine(
                color = outline,
                start = Offset(phoneLeft + phoneSize.width, start.dp.toPx()),
                end = Offset(phoneLeft + phoneSize.width, end.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun StepIndicator(step: Int, count: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) { i ->
            Box(
                Modifier.size(28.dp, 6.dp)
                    .background(
                        when {
                            i == step -> Muted
                            i < step -> Color(0xFFE6C7B6)
                            else -> Muted.copy(alpha = .2f)
                        },
                        RoundedCornerShape(4.dp),
                    )
            )
        }
    }
}
