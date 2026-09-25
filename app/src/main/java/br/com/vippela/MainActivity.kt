package br.com.vippela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.initializer
import br.com.vippela.ui.navigation.VippelaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state: br.com.vippela.data.DemoState =
                androidx.lifecycle.viewmodel.compose.viewModel(
                    factory =
                        androidx.lifecycle.viewmodel.viewModelFactory {
                            initializer {
                                br.com.vippela.data.DemoState(
                                    br.com.vippela.data.LocalAccounts(applicationContext)
                                )
                            }
                        }
                )
            VippelaApp(state)
        }
    }
}
