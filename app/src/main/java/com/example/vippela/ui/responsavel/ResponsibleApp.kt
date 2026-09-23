package com.example.vippela.ui.responsavel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vippela.R

private val Purple = Color(0xFF1C006B)
private val Violet = Color(0xFF55429B)
private val Orange = Color(0xFFFF7627)
private val SoftOrange = Color(0xFFFFEBDD)
private val Background = Color(0xFFF4F2FA)
private val Muted = Color(0xFF716D83)
private val Green = Color(0xFF39AE5A)

private enum class AuthScreen { ROLE, LOGIN, REGISTER, PASSWORD, PROFILES, APP }
private enum class MainTab { HOME, LEARNING, REPORTS, SETTINGS, PROFILE }

@Composable
fun ResponsibleApp() {
    var screen by rememberSaveable { mutableStateOf(AuthScreen.ROLE) }
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var notifications by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = screen != AuthScreen.ROLE || tab != MainTab.HOME) {
        if (screen == AuthScreen.APP && tab != MainTab.HOME) tab = MainTab.HOME
        else screen = when (screen) {
            AuthScreen.LOGIN -> AuthScreen.ROLE
            AuthScreen.REGISTER -> AuthScreen.LOGIN
            AuthScreen.PASSWORD -> AuthScreen.REGISTER
            AuthScreen.PROFILES -> AuthScreen.LOGIN
            AuthScreen.APP -> AuthScreen.PROFILES
            else -> AuthScreen.ROLE
        }
    }

    AnimatedContent(screen, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "responsible-flow") { current ->
        when (current) {
            AuthScreen.ROLE -> RoleScreen(onResponsible = { screen = AuthScreen.LOGIN })
            AuthScreen.LOGIN -> LoginScreen(
                onBack = { screen = AuthScreen.ROLE },
                onLogin = { screen = AuthScreen.PROFILES },
                onRegister = { screen = AuthScreen.REGISTER },
            )
            AuthScreen.REGISTER -> RegisterScreen(
                onBack = { screen = AuthScreen.LOGIN },
                onContinue = { screen = AuthScreen.PASSWORD },
            )
            AuthScreen.PASSWORD -> PasswordScreen(
                onBack = { screen = AuthScreen.REGISTER },
                onFinish = { screen = AuthScreen.PROFILES },
            )
            AuthScreen.PROFILES -> ProfilesScreen(
                onBack = { screen = AuthScreen.LOGIN },
                onSelect = { screen = AuthScreen.APP },
            )
            AuthScreen.APP -> ResponsibleScaffold(
                selected = tab,
                onSelect = { tab = it },
                onNotifications = { notifications = true },
            )
        }
    }

    if (notifications) ResponsibleNotifications(onDismiss = { notifications = false })
}

@Composable
private fun RoleScreen(onResponsible: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))
        Image(painterResource(R.drawable.launcher_logo), "Vippela", Modifier.size(106.dp))
        Spacer(Modifier.height(24.dp))
        Text("Área do responsável", color = Purple, fontSize = 31.sp, fontWeight = FontWeight.Bold)
        Text("Entre para acompanhar sua família", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(42.dp))
        RoleCard("Continuar como responsável", Icons.Rounded.Person, Modifier.fillMaxWidth(), onResponsible)
        Spacer(Modifier.weight(1f))
        Text("Proteção e bem-estar digital para sua família", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 28.dp))
    }
}

