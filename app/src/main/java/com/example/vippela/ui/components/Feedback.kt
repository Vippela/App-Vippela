package com.example.vippela.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vippela.ui.theme.LocalVippelaPalette
import kotlinx.coroutines.delay

class FeedbackState {
    var message by mutableStateOf<String?>(null)
    var title by mutableStateOf<String?>(null)

    fun show(title: String, message: String) {
        this.title = title
        this.message = message
    }

    fun dismiss() {
        title = null
        message = null
    }
}

@Composable
fun rememberFeedbackState(): FeedbackState = remember { FeedbackState() }

@Composable
fun FeedbackHost(state: FeedbackState, modifier: Modifier = Modifier) {
    val palette = LocalVippelaPalette.current
    val currentMessage = state.message

    LaunchedEffect(currentMessage) {
        if (currentMessage != null) {
            delay(3_200)
            state.dismiss()
        }
    }

    AnimatedVisibility(
        visible = currentMessage != null,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(palette.cardElevated, RoundedCornerShape(16.dp))
                .clickable { state.dismiss() }
                .padding(16.dp),
        ) {
            state.title?.let {
                Text(it, color = palette.onCard, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
            }
            Text(currentMessage.orEmpty(), color = palette.muted, fontSize = 14.sp, lineHeight = 18.sp)
        }
    }
}
