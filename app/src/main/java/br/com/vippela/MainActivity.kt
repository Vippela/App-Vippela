package br.com.vippela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.vippela.ui.navigation.VippelaApp
import br.com.vippela.ui.theme.VippelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VippelaTheme { VippelaApp() } }
    }
}
