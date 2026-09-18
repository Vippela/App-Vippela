package com.example.vippela.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SolucaoScreen(
    onNext: () -> Unit = {}
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
            val rng = Random(200)

            val path = androidx.compose.ui.graphics.Path().apply {
                val stepX = size.width / steps
                moveTo(0f, centerY)
                var y = centerY
                for (i in 1..steps) {
                    val x = i * stepX
                    val t = x / size.width
                    val wave1 = sin(Math.toRadians(t * 360.0 * 2.0)).toFloat() * 5.dp.toPx()
                    val wave2 = sin(Math.toRadians(t * 360.0 * 5.5)).toFloat() * 2.dp.toPx()
                    val noise = (rng.nextFloat() - 0.5f) * 2.5.dp.toPx()
                    val target = centerY + wave1 + wave2 + noise
                    y += (target - y) * 0.3f
                    lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = Color(0xFF9B93C4),
                style = Stroke(
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
                    text = "04",
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "A solução",
                color = TextTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Menos tempo de tela mais tempo para o que realmente importa.",
                color = TextBody,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gráfico circular + Legenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gráfico circular de progresso
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val stroke = 12.dp.toPx()
                        val diameter = size.width - stroke
                        val topLeft = Offset(stroke / 2, stroke / 2)

                        drawArc(
                            color = Color(0xFFE8E5F0),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        val sweepAngle = 270f * 0.60f
                        drawArc(
                            color = AccentPurple,
                            startAngle = 135f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "60%",
                            color = TextTitle,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "36 minutos",
                            color = TextBody,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Legenda
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TextBody.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "hoje",
                                color = TextBody,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "3h 30min",
                                color = TextTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(AccentPurple)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Futuro",
                                color = TextBody,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "1h 40min",
                                color = AccentPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Família topa ir reduzindo o tempo de tela",
                    color = TextBody,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Indicadores + Botão
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val isActive = index == 3
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
                    onClick = onNext,
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

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SolucaoScreenPreview() {
    SolucaoScreen()
}