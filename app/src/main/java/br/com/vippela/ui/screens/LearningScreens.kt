package br.com.vippela.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vippela.data.DemoState
import br.com.vippela.data.learning.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------------------
// Apoio visual
// ---------------------------------------------------------------------------

private fun iconeTrilha(nome: String): ImageVector =
    when (nome) {
        "chave" -> Icons.Outlined.VpnKey
        "golpe" -> Icons.Outlined.Phishing
        "privacidade", "cadeado" -> Icons.Outlined.Lock
        "telas" -> Icons.Outlined.HourglassBottom
        "bemestar", "coracao" -> Icons.Outlined.Spa
        "familia" -> Icons.Outlined.FamilyRestroom
        "olho" -> Icons.Outlined.Visibility
        "maos" -> Icons.Outlined.People
        "sol" -> Icons.Outlined.WbSunny
        "jardim" -> Icons.Outlined.LocalFlorist
        "petala", "folha" -> Icons.Outlined.Eco
        else -> Icons.Outlined.School
    }

private fun corDoPilar(pilar: String): Color =
    when (pilar) {
        "Saúde" -> Green
        "Bem-estar" -> Color(0xFFB4728E)
        else -> Violet
    }

private fun rotuloTipo(tipo: String): String =
    when (tipo) {
        "TXT" -> "Leitura"
        "VID" -> "Vídeo"
        "SIM" -> "Simulação"
        "CHK" -> "Checklist"
        "QUIZ" -> "Quiz"
        "FAM" -> "Em família"
        "AUD" -> "Áudio"
        else -> "Lição"
    }

private fun iconeTipo(tipo: String): ImageVector =
    when (tipo) {
        "SIM" -> Icons.Outlined.Phishing
        "CHK" -> Icons.Outlined.Checklist
        "FAM" -> Icons.Outlined.People
        "VID" -> Icons.Outlined.PlayCircleOutline
        "AUD" -> Icons.Outlined.VolumeUp
        else -> Icons.Outlined.MenuBook
    }

private fun tempoLicao(segundos: Int): String =
    if (segundos < 60) "$segundos s" else "${maxOf(1, segundos / 60)} min"

