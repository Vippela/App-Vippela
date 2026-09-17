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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Troque "R" pelo import do R do seu módulo, ex: import com.techfix.app.R

/**
 * Tela 01 do onboarding — "Seja bem-vindo!"
 * Standalone: não depende do OnboardingScreen/pager, útil pra visualizar
 * ou usar essa tela isolada (splash inicial, por exemplo).
 */
@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit,
    totalSteps: Int = 5,
    currentStep: Int = 0
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

            // Badge "01"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StepChipPurple)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "01",
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Seja\nbem-vindo!",
                color = TextTitle,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Vamos começar uma jornada digital mais saudável.",
                color = TextBody,
                fontSize = 15.sp,
                lineHeight = 22.sp
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
                    painter = painterResource(id = R.drawable.ic_onboarding_welcome),
                    contentDescription = "Ilustração de boas-vindas",
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
private fun OnboardingWelcomeScreenPreview() {
    OnboardingWelcomeScreen(onNext = {})
}
