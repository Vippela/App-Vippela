package br.com.vippela.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LinkedAppIcon(label: String, encoded: String?) {
    val bitmap by produceState<ImageBitmap?>(null, encoded) {
        value = withContext(Dispatchers.Default) {
            runCatching {
                val data = Base64.decode(encoded ?: return@withContext null, Base64.NO_WRAP)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
                if (bounds.outWidth !in 1..64 || bounds.outHeight !in 1..64) return@withContext null
                BitmapFactory.decodeByteArray(data, 0, data.size)?.asImageBitmap()
            }.getOrNull()
        }
    }
    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap!!, null, Modifier.size(36.dp), contentScale = ContentScale.Fit)
        else Text(label.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
    }
}
