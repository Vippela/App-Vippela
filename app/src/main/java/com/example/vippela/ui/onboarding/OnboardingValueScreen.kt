package com.techfix.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Troque "R" pelo import do R do seu módulo, ex: import com.techfix.app.R

/**
 * Tela 02 do onboarding — "Nosso valor"
 * Standalone: não depende do OnboardingScreen/pager.
 */
@Composable
fun OnboardingValueScreen(
    onNext: () -> Unit,
    totalSteps: Int = 5,
    currentStep: Int = 1
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLavender)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {

            // Badge "02"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StepChipPurple)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "02",
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Nosso\nvalor",
                color = TextTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "\"Segurança e bem-estar digital para a sua família\"",
                color = TextBody,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontStyle = FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card com a ilustração (troque pelo asset exportado do Figma)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CardWhite),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_onboarding_value),
                    contentDescription = "Ilustração de família e segurança digital",
                    modifier = Modifier.fillMaxSize(0.75f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de páginas (bolinhas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalSteps) { index ->
                    val isSelected = index == currentStep
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AccentPurple else StepChipPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text(
                    text = "Proximo",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 420)
@Composable
private fun OnboardingValueScreenPreview() {
    OnboardingValueScreen(onNext = {})
}
