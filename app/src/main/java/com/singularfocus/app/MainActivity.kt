package com.singularfocus.app

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val ink = Color(0xFF1C2727)
private val sage = Color(0xFF8CB4A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(245,247,244)
        window.navigationBarColor = android.graphics.Color.rgb(245,247,244)
        setContent { SingularApp() }
    }
}

@Composable
fun SingularApp() {
    var guided by remember { mutableStateOf(false) }
    var step by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf("setup") }
    var task by remember { mutableStateOf("") }
    var release by remember { mutableStateOf("") }
    var finishLine by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableLongStateOf(25L * 60) }
    var running by remember { mutableStateOf(false) }
    var deadline by remember { mutableLongStateOf(0L) }
    fun start() {
        remaining = duration * 60L
        deadline = SystemClock.elapsedRealtime() + remaining * 1000
        running = true
        screen = "focus"
    }
    LaunchedEffect(running, screen) {
        while (running && screen == "focus") {
            remaining = ((deadline - SystemClock.elapsedRealtime() + 999) / 1000).coerceAtLeast(0)
            if (remaining == 0L) { running = false; screen = "done" }
            delay(250)
        }
    }
    MaterialTheme(colorScheme = lightColorScheme(primary = ink, background = Color(0xFFF5F7F4), surface = Color.White, onSurface = ink)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F7F4)) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("SINGULAR FOCUS", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 13.sp, color = Color(0xFF537469))
                Spacer(Modifier.height(12.dp))
                when (screen) {
                    "setup" -> {
                        Text(if (guided) "Make one clear commitment." else "What matters right now?", fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.SemiBold)
                        if (!guided) {
                            Field("One thing to work on", task, { task = it }, "e.g. Write the introduction")
                            Field("What counts as done?", finishLine, { finishLine = it }, "A small, clear endpoint")
                            Text("Focus time", fontWeight = FontWeight.Medium)
                            DurationChoice(duration) { duration = it }
                            Button(onClick = { start() }, enabled = task.isNotBlank() && finishLine.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Begin focus") }
                            TextButton(onClick = { guided = true; step = 0 }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Help me decide instead") }
                        } else {
                            Text("STEP ${step + 1} OF 4", color = Color.Gray, fontSize = 12.sp)
                            when (step) {
                                0 -> Field("What is your one task?", task, { task = it }, "Name the work")
                                1 -> Field("What are you not doing right now?", release, { release = it }, "Release the other tasks")
                                2 -> Field("What would count as finished?", finishLine, { finishLine = it }, "A realistic endpoint")
                                else -> { Text("How long will you give it?"); DurationChoice(duration) { duration = it } }
                            }
                            Button(
                                onClick = { if (step == 3) start() else step++ },
                                enabled = when(step) { 0 -> task.isNotBlank(); 2 -> finishLine.isNotBlank(); else -> true },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (step == 3) "Begin focus" else "Continue") }
                            TextButton(onClick = { if (step > 0) step-- else guided = false }) { Text(if (step > 0) "Back" else "Quick start") }
                        }
                    }
                    "focus" -> {
                        Text("RIGHT NOW", fontSize = 12.sp, letterSpacing = 2.sp, color = Color.Gray)
                        Text(task, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
                        Text("Finish line: $finishLine", color = Color(0xFF537469))
                        if (release.isNotBlank()) Text("Not now: $release", color = Color.Gray)
                        Spacer(Modifier.height(36.dp))
                        Text("%02d:%02d".format(remaining / 60, remaining % 60), fontSize = 65.sp, fontWeight = FontWeight.Light, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Button(onClick = {
                            if (running) {
                                remaining = ((deadline - SystemClock.elapsedRealtime() + 999) / 1000).coerceAtLeast(0)
                                running = false
                            } else {
                                deadline = SystemClock.elapsedRealtime() + remaining * 1000
                                running = true
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (running) "Pause" else "Resume") }
                        OutlinedButton(onClick = { running = false; screen = "done" }, modifier = Modifier.fillMaxWidth()) { Text("Finish this session") }
                    }
                    else -> {
                        Spacer(Modifier.height(35.dp))
                        Text("You showed up.", fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                        Text("You can release this commitment now. The rest can wait.", fontSize = 17.sp, lineHeight = 25.sp)
                        Spacer(Modifier.height(18.dp))
                        Button(onClick = {
                            task = ""; release = ""; finishLine = ""; guided = false; step = 0
                            running = false; screen = "setup"
                        }, modifier = Modifier.fillMaxWidth()) { Text("Begin something new") }
                    }
                }
                Spacer(Modifier.weight(1f, fill = false))
                Text("One thing at a time.  ·  v0.1", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, hint: String) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        placeholder = { Text(hint) }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp), minLines = 1)
}

@Composable
private fun DurationChoice(value: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(10, 25, 45, 60).forEach { minutes ->
            if (minutes == value) Button(onClick = { onChange(minutes) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 2.dp)) { Text("${minutes}m") }
            else OutlinedButton(onClick = { onChange(minutes) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 2.dp)) { Text("${minutes}m") }
        }
    }
}
