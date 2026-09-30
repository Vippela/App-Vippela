package br.com.vippela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.initializer
import br.com.vippela.data.DemoState
import br.com.vippela.data.auth.AuthRepository
import br.com.vippela.data.auth.SessionStore
import br.com.vippela.data.linking.LinkStore
import br.com.vippela.ui.navigation.VippelaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state: DemoState =
                androidx.lifecycle.viewmodel.compose.viewModel(
                    factory =
                        androidx.lifecycle.viewmodel.viewModelFactory {
                            initializer {
                                val app = applicationContext
                                val sessoes = SessionStore(app)
                                DemoState(
                                    AuthRepository({ LinkStore(app).server }, sessoes)
                                )
                            }
                        }
                )
            VippelaApp(state)
        }
    }
}
