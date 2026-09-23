package com.example.vippela.ui.familiar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vippela.ui.animation.VippelaMotion
import com.example.vippela.ui.animation.familiarTransition
import com.example.vippela.ui.animation.rememberAnimationsEnabled
import com.example.vippela.ui.components.FeedbackHost
import com.example.vippela.ui.components.rememberFeedbackState
import com.example.vippela.ui.theme.LocalVippelaPalette
import com.example.vippela.ui.theme.VippelaPalette
import com.example.vippela.ui.theme.VippelaTheme

private const val HOME = 0
private const val LEARNING = 1
private const val PERFORMANCE = 2
private const val PROFILE = 3
private const val RELEASE = 4
private const val SETTINGS = 5
private const val ADD_FAMILY = 6
private const val GENERATE_CODE = 7

@Composable
fun FamiliarApp(
    darkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
) {
    var selectedPage by rememberSaveable { mutableIntStateOf(HOME) }
    var previousPage by rememberSaveable { mutableIntStateOf(HOME) }
    var notificationsOpen by rememberSaveable { mutableStateOf(false) }
    val feedback = rememberFeedbackState()

    fun navigate(destination: Int) {
        if (destination != selectedPage) {
            previousPage = selectedPage
            selectedPage = destination
        }
    }

    fun notify(title: String, message: String) {
        feedback.show(title, message)
    }

    BackHandler(enabled = selectedPage != HOME) {
        when (selectedPage) {
            GENERATE_CODE -> navigate(ADD_FAMILY)
            ADD_FAMILY, SETTINGS, RELEASE -> navigate(previousPage.coerceIn(HOME, PROFILE))
            else -> navigate(HOME)
        }
    }

    val animationsEnabled = rememberAnimationsEnabled()
    val palette = LocalVippelaPalette.current

    Box(Modifier.fillMaxSize().background(palette.background)) {
        AnimatedContent(
            targetState = selectedPage,
            transitionSpec = {
                familiarTransition(
                    slideInMillis = VippelaMotion.duration(animationsEnabled, VippelaMotion.FamiliarSlideInMillis),
                    slideOutMillis = VippelaMotion.duration(animationsEnabled, VippelaMotion.FamiliarSlideOutMillis),
                    fadeInMillis = VippelaMotion.duration(animationsEnabled, VippelaMotion.FamiliarFadeInMillis),
                    fadeOutMillis = VippelaMotion.duration(animationsEnabled, VippelaMotion.FamiliarFadeOutMillis),
                )
            },
            label = "familiar-navigation",
        ) { pageIndex ->
            when (pageIndex) {
                HOME -> HomeScreen(
                    onNavigate = ::navigate,
                    onNotifications = { notificationsOpen = true },
                    onSettings = { navigate(SETTINGS) },
                )
                LEARNING -> LearningScreen(
                    onNotifications = { notificationsOpen = true },
                    onSettings = { navigate(SETTINGS) },
                    onFeedback = ::notify,
                )
                PERFORMANCE -> PerformanceScreen(
                    onNotifications = { notificationsOpen = true },
                    onSettings = { navigate(SETTINGS) },
                    onFeedback = ::notify,
                )
                PROFILE -> ProfileScreen(
                    onNavigate = ::navigate,
                    onSettings = { navigate(SETTINGS) },
                    onFeedback = ::notify,
                )
                RELEASE -> ReleaseScreen(onFeedback = ::notify)
                SETTINGS -> SettingsScreen(
                    onBack = { navigate(previousPage.coerceIn(HOME, PROFILE)) },
                    onAddFamily = { navigate(ADD_FAMILY) },
                    onFeedback = ::notify,
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                )
                ADD_FAMILY -> AddFamilyScreen(
                    onBack = { navigate(previousPage.coerceIn(HOME, PROFILE)) },
                    onContinue = { navigate(GENERATE_CODE) },
                )
                GENERATE_CODE -> GenerateCodeScreen(
                    onBack = { navigate(ADD_FAMILY) },
                    onContinue = {
                        navigate(HOME)
                        notify("Código gerado", "O código FVG304HI09 está pronto para vincular o familiar.")
                    },
                )
            }
        }

        if (selectedPage <= PROFILE) {
            FamiliarBottomBar(
                selected = selectedPage,
                onNavigate = ::navigate,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        FeedbackHost(
            feedback,
            Modifier.align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 110.dp),
        )
    }

    if (notificationsOpen) {
        NotificationsDialog(onDismiss = { notificationsOpen = false })
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    onNotifications: () -> Unit,
    onSettings: () -> Unit,
    showActions: Boolean = true,
) {
    val palette = LocalVippelaPalette.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).background(palette.accentSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            color = palette.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showActions) {
            CircleIconButton(Icons.Rounded.Notifications, "Notificações", onNotifications)
            Spacer(Modifier.width(8.dp))
            CircleIconButton(Icons.Rounded.Settings, "Configurações", onSettings)
        }
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val palette = LocalVippelaPalette.current
    Box(
        Modifier.size(48.dp).background(palette.cardElevated, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = palette.onCard, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun ScreenScroll(content: @Composable ColumnScope.() -> Unit) {
    val palette = LocalVippelaPalette.current
    Column(
        Modifier.fillMaxSize().background(palette.background).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        content = content,
    )
}

@Composable
private fun ProgressRing(progress: Float, label: String, sublabel: String, size: androidx.compose.ui.unit.Dp = 120.dp) {
    val palette = LocalVippelaPalette.current
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = palette.divider,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = palette.accentSoft,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = palette.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(sublabel, color = palette.muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BarChart() {
    val palette = LocalVippelaPalette.current
    val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    val heights = listOf(.35f, .7f, .55f, .65f, .95f, .04f, .04f)
    val colors = listOf(
        Color(0xFF9B7BE8), Color(0xFF8157E0), palette.onBackground, Color(0xFF8157E0),
        palette.onBackground, Color(0xFFB9A6F0), Color(0xFFB9A6F0),
    )
    Row(
        Modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEachIndexed { i, day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier.padding(bottom = 6.dp)
                        .fillMaxWidth(.42f)
                        .height((90.dp * heights[i]).coerceAtLeast(4.dp))
                        .background(colors[i], RoundedCornerShape(6.dp)),
                )
                Text(day, color = palette.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun AppUsageRow(name: String, time: String, iconColor: Color, icon: ImageVector) {
    val palette = LocalVippelaPalette.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).background(iconColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(name, color = palette.onCard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(time, color = palette.onCard, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun HomeScreen(onNavigate: (Int) -> Unit, onNotifications: () -> Unit, onSettings: () -> Unit) {
    val palette = LocalVippelaPalette.current
    ScreenScroll {
        ScreenHeader("Olá, Gustavo !", onNotifications, onSettings)
        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.card)) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("Hoje", color = palette.onCard, fontSize = 16.sp)
                        Text("3h 23m", color = palette.onCard, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                        Text("TEMPO DE TELA", color = palette.onCard, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Seu uso nesta semana", color = palette.muted, fontSize = 14.sp)
                    }
                    ProgressRing(.6f, "60%", "de redução", 110.dp)
                }
                Spacer(Modifier.height(18.dp))
                BarChart()
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Mais usados hoje", color = palette.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.card)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                AppUsageRow("YouTube", "1h 12min", Color(0xFFFF0000), Icons.Rounded.PlayArrow)
                HorizontalDivider(color = palette.divider)
                AppUsageRow("WhatsApp", "45min", Color(0xFF25D366), Icons.Rounded.Phone)
                HorizontalDivider(color = palette.divider)
                AppUsageRow("Notion", "30min", Color(0xFF111111), Icons.Rounded.Lock)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Continue aprendendo", color = palette.accentSoft, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.card),
            modifier = Modifier.fillMaxWidth().clickable { onNavigate(LEARNING) },
        ) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                FishingHookIcon(Modifier.size(48.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Phising: como se proteger", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Módulo 1 de 3", color = palette.muted, fontSize = 14.sp)
                }
                Box(
                    Modifier.size(56.dp).background(palette.primaryButton, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.PlayArrow, "Continuar", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun FishingHookIcon(modifier: Modifier = Modifier) {
    val palette = LocalVippelaPalette.current
    Canvas(modifier) {
        val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * .55f, size.height * .12f)
            lineTo(size.width * .55f, size.height * .55f)
            cubicTo(
                size.width * .55f, size.height * .9f,
                size.width * .15f, size.height * .9f,
                size.width * .18f, size.height * .6f,
            )
        }
        drawPath(path, color = palette.onBackground, style = stroke)
        drawCircle(color = palette.onBackground, radius = 7.dp.toPx(), center = Offset(size.width * .55f, size.height * .1f))
    }
}

@Composable
private fun LearningScreen(onNotifications: () -> Unit, onSettings: () -> Unit, onFeedback: (String, String) -> Unit) {
    var module by rememberSaveable { mutableIntStateOf(1) }
    val palette = LocalVippelaPalette.current
    ScreenScroll {
        ScreenHeader("Aprendizado", onNotifications, onSettings)
        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.primaryButton)) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                FishingHookIcon(Modifier.size(56.dp).padding(end = 0.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Phising", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.Favorite, null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                    }
                    Text("Produzido por:Vippela", color = Color(0xFFCFC6F5), fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "“Descubra tudo por trás de uma das maiores redes de golpe mundial”",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..3).forEach { m ->
                val active = module == m
                Box(
                    Modifier.weight(1f)
                        .background(if (active) palette.primaryButton else palette.chip, RoundedCornerShape(20.dp))
                        .clickable { module = m }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Módulo $m", color = if (active) Color.White else palette.onCard, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        LessonCard("Oque esperar do curso?", Icons.Rounded.PhoneAndroid, 60, onFeedback)
        Spacer(Modifier.height(16.dp))
        LessonCard("Tipos de ataques de phishing", Icons.Rounded.PersonOff, 60, onFeedback)
        Spacer(Modifier.height(16.dp))
        LessonCard("Agentes maliciosos", Icons.Rounded.Lock, 60, onFeedback)
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun LessonCard(title: String, thumb: ImageVector, progress: Int, onFeedback: (String, String) -> Unit) {
    val palette = LocalVippelaPalette.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardElevated),
        modifier = Modifier.fillMaxWidth().clickable {
            onFeedback("Trilha de phishing", "Módulo selecionado. Seu progresso atual é de $progress%.")
        },
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LessonThumb(thumb, Modifier.size(88.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp)
                Text("Módulo 1 - 15 min", color = palette.accentSoft, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).background(palette.track, RoundedCornerShape(4.dp))) {
                    Box(Modifier.fillMaxWidth(progress / 100f).fillMaxHeight().background(palette.accentSoft, RoundedCornerShape(4.dp)))
                }
                Spacer(Modifier.height(6.dp))
                Text("Concluido  $progress%", color = palette.accentSoft, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun LessonThumb(icon: ImageVector, modifier: Modifier = Modifier) {
    val palette = LocalVippelaPalette.current
    Box(modifier.background(palette.surfaceMuted, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = palette.accentSoft, modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun PerformanceScreen(onNotifications: () -> Unit, onSettings: () -> Unit, onFeedback: (String, String) -> Unit) {
    val palette = LocalVippelaPalette.current
    ScreenScroll {
        ScreenHeader("Desempenho", onNotifications, onSettings)
        Spacer(Modifier.height(8.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.card)) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(.7f, "70%", "de redução", 110.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Seu progresso", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Você está quase lá", color = palette.muted, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier.background(palette.primaryButton, RoundedCornerShape(8.dp))
                            .clickable {
                                onFeedback("Seu progresso", "Você reduziu 70% do tempo de tela e está quase atingindo seus objetivos.")
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text("Mais detalhes", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Objetivos", color = palette.accentSoft, fontSize = 26.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("Ver todos >", color = palette.accentSoft, fontSize = 14.sp)
        }
        Spacer(Modifier.height(10.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                GoalRow("Estudar", "2h /4h", .5f, palette.accent, Icons.Rounded.School)
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = palette.divider)
                Spacer(Modifier.height(14.dp))
                GoalRow("Dormir", "8h /9h", .88f, palette.accentSoft, Icons.Rounded.Bedtime)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("Progresso Semanal", color = palette.accentSoft, fontSize = 26.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))

        WeeklyCard("-3h de tempo de tela", badge = "25% ↑", badgeColor = palette.success, activeDays = setOf(0, 1))
        Spacer(Modifier.height(14.dp))
        WeeklyCard("60% de tempo produtivo", activeDays = setOf(0, 1), highlightWord = "produtivo")
        Spacer(Modifier.height(14.dp))
        WeeklyCard("Aprenda sobre phishing", activeDays = setOf(0, 1), highlightWord = "phishing", icon = Icons.Rounded.Shield)
        Spacer(Modifier.height(14.dp))
        WeeklyCard("Adicionar objetivo", activeDays = setOf(0, 1), centered = true)
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun GoalRow(title: String, value: String, progress: Float, color: Color, icon: ImageVector) {
    val palette = LocalVippelaPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).background(color.copy(alpha = .2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(value, color = palette.muted, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).background(palette.track, RoundedCornerShape(5.dp))) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(color, RoundedCornerShape(5.dp)))
            }
        }
    }
}

@Composable
private fun WeeklyCard(
    title: String,
    badge: String? = null,
    badgeColor: Color = Color(0xFF44AF52),
    activeDays: Set<Int>,
    highlightWord: String? = null,
    icon: ImageVector = Icons.Rounded.AccessTime,
    centered: Boolean = false,
) {
    val palette = LocalVippelaPalette.current
    val days = listOf("seg", "ter", "qua", "qui", "sex", "sab", "dom")
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!centered) {
                    Icon(icon, null, tint = palette.accentSoft, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                val titleModifier = if (centered) Modifier.fillMaxWidth() else Modifier.weight(1f)
                if (highlightWord != null && title.contains(highlightWord)) {
                    val parts = title.split(highlightWord)
                    Text(
                        buildString {
                            append(parts[0])
                            append(highlightWord)
                            if (parts.size > 1) append(parts[1])
                        },
                        color = palette.onCard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                        modifier = titleModifier,
                    )
                } else {
                    Text(
                        title,
                        color = palette.onCard,
                        fontSize = 16.sp,
                        fontWeight = if (centered) FontWeight.Bold else FontWeight.Medium,
                        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                        modifier = titleModifier,
                    )
                }
                if (badge != null) {
                    Text(badge, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day ->
                    Text(day, color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { i, _ ->
                    Box(
                        Modifier.weight(1f).wrapContentSize(Alignment.Center)
                            .size(22.dp)
                            .background(if (i in activeDays) palette.primaryButton else palette.track, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(onNavigate: (Int) -> Unit, onSettings: () -> Unit, onFeedback: (String, String) -> Unit) {
    var period by rememberSaveable { mutableIntStateOf(0) }
    val palette = LocalVippelaPalette.current
    ScreenScroll {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar") { onNavigate(HOME) }
            Spacer(Modifier.width(12.dp))
            Text("Perfil", color = palette.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            CircleIconButton(Icons.Rounded.Settings, "Configurações", onSettings)
        }

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.card)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(96.dp).background(palette.accentSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Text("G", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Gustavo Caldeira", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("9 anos", color = palette.accentSoft, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Conhecimento digital:", color = palette.muted, fontSize = 14.sp)
                    Text("Básico", color = palette.onCard, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = palette.surfaceMuted)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("● Dispositivo em uso", color = palette.success, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Shield, null, tint = palette.onBackground, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ultimo uso há 0 min", color = palette.onBackground, fontSize = 14.sp)
                    }
                }
                Box(Modifier.size(56.dp).background(palette.chip, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PhoneAndroid, null, tint = palette.onCard, modifier = Modifier.size(28.dp))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth().background(palette.cardElevated, RoundedCornerShape(24.dp)).padding(6.dp),
        ) {
            listOf("Dia", "Semana", "Mês").forEachIndexed { i, label ->
                val active = period == i
                Box(
                    Modifier.weight(1f)
                        .background(if (active) palette.primaryButton else Color.Transparent, RoundedCornerShape(20.dp))
                        .clickable {
                            period = i
                            onFeedback("Período atualizado", "Use Dia, Semana ou Mês para acompanhar o histórico.")
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, color = if (active) Color.White else palette.onCard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Hoje", color = palette.accentSoft, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tempo de tela", color = palette.accentSoft, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("2h  14min", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                        Text("de 3h permitidas", color = palette.muted, fontSize = 14.sp)
                    }
                    Box(Modifier.size(56.dp).background(palette.track, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AccessTime, null, tint = palette.accentSoft, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(12.dp).background(palette.track, RoundedCornerShape(6.dp))) {
                    Box(Modifier.fillMaxWidth(.71f).fillMaxHeight().background(palette.primaryButton, RoundedCornerShape(6.dp)))
                }
                Spacer(Modifier.height(8.dp))
                Text("71% do limite diario", color = palette.success, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Objetivos", color = palette.accentSoft, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("15 min por semana", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("● Estudar", color = palette.onCard, fontSize = 15.sp)
                    }
                    Box(Modifier.size(56.dp).background(palette.accent.copy(alpha = .15f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Flag, null, tint = palette.accent, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().height(12.dp).background(palette.track, RoundedCornerShape(6.dp))) {
                    Box(Modifier.fillMaxWidth(.6f).fillMaxHeight().background(palette.accent, RoundedCornerShape(6.dp)))
                }
                Spacer(Modifier.height(10.dp))
                Row {
                    Text("9/15 Min", color = palette.onCard, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("3 dias restantes", color = palette.onCard, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ReleaseScreen(onFeedback: (String, String) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    val palette = LocalVippelaPalette.current
    ScreenScroll {
        ScreenHeader("Configurações", {}, {}, showActions = true)
        Spacer(Modifier.height(4.dp))
        Text("Liberação de app", color = palette.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).background(Color(0xFFFFE5E5), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFFFF0000), modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("YouTube", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Esse app não está na sua lista de permitidos. Mas você pode pedir para liberar!",
                        color = palette.muted,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().background(palette.primaryButton, RoundedCornerShape(12.dp))
                .clickable { onFeedback("Pedido preparado", "Adicione uma justificativa e envie o pedido ao seu responsável.") }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Pedir liberação", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }

        Spacer(Modifier.height(20.dp))
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text(
                    "Escreva um recado para quem cuida de você (opcional)",
                    color = palette.onCard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(12.dp))
                BasicTextField(
                    value = message,
                    onValueChange = { message = it.take(160) },
                    textStyle = TextStyle(color = palette.onCard, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.field, RoundedCornerShape(10.dp))
                        .border(1.dp, palette.divider, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    decorationBox = { inner ->
                        Box {
                            if (message.isEmpty()) {
                                Text("Ex: Usar o YouTube para estudar", color = palette.muted, fontSize = 15.sp)
                            }
                            inner()
                        }
                    },
                )
                Spacer(Modifier.height(14.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Para estudar", "Para o dever de casa", "Só mais 30 min").forEach { chip ->
                        Box(
                            Modifier.border(1.5.dp, palette.onCard, RoundedCornerShape(20.dp))
                                .clickable { message = chip }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(chip, color = palette.onCard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth().background(palette.primaryButton, RoundedCornerShape(12.dp))
                        .clickable { onFeedback("Pedido enviado", "Seu responsável recebeu a solicitação de liberação do YouTube.") }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Enviar pedido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("Seus pedidos", color = palette.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        RequestRow("YouTube", "Aguardando", Color(0xFFF5A623), Icons.Rounded.PlayArrow, Color(0xFFFFE5E5), Color(0xFFFF0000))
        Spacer(Modifier.height(10.dp))
        RequestRow("Instagram", "Liberado", palette.success, Icons.Rounded.CameraAlt, Color(0xFFFFE5E5), Color(0xFFE1306C))
        Spacer(Modifier.height(10.dp))
        RequestRow("TikTok", "Recusado", palette.muted, Icons.Rounded.Close, palette.field, palette.onCard, action = "pedir de novo")
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun RequestRow(
    name: String,
    status: String,
    statusColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    action: String? = null,
) {
    val palette = LocalVippelaPalette.current
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = palette.cardElevated)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(iconBg, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("● $status", color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            if (action != null) {
                Text(action, color = palette.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
            }
        }
    }
}

@Composable
private fun FamiliarBottomBar(selected: Int, onNavigate: (Int) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalVippelaPalette.current
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).height(82.dp),
        shape = RoundedCornerShape(30.dp),
        color = palette.cardElevated,
        shadowElevation = 9.dp,
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            NavItem(0, selected, onNavigate, "Início") { active -> HomeNavIcon(active) }
            NavItem(1, selected, onNavigate, "Trilhas") { active -> SchoolNavIcon(active) }
            NavItem(2, selected, onNavigate, "Relatório") { active -> ChartNavIcon(active) }
            NavItem(3, selected, onNavigate, "Perfil") { active ->
                Box(
                    Modifier.size(28.dp).background(if (active) palette.accent else palette.accentSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("G", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    index: Int,
    selected: Int,
    onNavigate: (Int) -> Unit,
    label: String,
    icon: @Composable (Boolean) -> Unit,
) {
    val palette = LocalVippelaPalette.current
    val active = selected == index
    Column(
        Modifier.weight(1f).fillMaxHeight()
            .background(if (active) palette.accent.copy(alpha = .18f) else Color.Transparent, RoundedCornerShape(24.dp))
            .clickable { onNavigate(index) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon(active)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = if (active) palette.accent else palette.onCard,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun HomeNavIcon(active: Boolean) {
    val palette = LocalVippelaPalette.current
    Canvas(Modifier.size(24.dp)) {
        val color = if (active) palette.accent else palette.onCard
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * .15f, size.height * .5f)
            lineTo(size.width * .5f, size.height * .15f)
            lineTo(size.width * .85f, size.height * .5f)
        }
        drawPath(path, color, style = stroke)
        val body = Path().apply {
            moveTo(size.width * .25f, size.height * .48f)
            lineTo(size.width * .25f, size.height * .85f)
            lineTo(size.width * .75f, size.height * .85f)
            lineTo(size.width * .75f, size.height * .48f)
        }
        drawPath(body, color, style = stroke)
    }
}

@Composable
private fun SchoolNavIcon(active: Boolean) {
    val palette = LocalVippelaPalette.current
    Canvas(Modifier.size(24.dp)) {
        val color = if (active) palette.accent else palette.onCard
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        val cap = Path().apply {
            moveTo(size.width * .5f, size.height * .2f)
            lineTo(size.width * .9f, size.height * .4f)
            lineTo(size.width * .5f, size.height * .6f)
            lineTo(size.width * .1f, size.height * .4f)
            close()
        }
        drawPath(cap, color, style = stroke)
        val body = Path().apply {
            moveTo(size.width * .3f, size.height * .48f)
            lineTo(size.width * .3f, size.height * .7f)
            cubicTo(
                size.width * .3f, size.height * .85f,
                size.width * .7f, size.height * .85f,
                size.width * .7f, size.height * .7f,
            )
            lineTo(size.width * .7f, size.height * .48f)
        }
        drawPath(body, color, style = stroke)
    }
}

@Composable
private fun ChartNavIcon(active: Boolean) {
    val palette = LocalVippelaPalette.current
    Canvas(Modifier.size(24.dp)) {
        val color = if (active) palette.accent else palette.onCard
        val bar = 4.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * .15f, size.height * .55f),
            size = Size(bar, size.height * .3f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * .45f, size.height * .2f),
            size = Size(bar, size.height * .65f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * .75f, size.height * .4f),
            size = Size(bar, size.height * .45f),
        )
    }
}

@Composable
private fun NotificationsDialog(onDismiss: () -> Unit) {
    val palette = LocalVippelaPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardElevated,
        titleContentColor = palette.onCard,
        textContentColor = palette.muted,
        title = { Text("Notificações", color = palette.onCard, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NotificationItem("Meta quase concluída", "Faltam 15 minutos para atingir seu objetivo de estudo.", palette.accent)
                NotificationItem("Tempo de tela", "Você reduziu 25% do uso nesta semana.", palette.success)
                NotificationItem("Novo conteúdo", "O módulo sobre phishing já está disponível.", palette.accentSoft)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Marcar como lidas", color = palette.accent) } },
    )
}

@Composable
private fun NotificationItem(title: String, message: String, color: Color) {
    val palette = LocalVippelaPalette.current
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 6.dp).size(9.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = palette.onCard, fontWeight = FontWeight.Bold)
            Text(message, color = palette.muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AddFamilyScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val valid = listOf(name, birthDate, phone, email).all { it.isNotBlank() }
    val palette = LocalVippelaPalette.current

    Column(
        Modifier.fillMaxSize().background(palette.background).verticalScroll(rememberScrollState()).padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).background(palette.field, RoundedCornerShape(18.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar", tint = palette.onBackground, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.background(palette.step, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("1 de 2", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Adicionar familiar", color = palette.onBackground, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text(
            "Informe os dados do familiar para vinculá-lo ao seu perfil.",
            color = palette.accentSoft,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(36.dp))
        FamilyField(name, { name = it }, "Nome completo", showErrors && name.isBlank(), KeyboardType.Text)
        Spacer(Modifier.height(16.dp))
        FamilyField(birthDate, { birthDate = it.take(10) }, "Data de nascimento", showErrors && birthDate.isBlank(), KeyboardType.Number)
        Spacer(Modifier.height(16.dp))
        FamilyField(phone, { phone = it.take(15) }, "Telefone", showErrors && phone.isBlank(), KeyboardType.Phone)
        Spacer(Modifier.height(16.dp))
        FamilyField(email, { email = it }, "E-mail:", showErrors && email.isBlank(), KeyboardType.Email)

        if (showErrors && !valid) {
            Spacer(Modifier.height(10.dp))
            Text("Preencha todos os campos obrigatórios", color = Color(0xFFFF4B16), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(48.dp))
        Box(
            Modifier.fillMaxWidth().background(palette.primaryButton, RoundedCornerShape(14.dp))
                .clickable {
                    if (valid) onContinue() else showErrors = true
                }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Continuar  ›", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(Modifier.size(width = 36.dp, height = 12.dp).background(palette.accentSoft, RoundedCornerShape(6.dp)))
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(width = 36.dp, height = 12.dp).background(palette.chip, RoundedCornerShape(6.dp)))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FamilyField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    invalid: Boolean,
    keyboardType: KeyboardType,
) {
    val palette = LocalVippelaPalette.current
    Row(
        Modifier.fillMaxWidth()
            .background(palette.field, RoundedCornerShape(12.dp))
            .border(if (invalid) 2.dp else 0.dp, if (invalid) Color(0xFFFF4B16) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Person, null, tint = palette.onBackground, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = palette.accentSoft, fontSize = 17.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(color = palette.onBackground, fontSize = 17.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (invalid) {
            Text("!", color = Color(0xFFFF4B16), fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
    }
}

@Composable
private fun GenerateCodeScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val palette = LocalVippelaPalette.current
    Column(
        Modifier.fillMaxSize().background(palette.background).verticalScroll(rememberScrollState()).padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).background(palette.field, RoundedCornerShape(18.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar", tint = palette.onBackground, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.background(palette.step, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("2 de 2", color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Gerar código", color = palette.onBackground, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text(
            "Crie um código para autorizar compras e saques.",
            color = palette.accentSoft,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))
        Box(
            Modifier.size(220.dp).align(Alignment.CenterHorizontally).background(Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(180.dp)) {
                val modules = 9
                val cell = size.width / modules
                val black = Color.Black
                for (r in 0 until modules) {
                    for (c in 0 until modules) {
                        val corner =
                            (r < 3 && c < 3) || (r < 3 && c >= modules - 3) || (r >= modules - 3 && c < 3)
                        val fill = if (corner) {
                            val rr = r % (modules - 3).coerceAtLeast(1)
                            val cc = c
                            (r + c) % 2 == 0 || (r in 1..1 && c in 1..1)
                        } else {
                            ((r * 7 + c * 5 + r * c) % 3) != 0
                        }
                        if (fill) {
                            drawRect(
                                color = black,
                                topLeft = Offset(c * cell, r * cell),
                                size = Size(cell, cell),
                            )
                        }
                    }
                }
                val finder = { x: Float, y: Float ->
                    drawRect(black, Offset(x, y), Size(cell * 3, cell * 3))
                    drawRect(Color.White, Offset(x + cell, y + cell), Size(cell, cell))
                    drawRect(black, Offset(x + cell * 1.2f, y + cell * 1.2f), Size(cell * .6f, cell * .6f))
                }
                finder(0f, 0f)
                finder(size.width - cell * 3, 0f)
                finder(0f, size.height - cell * 3)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "FVG304HI09",
            color = palette.onBackground,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))
        Text("Observações", color = palette.accentSoft, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text(
            "Com o código gerado,insira-o na seção “vincular familiar”",
            color = palette.accentSoft,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(40.dp))
        Box(
            Modifier.fillMaxWidth().background(palette.primaryButton, RoundedCornerShape(14.dp))
                .clickable(onClick = onContinue)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Continuar  ›", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(Modifier.size(width = 36.dp, height = 12.dp).background(palette.accentSoft, RoundedCornerShape(6.dp)))
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(width = 36.dp, height = 12.dp).background(palette.chip, RoundedCornerShape(6.dp)))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsScreen(
    onBack: () -> Unit,
    onAddFamily: () -> Unit,
    onFeedback: (String, String) -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    var notifications by rememberSaveable { mutableStateOf(true) }
    var reminders by rememberSaveable { mutableStateOf(true) }
    val palette = LocalVippelaPalette.current
    Column(
        Modifier.fillMaxSize().background(palette.background).verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar", tint = palette.onBackground) }
            Text("Configurações", color = palette.onBackground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        SettingsSection("Conta") {
            SettingsAction("Editar perfil", "Nome, foto e informações") { onFeedback("Editar perfil", "A edição de perfil está pronta para integração com seus dados.") }
            SettingsAction("Segurança e privacidade", "Senha, dados e dispositivos") { onFeedback("Segurança", "Seu dispositivo está protegido e conectado à família.") }
        }
        Spacer(Modifier.height(18.dp))
        SettingsSection("Preferências") {
            SettingsToggle("Notificações", "Alertas importantes do aplicativo", notifications) { notifications = it }
            SettingsToggle("Lembretes", "Objetivos e tempo de tela", reminders) { reminders = it }
            SettingsToggle("Modo escuro", "Alterar aparência do aplicativo", darkMode, onChecked = { checked ->
                onDarkModeChange(checked)
                onFeedback(
                    if (checked) "Modo escuro ativado" else "Modo claro ativado",
                    if (checked) "O aplicativo agora usa o tema escuro." else "O aplicativo voltou ao tema claro.",
                )
            })
        }
        Spacer(Modifier.height(18.dp))
        SettingsSection("Família") {
            SettingsAction("Adicionar familiar", "Gerar código e vincular um novo perfil", onAddFamily)
            SettingsAction("Conexão familiar", "Conectado a Cleber dos Santos Araújo") { onFeedback("Família", "A conexão familiar está ativa.") }
            SettingsAction("Solicitações de aplicativos", "Acompanhar pedidos enviados") { onFeedback("Solicitações", "Você possui um pedido aguardando resposta.") }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { onFeedback("Sair da conta", "A confirmação de saída será solicitada antes de desconectar.") }, modifier = Modifier.fillMaxWidth()) {
            Text("Sair da conta", color = palette.danger)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val palette = LocalVippelaPalette.current
    Text(title, color = palette.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Card(colors = CardDefaults.cardColors(containerColor = palette.cardElevated), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp), content = content)
    }
}

@Composable
private fun SettingsAction(title: String, subtitle: String, onClick: () -> Unit) {
    val palette = LocalVippelaPalette.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = palette.onCard, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = palette.muted, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = palette.onCard, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    val palette = LocalVippelaPalette.current
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = palette.onCard, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = palette.muted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = palette.primaryButton))
    }
}

private val AccentOrange = Color(0xFFFF7424)

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun FamiliarAppPreview() {
    VippelaTheme {
        FamiliarApp()
    }
}
