package br.com.vippela.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*

@Composable
fun LearningScreen(state: DemoState, go: (String) -> Unit) {
    var topic by rememberSaveable { mutableIntStateOf(0) }
    var module by rememberSaveable { mutableIntStateOf(0) }
    Page {
        if (state.isParent)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                        "Início" to Icons.Outlined.FamilyRestroom,
                        "Phishing" to Icons.Outlined.Phishing,
                        "Privacidade" to Icons.Outlined.Lock,
                    )
                    .forEachIndexed { index, (title, icon) ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                Modifier.fillMaxWidth().height(88.dp).clickable { topic = index },
                                shape = RoundedCornerShape(12.dp),
                                color = if (topic == index) Violet else Purple,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, title, Modifier.size(44.dp), tint = Color.White)
                                }
                            }
                            Text(
                                title,
                                Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
            }
        if (topic == 2) {
            Panel {
                Text("Sua privacidade importa", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Cuide das informações que você compartilha, use senhas diferentes e revise as permissões dos aplicativos."
                )
                PrimaryButton("Começar trilha") { go("privacy-lesson") }
            }
        } else {
            if (!state.isParent || topic == 1) {
                Surface(shape = RoundedCornerShape(12.dp), color = Purple) {
                    Row(
                        Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Phishing,
                            null,
                            Modifier.size(46.dp),
                            tint = Color.White,
                        )
                        Column {
                            Text(
                                "Phishing",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            Text(
                                "Produzido por Vippela\nDescubra o que está por trás dos golpes digitais.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                            )
                        }
                    }
                }
                Tabs(listOf("Módulo 1", "Módulo 2", "Módulo 3"), module) { module = it }
            } else Heading("Últimos vistos:")
            lessons.forEachIndexed { index, lesson ->
                if (index / 3 == module) {
                    Surface(
                        Modifier.fillMaxWidth().clickable { go("lesson/$index") },
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 3.dp,
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                Modifier.size(78.dp).background(Purple, RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    listOf(
                                        Icons.Outlined.PhoneAndroid,
                                        Icons.Outlined.Phishing,
                                        Icons.Outlined.Security,
                                    )[index % 3],
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp),
                                )
                            }
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(lesson.title, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Módulo ${module + 1} • 15 min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted,
                                )
                                val finished = state.completed[state.lessonKey(index)] == true
                                Progress(if (finished) 1f else .6f)
                                Text(
                                    if (finished) "Concluído 100%" else "Concluído 60%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonScreen(index: Int, state: DemoState, back: () -> Unit) {
    val lesson = lessons[index]
    var answer by rememberSaveable(index) { mutableIntStateOf(-1) }
    var checked by rememberSaveable(index) { mutableStateOf(false) }
    Page {
        Panel {
            Icon(Icons.Outlined.School, null, Modifier.size(52.dp), tint = Violet)
            Text(lesson.title, style = MaterialTheme.typography.titleLarge)
            Text(
                "Módulo ${index / 3 + 1} • Leitura e atividade",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Text(lesson.text)
        }
        Heading("Vamos praticar?")
        Text(lesson.question)
        lesson.answers.forEachIndexed { i, label ->
            Surface(
                Modifier.fillMaxWidth().clickable {
                    answer = i
                    checked = false
                },
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        answer == i,
                        {
                            answer = i
                            checked = false
                        },
                    )
                    Text(label, Modifier.weight(1f))
                }
            }
        }
        if (checked)
            Panel {
                Text(
                    if (answer == lesson.correct) "Muito bem! Aula concluída."
                    else "Vamos tentar de novo?",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    if (answer == lesson.correct)
                        "Verificar antes de agir ajuda a proteger você e sua família."
                    else "Releia a orientação e escolha uma atitude que proteja suas informações."
                )
            }
        PrimaryButton(
            if (checked && answer == lesson.correct) "Voltar às trilhas" else "Verificar resposta",
            answer >= 0,
        ) {
            if (checked && answer == lesson.correct) back()
            else {
                checked = true
                if (answer == lesson.correct) state.completed[state.lessonKey(index)] = true
            }
        }
    }
}

@Composable
fun PrivacyLessonScreen(back: () -> Unit) {
    var completed by rememberSaveable { mutableStateOf(false) }
    Page {
        Heading("Compartilhe com cuidado")
        Panel {
            Icon(Icons.Outlined.Lock, null, Modifier.size(48.dp), tint = Violet)
            Text(
                "Seu endereço, sua escola e seus códigos de acesso são informações que merecem cuidado. Antes de publicar uma foto, observe o que aparece ao fundo."
            )
            Text(
                "Revise quem pode ver suas publicações e converse com uma pessoa de confiança quando tiver dúvidas."
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(completed, { completed = it })
            Text("Entendi: vou conferir antes de compartilhar.", Modifier.weight(1f))
        }
        PrimaryButton("Concluir leitura", completed, back)
    }
}