@Composable
fun Etiqueta(texto: String, cor: Color) {
    Surface(shape = RoundedCornerShape(30.dp), color = cor.copy(alpha = 0.14f)) {
        Text(
            texto,
            Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Purple,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Medalhão de pétalas desenhado em Compose. Sem imagens: escala bem e
 * acompanha o tema. O número de pétalas representa o nível do selo.
 */
@Composable
fun Medalhao(
    badge: Badge,
    conquistado: Boolean,
    size: Dp = 76.dp,
) {
    val cor = if (conquistado) Color(badge.cor) else Color(0xFFCFC8DD)
    val miolo = if (conquistado) Purple else Color(0xFFE4DFEE)
    Box(
        Modifier.size(size).semantics {
            contentDescription =
                "${badge.nome}, ${if (conquistado) "conquistado" else "ainda não conquistado"}"
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val raio = this.size.minDimension / 2f
            val centro = Offset(this.size.width / 2f, this.size.height / 2f)
            repeat(badge.tier.petalas) { indice ->
                val angulo =
                    (-90f + indice * 360f / badge.tier.petalas) * (Math.PI / 180f).toFloat()
                drawCircle(
                    color = cor.copy(alpha = if (conquistado) 0.55f else 0.35f),
                    radius = raio * 0.24f,
                    center =
                        Offset(
                            centro.x + cos(angulo) * raio * 0.66f,
                            centro.y + sin(angulo) * raio * 0.66f,
                        ),
                )
            }
            drawCircle(cor.copy(alpha = 0.20f), radius = raio * 0.60f, center = centro)
            drawCircle(miolo, radius = raio * 0.46f, center = centro)
        }
        Icon(
            iconeTrilha(badge.icone),
            null,
            Modifier.size(size * 0.28f),
            tint = if (conquistado) Color.White else Color(0xFF9A92AC),
        )
    }
}

// ---------------------------------------------------------------------------
// Home da trilha
// ---------------------------------------------------------------------------

@Composable
fun LearningHomeScreen(state: DemoState, learning: LearningState, go: (String) -> Unit) {
    val continuar = learning.continuar()
    Page {
        Text(
            if (state.isParent) "Aprender em família" else "Sua trilha",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            "Lições curtas para proteger, cuidar da saúde e viver melhor com a tecnologia.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )

        if (continuar != null) {
            val (trilha, licao) = continuar
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("CONTINUE DE ONDE PAROU", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Text(trilha.titulo, style = MaterialTheme.typography.titleMedium)
                    }
                    Etiqueta(trilha.pilar, corDoPilar(trilha.pilar))
                }
                Text(licao.titulo, style = MaterialTheme.typography.bodyMedium, color = Muted)
                Progress(learning.progresso(trilha))
                Text(
                    "${learning.licoesConcluidas(trilha)} de ${trilha.listaLicoes.size} lições · " +
                        "${licao.duracaoMin} min agora",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                PrimaryButton(if (learning.licoesConcluidas(trilha) == 0) "Começar" else "Continuar") {
                    go("lesson/${licao.id}")
                }
            }
        }

        DesafioDaSemana(learning)

        Heading("Trilhas")
        learning.trilhas.forEach { trilha ->
            TrilhaCard(trilha, learning) { go("track/${trilha.slug}") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                SecondaryButton("Meus selos") { go("badges") }
            }
            Box(Modifier.weight(1f)) {
                SecondaryButton("Família") { go("familia-progresso") }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CloudOff, null, tint = Muted, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "As duas trilhas já estão no aparelho. Você pode estudar sem internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

@Composable
private fun DesafioDaSemana(learning: LearningState) {
    val feito = learning.desafioDaSemanaFeito()
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF6DCE6),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.People, null, tint = Color(0xFF8C4A63), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "DESAFIO DA SEMANA · 15 MIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8C4A63),
                )
            }
            Text(
                "Conversem sobre um golpe que alguém da casa já recebeu.",
                style = MaterialTheme.typography.titleMedium,
                color = Purple,
            )
            Text(
                "Cada um conta o que aconteceu e o que faria diferente hoje. Sem julgamento.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6A4C57),
            )
            if (feito) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Conversa registrada nesta semana. Obrigado por cuidar da família.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green,
                    )
                }
            } else {
                TextButton(onClick = { learning.marcarDesafioDaSemana() }) {
                    Text("Já conversamos", color = Purple, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = Purple, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TrilhaCard(trilha: Trilha, learning: LearningState, onClick: () -> Unit) {
    val progresso = learning.progresso(trilha)
    val concluida = learning.trilhaConcluida(trilha)
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(54.dp).background(corDoPilar(trilha.pilar), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconeTrilha(trilha.icone), null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(trilha.titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${trilha.nivel} · ${trilha.duracaoMin} min · ${trilha.listaLicoes.size} lições",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                if (progresso > 0f) Progress(progresso)
                Text(
                    when {
                        concluida -> "Concluída · certificado disponível"
                        progresso > 0f -> "Em andamento · ${(progresso * 100).toInt()}%"
                        else -> "Começar agora"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (concluida) Green else if (progresso > 0f) Violet else Muted,
                )
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = Muted)
        }
    }
}

// ---------------------------------------------------------------------------
// Detalhe da trilha
// ---------------------------------------------------------------------------

@Composable
fun TrackDetailScreen(
    slug: String,
    learning: LearningState,
    go: (String) -> Unit,
    back: () -> Unit,
) {
    val trilha = learning.trilha(slug)
    if (trilha == null) {
        Page {
            Text("Trilha não encontrada.", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Voltar") { back() }
        }
        return
    }
    val progresso = learning.progresso(trilha)
    val concluida = learning.trilhaConcluida(trilha)
    Page {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(50.dp).background(corDoPilar(trilha.pilar), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(iconeTrilha(trilha.icone), null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Etiqueta(trilha.pilar, corDoPilar(trilha.pilar))
                    Spacer(Modifier.height(6.dp))
                    Text(trilha.nivel, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                if (concluida) Icon(Icons.Outlined.Verified, null, tint = Green)
            }
            Text(trilha.titulo, style = MaterialTheme.typography.titleLarge)
            Text(trilha.objetivo, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta("${trilha.duracaoMin} min", Violet)
                Etiqueta("${trilha.listaLicoes.size} lições", Violet)
                Etiqueta("${trilha.listaModulos.size} módulos", Violet)
            }
            if (progresso > 0f) {
                Progress(progresso)
                Text(
                    "${(progresso * 100).toInt()}% concluído",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }

        val faltando =
            trilha.listaPrerequisitos.mapNotNull { learning.trilha(it) }
                .filterNot { learning.trilhaConcluida(it) }
        if (faltando.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "Recomendado depois de: ${faltando.joinToString { it.titulo }}. " +
                        "Você pode seguir mesmo assim.",
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }

        trilha.listaModulos.forEachIndexed { indice, modulo ->
            ModuloCard(trilha, modulo, indice, learning, go)
        }

        if (concluida) {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WorkspacePremium, null, tint = Color(0xFF8C4A63))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Trilha concluída", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Seu certificado de participação está pronto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
                PrimaryButton("Ver certificado") { go("certificado/${trilha.slug}") }
            }
        } else {
            learning.proximaLicao(trilha)?.let { licao ->
                PrimaryButton(
                    if (learning.licoesConcluidas(trilha) == 0) "Começar pela primeira lição"
                    else "Continuar: ${licao.titulo}"
                ) {
                    go("lesson/${licao.id}")
                }
            }
        }
    }
}

@Composable
private fun ModuloCard(
    trilha: Trilha,
    modulo: Modulo,
    indice: Int,
    learning: LearningState,
    go: (String) -> Unit,
) {
    val liberado = learning.moduloLiberado(trilha, indice)
    val aprovado = learning.moduloAprovado(modulo)
    val feitas = modulo.listaLicoes.count { learning.licaoConcluida(it.id) }
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (liberado) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                null,
                tint = if (liberado) Violet else Muted,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("MÓDULO ${indice + 1}", style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(modulo.titulo, style = MaterialTheme.typography.titleMedium)
            }
            if (aprovado) Icon(Icons.Outlined.CheckCircle, null, tint = Green)
        }
        Text(
            "$feitas de ${modulo.listaLicoes.size} lições · ${modulo.duracaoMin} min",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )

        if (!liberado) {
            Text(
                "Conclua o módulo ${indice} para abrir. Você pode espiar a primeira lição.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            modulo.listaLicoes.firstOrNull()?.let { primeira ->
                LessonRow(primeira, learning, espiar = true) { go("lesson/${primeira.id}") }
            }
            return@Panel
        }

        modulo.listaLicoes.forEach { licao ->
            LessonRow(licao, learning) { go("lesson/${licao.id}") }
        }

        val habilitado = feitas == modulo.listaLicoes.size
        OutlinedButton(
            onClick = { go("quiz/modulo:${modulo.id}") },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Icon(Icons.Outlined.Quiz, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (aprovado) "Refazer checagem do módulo"
                else if (habilitado) "Fazer checagem do módulo"
                else "Conclua as lições para liberar"
            )
        }
        if (aprovado) {
            Text(
                "Aprovado · ${learning.acertosModulo(modulo)} de ${modulo.listaLicoes.size}",
                style = MaterialTheme.typography.bodySmall,
                color = Green,
            )
        }
    }
}

@Composable
private fun LessonRow(
    licao: Licao,
    learning: LearningState,
    espiar: Boolean = false,
    onClick: () -> Unit,
) {
    val feita = learning.licaoConcluida(licao.id)
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(34.dp).background(
                when {
                    feita -> Green.copy(alpha = 0.14f)
                    espiar -> Color(0xFFEDE7F8)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (feita) Icons.Outlined.Check else iconeTipo(licao.tipo),
                null,
                tint = if (feita) Green else Violet,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(licao.titulo, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${rotuloTipo(licao.tipo)} · ${tempoLicao(licao.duracaoSeg)}" +
                    if (espiar) " · prévia" else "",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        if (espiar) Etiqueta("Prévia", Violet) else Icon(
            Icons.AutoMirrored.Outlined.ArrowForward,
            null,
            tint = Muted,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Lição
// ---------------------------------------------------------------------------

@Composable
fun LessonScreen(
    lessonId: String,
    learning: LearningState,
    go: (String) -> Unit,
    back: () -> Unit,
) {
    val licao = learning.licao(lessonId)
    val contexto = learning.moduloDe(lessonId)
    if (licao == null || contexto == null) {
        Page {
            Text("Lição não encontrada.", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Voltar") { back() }
        }
        return
    }
    val (trilha, modulo) = contexto
    val context = LocalContext.current
    var fonte by rememberSaveable(lessonId) { mutableIntStateOf(17) }
    var falando by remember { mutableStateOf(false) }
    var motor by remember { mutableStateOf<TextToSpeech?>(null) }
    var pronto by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine =
            TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    engine?.language = Locale("pt", "BR")
                    pronto = true
                }
            }
        motor = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
            falando = false
        }
    }

    val narracao = remember(lessonId) { montarNarracao(licao) }
    val marcados = remember(lessonId) { mutableStateOf(setOf<String>()) }
    var escolha by remember(lessonId) { mutableStateOf<Int?>(null) }

    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Etiqueta(rotuloTipo(licao.tipo), corDoPilar(trilha.pilar))
            Spacer(Modifier.weight(1f))
            Text(
                "${trilha.titulo.substringBefore(":")} · Módulo ${trilha.listaModulos.indexOf(modulo) + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        Text(licao.titulo, style = MaterialTheme.typography.headlineMedium)
        Text(licao.resumo, style = MaterialTheme.typography.bodyMedium, color = Muted)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistButton(
                texto = if (falando) "Parar narração" else if (pronto) "Ouvir lição" else "Preparando voz...",
                icone = if (falando) Icons.Outlined.StopCircle else Icons.Outlined.VolumeUp,
                ativo = pronto,
            ) {
                val engine = motor
                if (engine != null) {
                    if (falando) {
                        engine.stop()
                        falando = false
                    } else {
                        engine.speak(narracao, TextToSpeech.QUEUE_FLUSH, null, "vippela-$lessonId")
                        falando = true
                    }
                }
            }
            AssistButton(texto = "A+", icone = Icons.Outlined.FormatSize, ativo = true) {
                fonte = when (fonte) {
                    17 -> 19
                    19 -> 21
                    else -> 17
                }
            }
        }

        licao.listaBlocos.forEachIndexed { indice, bloco ->
            when (bloco.tipo) {
                "texto" -> {
                    Text(bloco.titulo.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        bloco.texto.orEmpty(),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = fonte.sp,
                                lineHeight = (fonte + 9).sp,
                            ),
                    )
                }
                "destaque" -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF6DCE6),
                    ) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Outlined.Eco, null, tint = Color(0xFF8C4A63), modifier = Modifier.size(22.dp))
                            Text(
                                bloco.texto.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Purple,
                            )
                        }
                    }
                }
                "checklist" -> {
                    Panel {
                        Text(bloco.titulo.orEmpty(), style = MaterialTheme.typography.titleMedium)
                        bloco.listaItens.forEachIndexed { itemIndice, item ->
                            val chave = "$indice:$itemIndice"
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        marcados.value =
                                            if (chave in marcados.value) marcados.value - chave
                                            else marcados.value + chave
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = chave in marcados.value,
                                    onCheckedChange = {
                                        marcados.value =
                                            if (chave in marcados.value) marcados.value - chave
                                            else marcados.value + chave
                                    },
                                )
                                Text(
                                    item,
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = fonte.sp),
                                )
                            }
                        }
                    }
                }
                "simulacao" -> {
                    Panel {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Phishing, null, tint = Color(0xFF8C4A63))
                            Spacer(Modifier.width(8.dp))
                            Text("O que você faz?", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            bloco.texto.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fonte.sp),
                        )
                        bloco.listaOpcoes.forEachIndexed { opcaoIndice, opcao ->
                            val escolhida = escolha == opcaoIndice
                            Surface(
                                Modifier.fillMaxWidth().clickable {
                                    escolha = if (escolhida) null else opcaoIndice
                                },
                                shape = RoundedCornerShape(14.dp),
                                color =
                                    if (escolhida) {
                                        if (opcao.correta) Green.copy(alpha = 0.10f)
                                        else Color(0xFFFBE3DF)
                                    } else MaterialTheme.colorScheme.surfaceVariant,
                                border =
                                    if (escolhida) {
                                        BorderStroke(1.dp, if (opcao.correta) Green else Red)
                                    } else null,
                            ) {
                                Text(
                                    opcao.texto,
                                    Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = fonte.sp),
                                )
                            }
                            if (escolhida) {
                                Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                    if (opcao.correta) {
                                        Text(
                                            "Boa escolha",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Green,
                                        )
                                    }
                                    Text(
                                        opcao.resultado,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Muted,
                                    )
                                    TextButton(onClick = { escolha = null }) { Text("Tentar outra escolha") }
                                }
                            }
                        }
                    }
                }
                "familia" -> {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEAF4EE)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.People, null, tint = Green, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "ATIVIDADE EM FAMÍLIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Green,
                                )
                            }
                            Text(
                                bloco.texto.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = fonte.sp),
                            )
                            Text(
                                "Conte com alguém: vale ligar, sentar junto ou mandar uma mensagem.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }
        }

        Text(
            "Ao terminar, você faz 3 perguntas rápidas. Pode tentar quantas vezes quiser.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        PrimaryButton(
            if (learning.licaoConcluida(licao.id)) "Refazer o quiz" else "Fazer o quiz"
        ) {
            go("quiz/licao:${licao.id}")
        }
        if (learning.licaoConcluida(licao.id)) {
            Text(
                "Lição concluída. Você pode rever quando quiser.",
                style = MaterialTheme.typography.bodySmall,
                color = Green,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AssistButton(
    texto: String,
    icone: ImageVector,
    ativo: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = ativo,
        shape = RoundedCornerShape(30.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(icone, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}

private fun montarNarracao(licao: Licao): String =
    buildString {
        append(licao.titulo).append(". ")
        append(licao.resumo).append(" ")
        licao.listaBlocos.forEach { bloco ->
            bloco.titulo?.let { append(it).append(". ") }
            bloco.texto?.let { append(it).append(" ") }
            bloco.listaItens.forEach { append(it).append(". ") }
            bloco.listaOpcoes.forEach { append(it.texto).append(". ") }
        }
        append("Ao terminar, faça o quiz de três perguntas.")
    }

// ---------------------------------------------------------------------------
// Quiz
// ---------------------------------------------------------------------------

private fun montarAlvo(learning: LearningState, chave: String): AlvoQuiz? {
    if (chave.startsWith("licao:")) {
        val id = chave.removePrefix("licao:")
        val licao = learning.licao(id) ?: return null
        val (trilha, modulo) = learning.moduloDe(id) ?: return null
        return AlvoQuiz(AlvoQuiz.TipoAlvo.LICAO, trilha, modulo, licao)
    }
    val id = chave.removePrefix("modulo:")
    learning.trilhas.forEach { trilha ->
        trilha.listaModulos.firstOrNull { it.id == id }?.let { modulo ->
            return AlvoQuiz(AlvoQuiz.TipoAlvo.MODULO, trilha, modulo)
        }
    }
    return null
}

@Composable
fun QuizScreen(
    alvoKey: String,
    learning: LearningState,
    go: (String) -> Unit,
    back: () -> Unit,
) {
    val alvo = remember(alvoKey) { montarAlvo(learning, alvoKey) }
    if (alvo == null || alvo.perguntas.isEmpty()) {
        Page {
            Text("Quiz indisponível.", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Voltar") { back() }
        }
        return
    }
    val perguntas = alvo.perguntas
    var indice by remember(alvoKey) { mutableIntStateOf(0) }
    var escolhida by remember(alvoKey, indice) { mutableIntStateOf(-1) }
    var confirmada by remember(alvoKey, indice) { mutableStateOf(false) }
    var acertos by remember(alvoKey) { mutableIntStateOf(0) }
    val revisao = remember(alvoKey) { mutableStateListOf<RevisaoResposta>() }
    var finalizado by remember(alvoKey) { mutableStateOf(false) }

    val pergunta = perguntas.getOrNull(indice) ?: perguntas.first()
    val correta = pergunta.correta.coerceIn(0, maxOf(0, pergunta.listaAlternativas.lastIndex))
    val ultima = indice == perguntas.lastIndex

    fun finalizar() {
        if (finalizado) return
        finalizado = true
        val total = perguntas.size
        val aprovado = acertos >= alvo.acertosMinimos
        val selos =
            when (alvo.tipo) {
                AlvoQuiz.TipoAlvo.LICAO ->
                    alvo.licao?.let { licao ->
                        learning.concluirLicao(licao, acertos, total, aprovado)
                    }.orEmpty()
                AlvoQuiz.TipoAlvo.MODULO -> learning.registrarModulo(alvo.modulo, acertos)
            }
        val continuar =
            when (alvo.tipo) {
                AlvoQuiz.TipoAlvo.LICAO -> {
                    val posicao = alvo.modulo.listaLicoes.indexOfFirst { it.id == alvo.licao?.id }
                    alvo.modulo.listaLicoes.getOrNull(posicao + 1)?.let { "lesson/${it.id}" }
                        ?: "track/${alvo.trilha.slug}"
                }
                AlvoQuiz.TipoAlvo.MODULO -> {
                    val indiceModulo = alvo.trilha.listaModulos.indexOfFirst { it.id == alvo.modulo.id }
                    val proximo = alvo.trilha.listaModulos.getOrNull(indiceModulo + 1)
                    when {
                        learning.trilhaConcluida(alvo.trilha) -> "certificado/${alvo.trilha.slug}"
                        aprovado && proximo != null ->
                            proximo.listaLicoes.firstOrNull()?.let { "lesson/${it.id}" }
                                ?: "track/${alvo.trilha.slug}"
                        else -> "track/${alvo.trilha.slug}"
                    }
                }
            }
        learning.publicarResultado(
            ResultadoQuiz(
                titulo = alvo.titulo,
                alvo = alvoKey,
                acertos = acertos,
                total = total,
                aprovado = aprovado,
                voltarPara = "track/${alvo.trilha.slug}",
                continuarPara = continuar,
                revisao = revisao.toList(),
                selos = selos,
            )
        )
        go("resultado")
    }

    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                alvo.titulo,
                style = MaterialTheme.typography.labelLarge,
                color = Muted,
                modifier = Modifier.weight(1f),
            )
            Etiqueta("${indice + 1} de ${perguntas.size}", corDoPilar(alvo.trilha.pilar))
        }
        Progress((indice + 1).toFloat() / perguntas.size)
        Text(pergunta.enunciado, style = MaterialTheme.typography.titleLarge)

        pergunta.listaAlternativas.forEachIndexed { alternativaIndice, alternativa ->
            val selecionada = escolhida == alternativaIndice
            val certa = alternativaIndice == correta
            val corFundo =
                when {
                    !confirmada -> if (selecionada) Color(0xFFEDE7F8) else MaterialTheme.colorScheme.surface
                    certa -> Green.copy(alpha = 0.12f)
                    selecionada -> Color(0xFFFBE3DF)
                    else -> MaterialTheme.colorScheme.surface
                }
            Surface(
                Modifier.fillMaxWidth().clickable(enabled = !confirmada) {
                    escolhida = alternativaIndice
                },
                shape = RoundedCornerShape(14.dp),
                color = corFundo,
                border =
                    when {
                        confirmada && certa -> BorderStroke(1.dp, Green)
                        confirmada && selecionada -> BorderStroke(1.dp, Red)
                        selecionada -> BorderStroke(1.dp, Violet)
                        else -> null
                    },
                shadowElevation = if (selecionada || (confirmada && certa)) 0.dp else 1.dp,
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selecionada,
                        onClick = { if (!confirmada) escolhida = alternativaIndice },
                        enabled = !confirmada,
                    )
                    Text(
                        alternativa,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (confirmada && certa) {
                        Icon(Icons.Outlined.CheckCircle, "Resposta correta", tint = Green)
                    }
                }
            }
        }

        if (confirmada) {
            val acertou = escolhida == correta
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (acertou) Color(0xFFEAF4EE) else Color(0xFFFFF4E5),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (acertou) "Isso mesmo" else "Vale revisar",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (acertou) Green else Amber,
                    )
                    Text(
                        if (acertou) pergunta.feedbackOk else pergunta.feedbackErro,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (!acertou) {
                        Text(
                            "Resposta correta: ${pergunta.listaAlternativas.getOrNull(correta).orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            }
        }

        PrimaryButton(
            text =
                when {
                    !confirmada -> "Conferir resposta"
                    ultima -> "Ver resultado"
                    else -> "Próxima pergunta"
                },
            enabled = escolhida >= 0,
        ) {
            if (!confirmada) {
                confirmada = true
                if (escolhida == correta) {
                    acertos += 1
                }
                revisao +=
                    RevisaoResposta(
                        enunciado = pergunta.enunciado,
                        escolhida = pergunta.listaAlternativas.getOrNull(escolhida).orEmpty(),
                        correta = pergunta.listaAlternativas.getOrNull(correta).orEmpty(),
                        acertou = escolhida == correta,
                        feedback =
                            if (escolhida == correta) pergunta.feedbackOk else pergunta.feedbackErro,
                    )
            } else if (ultima) {
                finalizar()
            } else {
                indice += 1
            }
        }
        if (alvo.tipo == AlvoQuiz.TipoAlvo.LICAO) {
            TextButton(onClick = back, modifier = Modifier.fillMaxWidth()) {
                Text("Rever a lição", color = Muted)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Resultado
// ---------------------------------------------------------------------------

@Composable
fun ResultScreen(learning: LearningState, go: (String) -> Unit, back: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = back)
    val resultado = learning.ultimoResultado
    if (resultado == null) {
        Page {
            Text("Nenhum resultado recente.", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Ir para as trilhas") { go("learn") }
        }
        return
    }
    Page {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Ring(
                value = resultado.percentual / 100f,
                label = "acertos",
                color = if (resultado.aprovado) Green else Amber,
                size = 120.dp,
            )
            Text(
                if (resultado.aprovado) "Muito bem!" else "Quase lá",
                style = MaterialTheme.typography.headlineMedium,
                color = if (resultado.aprovado) Green else Amber,
            )
            Text(
                "${resultado.acertos} de ${resultado.total} · ${resultado.percentual}%",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (resultado.aprovado) {
                    "Você pode seguir para o próximo passo."
                } else {
                    "Sem problema: releia e tente de novo. Não há limite de tentativas."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
            )
        }

        if (resultado.selos.isNotEmpty()) {
            Panel {
                Text("Selos que você ganhou", style = MaterialTheme.typography.titleMedium)
                resultado.selos.forEach { selo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Medalhao(selo, true, 56.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(selo.nome, style = MaterialTheme.typography.titleMedium)
                            Text(selo.descricao, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
            }
        }

        Heading("Revisão")
        resultado.revisao.forEachIndexed { indice, resposta ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (resposta.acertou) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                            null,
                            tint = if (resposta.acertou) Green else Red,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Pergunta ${indice + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                    Text(resposta.enunciado, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Sua resposta: ${resposta.escolhida}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (resposta.acertou) Green else Red,
                    )
                    if (!resposta.acertou) {
                        Text(
                            "Correta: ${resposta.correta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green,
                        )
                    }
                    Text(resposta.feedback, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        }

        if (resultado.aprovado) {
            PrimaryButton(
                if (resultado.continuarPara?.startsWith("certificado") == true) "Ver certificado"
                else "Continuar"
            ) {
                val destino = resultado.continuarPara ?: resultado.voltarPara
                go(destino)
            }
        } else {
            PrimaryButton("Revisar e tentar de novo") { go("quiz/${resultado.alvo}") }
        }
        SecondaryButton("Voltar à trilha") { back() }
    }
}
