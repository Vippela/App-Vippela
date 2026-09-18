package com.example.vippela.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

data class AppIcon(
    val nome: String,
    val cor: Color
)

private val apps = listOf(
    AppIcon("Instagram", Color(0xFFE1306C)),
    AppIcon("Pinterest", Color(0xFFE60023)),
    AppIcon("Reddit", Color(0xFFFF4500)),
    AppIcon("Netflix", Color(0xFFE50914)),
    AppIcon("Spotify", Color(0xFF1DB954)),
    AppIcon("Telegram", Color(0xFF2AABEE)),
    AppIcon("X", Color(0xFF111111)),
    AppIcon("Discord", Color(0xFF5865F2)),
    AppIcon("TikTok", Color(0xFF010101)),
    AppIcon("YouTube", Color(0xFFFF0000)),
    AppIcon("Twitch", Color(0xFF9146FF)),
    AppIcon("Shopee", Color(0xFFEE4D2D))
)

@Composable
fun ProblemaScreen(
    onProximo: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLavender)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
        ) {
            val strokeWidth = 1.5.dp.toPx()
            val dashLen = 6.dp.toPx()
            val gapLen = 4.dp.toPx()
            val pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLen, gapLen), 0f
            )
            val centerY = size.height / 2
            val steps = 300
            val rng = Random(99)

            val path = androidx.compose.ui.graphics.Path().apply {
                val stepX = size.width / steps
                moveTo(0f, centerY)
                var y = centerY
                for (i in 1..steps) {
                    val x = i * stepX
                    val t = x / size.width
                    val wave1 = sin(Math.toRadians(t * 360.0 * 3.0)).toFloat() * 3.5.dp.toPx()
                    val wave2 = sin(Math.toRadians(t * 360.0 * 7.5)).toFloat() * 1.2.dp.toPx()
                    val noise = (rng.nextFloat() - 0.5f) * 3.5.dp.toPx()
                    val target = centerY + wave1 + wave2 + noise
                    y += (target - y) * 0.35f
                    lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = Color(0xFF9B93C4),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    pathEffect = pathEffect,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StepChipPurple)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "03",
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "O problema",
                color = TextTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "O excesso de tempo de tela está impactando o que mais importa",
                color = TextBody,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                apps.chunked(4).forEach { linha ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        linha.forEach { app -> IconeApp(app) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val isActive = index == 2
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isActive) 20.dp else 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isActive) AccentPurple else IndicatorInactive)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onProximo,
                    modifier = Modifier
                        .widthIn(min = 140.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
                ) {
                    Text(
                        text = "Próximo",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IconeApp(app: AppIcon) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(app.cor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = app.nome.first().toString(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ProblemaScreenPreview() {
    ProblemaScreen()
}