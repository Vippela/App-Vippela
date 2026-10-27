package br.com.vippela.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import br.com.vippela.data.DemoState
import br.com.vippela.data.FamilyMember
import br.com.vippela.data.learning.Badge
import br.com.vippela.data.learning.BadgeCatalog
import br.com.vippela.data.learning.LearningState
import br.com.vippela.data.learning.Trilha
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// Meus selos
// ---------------------------------------------------------------------------

@Composable
fun BadgesScreen(learning: LearningState, back: () -> Unit) {
    val conquistados = BadgeCatalog.todos.count { learning.seloConquistado(it.codigo) }
    Page {
        Text("Meus selos", style = MaterialTheme.typography.headlineMedium)
        Text(
            "$conquistados de ${BadgeCatalog.todos.size} selos. Cada um marca um passo — não uma competição.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        Progress(
            conquistados.toFloat() / BadgeCatalog.todos.size,
            color = Color(BadgeCatalog.PEONIA),
        )

        BadgeCatalog.todos.chunked(2).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                linha.forEach { selo ->
                    BadgeCard(
                        badge = selo,
                        conquistado = learning.seloConquistado(selo.codigo),
                        data = learning.dataSelo(selo.codigo),
                        modifier = Modifier.weight(1f),
                    )
                }
                if (linha.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Spa, null, tint = Muted, modifier = Modifier.size(20.dp))
                Text(
                    "Selos sem prazo e sem ranking. Se uma semana passar sem lição, nada é perdido: " +
                        "seu jardim continua aqui.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        SecondaryButton("Voltar para as trilhas", back)
    }
}

@Composable
private fun BadgeCard(badge: Badge, conquistado: Boolean, data: String?, modifier: Modifier) {
    Surface(
        modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (conquistado) 3.dp else 0.dp,
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Medalhao(badge, conquistado)
            Text(
                badge.nome,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = if (conquistado) Purple else Muted,
            )
            Text(
                if (conquistado) badge.descricao else badge.criterio,
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            when {
                conquistado && data != null ->
                    Text(
                        dataFormatadaCurta(data),
                        style = MaterialTheme.typography.labelSmall,
                        color = Green,
                    )
                !badge.disponivel ->
                    Text(
                        "Em breve",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                else -> Text("Ainda não", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
    }
}

private fun dataFormatadaCurta(iso: String): String =
    runCatching {
            val instant = java.time.Instant.parse(iso)
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            formato.format(Date.from(instant))
        }
        .getOrDefault("Conquistado")

// ---------------------------------------------------------------------------
// Certificado
// ---------------------------------------------------------------------------

@Composable
fun CertificateScreen(
    slug: String,
    state: DemoState,
    learning: LearningState,
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
    val concluida = learning.trilhaConcluida(trilha)
    var codigo by remember(slug) { mutableStateOf(learning.certificadoExistente(slug)) }
    LaunchedEffect(slug, concluida) {
        if (concluida && codigo == null) {
            codigo = learning.emitirCertificado(trilha)
        }
    }

    if (!concluida) {
        Page {
            Panel {
                Icon(Icons.Outlined.WorkspacePremium, null, tint = Muted, modifier = Modifier.size(40.dp))
                Text("Certificado ainda não liberado", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Conclua todos os módulos de \"${trilha.titulo}\" para emitir o certificado de participação.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                PrimaryButton("Voltar à trilha") { back() }
            }
        }
        return
    }

    val context = LocalContext.current
    val nome = state.currentName
    val data = remember(slug) {
        SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")).format(Date())
    }
    var gerando by remember { mutableStateOf(false) }

    Page {
        Text("Certificado", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Certificado de participação, sem validade profissional. Emitido pela Vippela.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2D9F0)),
            shadowElevation = 3.dp,
        ) {
            Column(
                Modifier.background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFF7FB), Color(0xFFF2ECFA), Color(0xFFEDE7F8))
                        ),
                        RoundedCornerShape(24.dp),
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "VIPPELA",
                    style = MaterialTheme.typography.labelLarge,
                    color = Purple,
                    letterSpacing = 3.sp,
                )
                Text(
                    "Certificado de participação",
                    style = MaterialTheme.typography.titleMedium,
                    color = Muted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    nome,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Purple,
                    textAlign = TextAlign.Center,
                )
                Text("concluiu a trilha", style = MaterialTheme.typography.bodyMedium, color = Muted)
                Text(
                    trilha.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = Purple,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${trilha.duracaoMin} minutos · ${trilha.listaLicoes.size} lições · ${trilha.listaModulos.size} módulos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Medalhao(BadgeCatalog.todos[0], true, 42.dp)
                    Medalhao(
                        if (trilha.slug == "contas-e-senhas") BadgeCatalog.todos[1] else BadgeCatalog.todos[2],
                        true,
                        42.dp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Emitido em $data",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Text(
                    "Código de verificação: ${codigo ?: "..."}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Purple,
                )
            }
        }

        Panel {
            Text("Como verificar", style = MaterialTheme.typography.titleMedium)
            Text(
                "Este código identifica a emissão no servidor da Vippela. " +
                    "A página pública de verificação entra na fase 3 do projeto.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Text(
                "Trilha concluída em ${learning.trilhasConcluidas()} de ${learning.trilhas.size} trilhas disponíveis.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }

        PrimaryButton("Compartilhar conquista") {
            val texto =
                "Concluí a trilha \"${trilha.titulo}\" na Vippela. " +
                    "Certificado de participação ${codigo ?: ""}."
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, texto)
                }
            context.startActivity(Intent.createChooser(intent, "Compartilhar certificado"))
        }
        SecondaryButton(if (gerando) "Gerando PDF..." else "Baixar certificado em PDF") {
            if (gerando) return@SecondaryButton
            gerando = true
            val arquivo =
                gerarCertificadoPdf(
                    context = context,
                    nome = nome,
                    trilha = trilha,
                    codigo = codigo.orEmpty(),
                    data = data,
                )
            gerando = false
            if (arquivo == null) {
                Toast.makeText(context, "Não foi possível gerar o PDF.", Toast.LENGTH_LONG).show()
            } else {
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        arquivo,
                    )
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                context.startActivity(Intent.createChooser(intent, "Compartilhar PDF"))
            }
        }
        Text(
            "O certificado fica vinculado ao seu perfil. Se o nome mudar, uma nova via é emitida.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}

private fun gerarCertificadoPdf(
    context: Context,
    nome: String,
    trilha: Trilha,
    codigo: String,
    data: String,
): File? =
    runCatching {
            val documento = PdfDocument()
            val pagina =
                documento.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = pagina.canvas
            canvas.drawColor(AndroidColor.rgb(250, 247, 252))

            val borda =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    color = AndroidColor.parseColor("#B9A7E6")
                }
            canvas.drawRoundRect(28f, 28f, 567f, 814f, 24f, 24f, borda)

            val texto =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.parseColor("#160064")
                    textAlign = Paint.Align.CENTER
                }

            texto.textSize = 16f
            texto.letterSpacing = 0.4f
            canvas.drawText("VIPPELA", 297.5f, 120f, texto)

            texto.textSize = 13f
            texto.color = AndroidColor.parseColor("#5C5085")
            canvas.drawText("CERTIFICADO DE PARTICIPAÇÃO", 297.5f, 160f, texto)

            texto.color = AndroidColor.parseColor("#160064")
            texto.textSize = 30f
            canvas.drawText(nome, 297.5f, 260f, texto)

            texto.textSize = 13f
            texto.color = AndroidColor.parseColor("#5C5085")
            canvas.drawText("concluiu a trilha", 297.5f, 300f, texto)

            texto.textSize = 20f
            texto.color = AndroidColor.parseColor("#160064")
            desenharCentralizado(canvas, trilha.titulo, 350f, 26f, texto)

            texto.textSize = 12f
            texto.color = AndroidColor.parseColor("#5C5085")
            canvas.drawText(
                "${trilha.duracaoMin} minutos · ${trilha.listaLicoes.size} lições",
                297.5f,
                430f,
                texto,
            )
            canvas.drawText("Emitido em $data", 297.5f, 470f, texto)

            texto.textSize = 12f
            texto.color = AndroidColor.parseColor("#8047BB")
            canvas.drawText("Código de verificação: $codigo", 297.5f, 700f, texto)

            texto.textSize = 9f
            texto.color = AndroidColor.parseColor("#7A7290")
            canvas.drawText(
                "Certificado de participação, sem validade profissional.",
                297.5f,
                730f,
                texto,
            )
            canvas.drawText("Equipe Vippela — Educação Digital", 297.5f, 748f, texto)

            documento.finishPage(pagina)
            val pasta = File(context.cacheDir, "certificados").apply { mkdirs() }
            val arquivo = File(pasta, "certificado-${trilha.slug}.pdf")
            arquivo.outputStream().use { documento.writeTo(it) }
            documento.close()
            arquivo
        }
        .getOrNull()

private fun desenharCentralizado(
    canvas: android.graphics.Canvas,
    texto: String,
    yInicial: Float,
    tamanho: Float,
    paint: Paint,
) {
    val palavras = texto.split(" ")
    val linhas = mutableListOf<String>()
    var atual = ""
    palavras.forEach { palavra ->
        val tentativa = if (atual.isEmpty()) palavra else "$atual $palavra"
        if (tentativa.length > 38) {
            linhas += atual
            atual = palavra
        } else {
            atual = tentativa
        }
    }
    if (atual.isNotEmpty()) linhas += atual
    linhas.take(3).forEachIndexed { indice, linha ->
        canvas.drawText(linha, 297.5f, yInicial + indice * (tamanho + 8f), paint)
    }
}

// ---------------------------------------------------------------------------
// Progresso da família
// ---------------------------------------------------------------------------

@Composable
fun FamilyProgressScreen(state: DemoState, learning: LearningState, back: () -> Unit) {
    val visibilidade = remember { mutableStateMapOf<Int, String>() }
    Page {
        Text("Progresso da família", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Acompanhar serve para puxar assunto, não para vigiar. Cada pessoa escolhe o que compartilha.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )

        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.currentName, 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.currentName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Seu progresso · ${learning.trilhasConcluidas()} trilha(s) concluída(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Icon(Icons.Outlined.Lock, null, tint = Muted, modifier = Modifier.size(18.dp))
            }
            Text(
                "Só você vê suas respostas de quiz e suas tentativas. Nem o responsável tem acesso.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }

        Heading("Quem está aprendendo")
        state.members.forEach { membro ->
            FamiliaCard(
                membro = membro,
                visibilidade = visibilidade[membro.id] ?: if (membro.age < 13) "Resumo" else "Privado",
                onVisibilidade = { visibilidade[membro.id] = it },
            )
        }

        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Shield, null, tint = Violet, modifier = Modifier.size(22.dp))
                Column {
                    Text("O que nunca é compartilhado", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Respostas de quiz, erros, tempo por lição, tentativas reprovadas e histórico. " +
                            "O responsável vê conclusão e selos — não desempenho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
        }
        Text(
            "Os dados dos familiares nesta tela são demonstrativos. A leitura real do progresso " +
                "será liberada pelo servidor somente com consentimento de cada perfil.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        SecondaryButton("Voltar", back)
    }
}

@Composable
private fun FamiliaCard(
    membro: FamilyMember,
    visibilidade: String,
    onVisibilidade: (String) -> Unit,
) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(membro.name, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(membro.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${membro.age} anos · ${if (membro.age < 13) "criança" else if (membro.age < 18) "adolescente" else "adulto"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Etiqueta("Demonstração", Violet)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Resumo", "Detalhado", "Privado").forEach { opcao ->
                Surface(
                    Modifier.clickable { onVisibilidade(opcao) },
                    shape = RoundedCornerShape(30.dp),
                    color = if (visibilidade == opcao) Purple else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        opcao,
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (visibilidade == opcao) Color.White else Purple,
                    )
                }
            }
        }
        Text(
            when (visibilidade) {
                "Detalhado" -> "Você vê o módulo atual, o % da trilha e as atividades em família."
                "Privado" -> "Você vê apenas que a pessoa participa da trilha."
                else -> "Você vê trilhas concluídas, selos e a última atividade em data aproximada."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        if (visibilidade != "Privado") {
            Progress(if (membro.id == 1) 0.5f else if (membro.id == 2) 0.25f else 0.75f)
            Text(
                when (membro.id) {
                    1 -> "1 trilha concluída · 3 selos"
                    2 -> "Em andamento · 4 de 12 lições"
                    else -> "1 trilha concluída · 4 selos"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
    }
}