@Composable
private fun RoleCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier.clickable(onClick = onClick).height(175.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(68.dp).background(SoftOrange, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Purple, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, color = Purple, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

@Composable
private fun LoginScreen(onBack: () -> Unit, onLogin: () -> Unit, onRegister: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AuthColumn(onBack) {
        Text("Entrar na Conta", color = Purple, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Que bom ter você de volta!", color = Muted)
        Spacer(Modifier.height(28.dp))
        AppField(email, { email = it }, "E-mail", Icons.Rounded.Email, KeyboardType.Email)
        Spacer(Modifier.height(14.dp))
        AppField(password, { password = it }, "Senha", Icons.Rounded.Lock, password = true)
        TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) { Text("Esqueci minha senha", color = Violet) }
        PrimaryButton("Entrar", enabled = email.isNotBlank() && password.isNotBlank(), onClick = onLogin)
        Spacer(Modifier.height(20.dp))
        Text("ou", color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialButton("Google", Modifier.weight(1f))
            SocialButton("Facebook", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text("Ainda não tem conta?", color = Muted)
            TextButton(onClick = onRegister) { Text("Cadastre-se", color = Orange, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun RegisterScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var birth by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    AuthColumn(onBack) {
        StepBadge("1 de 2")
        Text("Cadastro", color = Purple, fontSize = 31.sp, fontWeight = FontWeight.Bold)
        Text("Preencha seus dados pessoais", color = Muted)
        Spacer(Modifier.height(24.dp))
        AppField(name, { name = it }, "Nome completo", Icons.Rounded.Person)
        Spacer(Modifier.height(12.dp))
        AppField(birth, { birth = it.take(10) }, "Data de nascimento", Icons.Rounded.Info, KeyboardType.Number)
        Spacer(Modifier.height(12.dp))
        AppField(phone, { phone = it.take(15) }, "Telefone", Icons.Rounded.Info, KeyboardType.Phone)
        Spacer(Modifier.height(12.dp))
        AppField(email, { email = it }, "E-mail", Icons.Rounded.Email, KeyboardType.Email)
        Spacer(Modifier.height(26.dp))
        PrimaryButton("Continuar", listOf(name, birth, phone, email).all { it.isNotBlank() }, onContinue)
        StepProgress(0)
    }
}

@Composable
private fun PasswordScreen(onBack: () -> Unit, onFinish: () -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val valid = password.length >= 8 && password == confirm
    AuthColumn(onBack) {
        StepBadge("2 de 2")
        Text("Último passo", color = Purple, fontSize = 31.sp, fontWeight = FontWeight.Bold)
        Text("Crie uma senha para proteger sua conta", color = Muted)
        Spacer(Modifier.height(28.dp))
        AppField(password, { password = it }, "Senha", Icons.Rounded.Lock, password = true)
        Spacer(Modifier.height(14.dp))
        AppField(confirm, { confirm = it }, "Confirmar senha", Icons.Rounded.Lock, password = true)
        Spacer(Modifier.height(18.dp))
        PasswordRule("Mínimo de 8 caracteres", password.length >= 8)
        PasswordRule("As senhas são iguais", password.isNotBlank() && password == confirm)
        Spacer(Modifier.height(28.dp))
        PrimaryButton("Finalizar", valid, onFinish)
        StepProgress(1)
    }
}

@Composable
private fun ProfilesScreen(onBack: () -> Unit, onSelect: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Background).padding(24.dp)) {
        AppBack(onBack)
        Spacer(Modifier.height(10.dp))
        Image(painterResource(R.drawable.launcher_logo), null, Modifier.size(72.dp).align(Alignment.CenterHorizontally))
        Text("Vippela", color = Purple, fontSize = 29.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Selecione o perfil para continuar", color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(24.dp))
        ProfileRow("Gabriel Silva", "Online", "GS", onSelect)
        ProfileRow("Maria Liz", "Online", "ML", onSelect)
        ProfileRow("Léo Souza", "Offline", "LS", onSelect)
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(8.dp)); Text("Adicionar perfil")
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Gerenciar perfis", true, onSelect)
    }
}

@Composable
private fun ResponsibleScaffold(selected: MainTab, onSelect: (MainTab) -> Unit, onNotifications: () -> Unit) {
    Scaffold(
        containerColor = Background,
        bottomBar = { ResponsibleBottomBar(selected, onSelect) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selected) {
                MainTab.HOME -> ResponsibleHome(onNotifications)
                MainTab.LEARNING -> ResponsibleLearning(onNotifications)
                MainTab.REPORTS -> ResponsibleReports(onNotifications)
                MainTab.SETTINGS -> ResponsibleSettings(onNotifications)
                MainTab.PROFILE -> ResponsibleProfile(onNotifications)
            }
        }
    }
}

@Composable
private fun ScreenColumn(title: String, onNotifications: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, color = Purple, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
            IconButton(onClick = onNotifications, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.Rounded.Notifications, "Notificações", tint = Purple) }
        }
        Spacer(Modifier.height(20.dp))
        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ResponsibleHome(onNotifications: () -> Unit) = ScreenColumn("Início", onNotifications) {
    Text("Bem-vindo, Cleber", color = Purple, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))
    FamilySelector()
    Spacer(Modifier.height(18.dp))
    Text("Alertas do período", color = Purple, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("2", "Bloqueios", Orange, Modifier.weight(1f))
        StatCard("4h", "Tempo de tela", Violet, Modifier.weight(1f))
        StatCard("3", "Objetivos", Green, Modifier.weight(1f))
    }
    Spacer(Modifier.height(18.dp))
    AppCard {
        Text("Uso de hoje", color = Purple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(progress = { .71f }, modifier = Modifier.fillMaxWidth().height(12.dp), color = Violet, trackColor = Color(0xFFE1DFE8), strokeCap = StrokeCap.Round)
        Spacer(Modifier.height(8.dp))
        Row { Text("2h 14min", color = Purple, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("de 3h permitidas", color = Muted) }
    }
    Spacer(Modifier.height(16.dp))
    AppCard {
        Text("Recomendações", color = Purple, fontWeight = FontWeight.Bold)
        Text("Revise os aplicativos mais usados e ajuste os limites quando necessário.", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun ResponsibleLearning(onNotifications: () -> Unit) = ScreenColumn("Aprendizado", onNotifications) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LearningCategory("Início", true, Modifier.weight(1f)); LearningCategory("Phishing", false, Modifier.weight(1f)); LearningCategory("Privacidade", false, Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))
    Text("Últimos vistos", color = Purple, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    LessonCard("Agentes maliciosos", "Módulo 1 • 15 min", .60f)
    LessonCard("Como reconhecer golpes", "Módulo 2 • 12 min", .25f)
    LessonCard("Privacidade familiar", "Módulo 1 • 10 min", .80f)
}

@Composable
private fun ResponsibleReports(onNotifications: () -> Unit) = ScreenColumn("Relatórios", onNotifications) {
    FamilySelector()
    Spacer(Modifier.height(18.dp))
    AppCard {
        Row { Text("Tempo de tela", color = Purple, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("2h 14min", color = Violet) }
        Spacer(Modifier.height(16.dp))
        UsageChart()
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("Seg","Ter","Qua","Qui","Sex","Sáb","Dom").forEach { Text(it, fontSize = 11.sp, color = Muted) } }
    }
    Spacer(Modifier.height(16.dp))
    Text("Monitoramento de atividades", color = Purple, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    ActivityRow("Aplicativos mais usados", "YouTube, WhatsApp e Notion", Orange)
    ActivityRow("Bloqueios de aplicativos", "3 bloqueios hoje", Violet)
    ActivityRow("Conteúdo acessado", "Dentro dos limites", Green)
}

@Composable
private fun ResponsibleSettings(onNotifications: () -> Unit) {
    var dailyLimit by rememberSaveable { mutableFloatStateOf(3f) }
    var alerts by rememberSaveable { mutableStateOf(true) }
    ScreenColumn("Configurações", onNotifications) {
        Text("Limites e permissões", color = Purple, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        AppCard {
            Text("Limite diário de tela", color = Purple, fontWeight = FontWeight.SemiBold)
            Text("${dailyLimit.toInt()} horas por dia", color = Muted)
            Slider(value = dailyLimit, onValueChange = { dailyLimit = it }, valueRange = 1f..8f, colors = SliderDefaults.colors(thumbColor = Orange, activeTrackColor = Orange))
        }
        Spacer(Modifier.height(14.dp))
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Alertas e lembretes", color = Purple, fontWeight = FontWeight.SemiBold); Text("Receber alertas de atividade", color = Muted, fontSize = 12.sp) }
                Switch(alerts, { alerts = it })
            }
        }
        Spacer(Modifier.height(14.dp))
        SettingRow("Editar perfil", "Foto, nome e informações")
        SettingRow("Uso e segurança", "Senha e dispositivos")
        SettingRow("Gerenciar plano", "Plano familiar ativo")
    }
}

@Composable
private fun ResponsibleProfile(onNotifications: () -> Unit) = ScreenColumn("Perfil", onNotifications) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar("GC", 68.dp)
            Spacer(Modifier.width(14.dp))
            Column { Text("Gustavo Caldeira", color = Purple, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Responsável familiar", color = Muted); Text("Ativo • Protegido", color = Green, fontSize = 12.sp) }
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("Família", color = Purple, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    ProfileRow("Gabriel Silva", "Online", "GS", {})
    ProfileRow("Maria Liz", "Online", "ML", {})
    ProfileRow("Léo Souza", "Offline", "LS", {})
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Add, null); Spacer(Modifier.width(8.dp)); Text("Adicionar familiar") }
}

@Composable
private fun ResponsibleBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    val items = listOf(
        Triple(MainTab.HOME, Icons.Rounded.Home, "Início"),
        Triple(MainTab.LEARNING, Icons.Rounded.Star, "Aprender"),
        Triple(MainTab.REPORTS, Icons.Rounded.Info, "Relatórios"),
        Triple(MainTab.SETTINGS, Icons.Rounded.Settings, "Config."),
        Triple(MainTab.PROFILE, Icons.Rounded.Person, "Perfil"),
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 7.dp) {
        items.forEach { (tab, icon, label) ->
            NavigationBarItem(selected == tab, { onSelect(tab) }, { Icon(icon, label) }, label = { Text(label, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Orange, selectedTextColor = Orange, indicatorColor = SoftOrange, unselectedIconColor = Muted, unselectedTextColor = Muted))
        }
    }
}

@Composable
private fun ResponsibleNotifications(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notificações", color = Purple, fontWeight = FontWeight.Bold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            NotificationLine("Limite atingido", "Gabriel chegou a 90% do limite diário.", Orange)
            NotificationLine("Objetivo concluído", "Maria concluiu a meta de estudo.", Green)
            NotificationLine("Solicitação de app", "Há um novo pedido para liberar o YouTube.", Violet)
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Marcar como lidas") } },
    )
}

@Composable private fun NotificationLine(title: String, text: String, color: Color) = Row { Box(Modifier.padding(top = 6.dp).size(9.dp).background(color, CircleShape)); Spacer(Modifier.width(10.dp)); Column { Text(title, color = Purple, fontWeight = FontWeight.Bold); Text(text, color = Muted, fontSize = 13.sp) } }

@Composable
private fun AuthColumn(onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(26.dp)) { AppBack(onBack); Spacer(Modifier.height(18.dp)); content() }
@Composable private fun AppBack(onClick: () -> Unit) = IconButton(onClick, modifier = Modifier.background(Color.White, CircleShape)) { Icon(Icons.Rounded.ArrowBack, "Voltar", tint = Purple) }
@Composable private fun StepBadge(text: String) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Box(Modifier.background(Color(0xFFD6C3FF), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 5.dp)) { Text(text, color = Violet, fontWeight = FontWeight.Bold) } }
@Composable private fun StepProgress(active: Int) = Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) { repeat(2) { Box(Modifier.padding(4.dp).width(if (it == active) 44.dp else 24.dp).height(7.dp).background(if (it == active) Violet else Color(0xFFD6D2E3), CircleShape)) } }

@Composable
private fun AppField(value: String, onValue: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false) {
    OutlinedTextField(value, onValue, Modifier.fillMaxWidth(), label = { Text(label) }, leadingIcon = { Icon(icon, null) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard), visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, focusedLabelColor = Purple, cursorColor = Purple, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
}
@Composable private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) = Button(onClick, Modifier.fillMaxWidth().height(54.dp), enabled = enabled, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Icon(Icons.Rounded.KeyboardArrowRight, null) }
@Composable private fun SocialButton(text: String, modifier: Modifier) = OutlinedButton({}, modifier.height(48.dp), shape = RoundedCornerShape(10.dp)) { Text(text, color = Purple) }
@Composable private fun PasswordRule(text: String, valid: Boolean) = Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.CheckCircle, null, tint = if (valid) Green else Color(0xFFB7B3C3), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(text, color = if (valid) Green else Muted, fontSize = 13.sp) }

@Composable
private fun ProfileRow(name: String, status: String, initials: String, onClick: () -> Unit) = Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(initials, 50.dp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(name, color = Purple, fontWeight = FontWeight.Bold); Text(status, color = if (status == "Online") Green else Muted, fontSize = 12.sp) }; Icon(Icons.Rounded.KeyboardArrowRight, null, tint = Muted) } }
@Composable private fun Avatar(initials: String, size: androidx.compose.ui.unit.Dp) = Box(Modifier.size(size).background(Color(0xFFE2DAF5), CircleShape), contentAlignment = Alignment.Center) { Text(initials, color = Purple, fontWeight = FontWeight.Bold) }
@Composable private fun AppCard(content: @Composable ColumnScope.() -> Unit) = Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp), content = content) }
@Composable private fun StatCard(value: String, label: String, color: Color, modifier: Modifier) = Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(12.dp)) { Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 11.sp) } }
@Composable private fun FamilySelector() = Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Avatar("GS", 46.dp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Gabriel Silva", color = Purple, fontWeight = FontWeight.Bold); Text("Online • 17 anos", color = Green, fontSize = 12.sp) }; Icon(Icons.Rounded.KeyboardArrowRight, null, tint = Purple) } }
@Composable private fun LearningCategory(text: String, selected: Boolean, modifier: Modifier) = Surface(modifier, color = if (selected) Purple else Color.White, shape = RoundedCornerShape(20.dp)) { Text(text, color = if (selected) Color.White else Purple, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 9.dp), fontSize = 12.sp) }
@Composable private fun LessonCard(title: String, subtitle: String, progress: Float) = Card(Modifier.fillMaxWidth().padding(vertical = 7.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(16.dp)) { Text(title, color = Purple, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet, trackColor = Color(0xFFE7E4EE), strokeCap = StrokeCap.Round) } }
@Composable private fun ActivityRow(title: String, subtitle: String, color: Color) = Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).background(color.copy(alpha=.14f), CircleShape), contentAlignment = Alignment.Center) { Box(Modifier.size(12.dp).background(color, CircleShape)) }; Spacer(Modifier.width(12.dp)); Column { Text(title, color = Purple, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 12.sp) } } }
@Composable private fun SettingRow(title: String, subtitle: String) = Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, color = Purple, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 12.sp) }; Icon(Icons.Rounded.KeyboardArrowRight, null, tint = Muted) } }

@Composable
private fun UsageChart() {
    val values = listOf(.45f, .72f, .58f, .65f, .88f, .25f, .18f)
    Canvas(Modifier.fillMaxWidth().height(140.dp)) {
        val slot = size.width / values.size
        values.forEachIndexed { index, value ->
            val barHeight = size.height * value
            drawRoundRect(color = if (index == 4) Purple else Color(0xFF8B5BD1), topLeft = Offset(slot * index + slot * .2f, size.height - barHeight), size = Size(slot * .6f, barHeight), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f))
        }
    }
}
