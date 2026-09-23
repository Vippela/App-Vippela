package br.com.vippela.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import br.com.vippela.ui.theme.*

@Composable
fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun Heading(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = Muted)
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick,
        Modifier.fillMaxWidth().heightIn(min = 50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(text)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick,
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Text(text)
    }
}

@Composable
fun CircleIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(
        onClick,
        Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
    ) {
        Icon(icon, label, tint = Muted, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun Header(title: String, back: (() -> Unit)?, notify: () -> Unit, settings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (back != null) CircleIcon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar", back)
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
        CircleIcon(Icons.Outlined.Notifications, "Notificações", notify)
        if (back == null) CircleIcon(Icons.Outlined.Settings, "Configurações", settings)
    }
}

@Composable
fun Avatar(name: String, size: Dp = 52.dp, photo: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bitmap by
        androidx.compose.runtime.produceState<androidx.compose.ui.graphics.ImageBitmap?>(
            null,
            photo,
        ) {
            value =
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    photo?.let {
                        runCatching {
                                val uri = android.net.Uri.parse(it)
                                val options =
                                    android.graphics.BitmapFactory.Options().apply {
                                        inJustDecodeBounds = true
                                    }
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    android.graphics.BitmapFactory.decodeStream(
                                        input,
                                        null,
                                        options,
                                    )
                                }
                                options.inSampleSize =
                                    maxOf(options.outWidth, options.outHeight)
                                        .div(512)
                                        .coerceAtLeast(1)
                                options.inJustDecodeBounds = false
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    android.graphics.BitmapFactory.decodeStream(
                                            input,
                                            null,
                                            options,
                                        )
                                        ?.asImageBitmap()
                                }
                            }
                            .getOrNull()
                    }
                }
        }
    Box(
        Modifier.size(size).clip(CircleShape).background(Color(0xFFECE3F7)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null)
            Image(
                bitmap!!,
                "Foto de perfil",
                Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        else
            Text(
                name.split(" ").take(2).mapNotNull { it.firstOrNull() }.joinToString(""),
                style = MaterialTheme.typography.titleMedium,
                color = Violet,
            )
    }
}

@Composable
fun RiskBadge(risk: String) {
    val color =
        when (risk) {
            "Baixo" -> Green
            "Alto" -> Red
            else -> Amber
        }
    Surface(color = color.copy(alpha = .10f), shape = RoundedCornerShape(30.dp)) {
        Text(
            "● $risk",
            Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            color = color,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun MenuRow(title: String, subtitle: String, icon: ImageVector, action: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = action)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            icon,
            null,
            tint = Violet,
            modifier =
                Modifier.size(36.dp)
                    .background(Color(0xFFF0EAF7), RoundedCornerShape(9.dp))
                    .padding(6.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
    }
}

@Composable
fun Tabs(items: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        items.forEachIndexed { index, label ->
            Box(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (index == selected) Purple else Color.Transparent)
                    .clickable { onSelect(index) }
                    .semantics { this.selected = index == selected }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color =
                        if (index == selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun Progress(value: Float, color: Color = Violet) {
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
        color = color,
        trackColor = Color(0xFFEAE5F3),
    )
}

@Composable
fun Ring(value: Float, label: String, color: Color = Violet, size: Dp = 90.dp) {
    Box(
        Modifier.size(size).semantics {
            contentDescription = "$label, ${(value * 100).toInt()} por cento"
        },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = Color(0xFFEAE5F3),
            strokeWidth = 6.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(value * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
            if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AppSymbol(name: String) {
    val resource =
        when (name) {
            "YouTube" -> br.com.vippela.R.drawable.app_youtube
            "Instagram" -> br.com.vippela.R.drawable.app_instagram
            "TikTok" -> br.com.vippela.R.drawable.app_tiktok
            "Khan Academy" -> br.com.vippela.R.drawable.app_khanacademy
            else -> null
        }
    Box(
        Modifier.size(44.dp).background(Color.White, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (resource != null)
            Image(
                androidx.compose.ui.res.painterResource(resource),
                name,
                Modifier.size(28.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            )
        else Icon(Icons.Outlined.Apps, name, tint = Purple)
    }
}

@Composable
fun Brand() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            androidx.compose.ui.res.painterResource(br.com.vippela.R.drawable.vippela_logo),
            "Vippela",
            Modifier.size(160.dp, 128.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            "Feito de pessoas para Pessoas",
            style = MaterialTheme.typography.labelMedium,
            color = Muted,
        )
    }
}

fun duration(minutes: Int) = "${minutes / 60}h ${minutes % 60}min"

@Composable
fun PillTabs(items: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { index, label ->
            Box(
                Modifier.weight(1f).heightIn(min = 48.dp).clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = if (index == selected) Color(0xFFFFE288) else Purple,
                ) {
                    Text(
                        label,
                        Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (index == selected) Purple else Color.White,
                    )
                }
            }
        }
    }
}
