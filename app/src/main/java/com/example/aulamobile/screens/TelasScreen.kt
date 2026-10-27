package com.example.aulatelas.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

private val Green = Color(0xFF198754)
private val LightGreen = Color(0xFFEAF6EF)
private val Ink = Color(0xFF1C2922)
private val Red = Color(0xFFB42318)
private val LightRed = Color(0xFFFDECEA)

// takenDate guarda o dia (yyyy-MM-dd) em que a dose foi marcada.
// Assim, a dose volta a ficar "pendente" automaticamente no dia seguinte.
private data class Medicine(
    val id: Int,
    val name: String,
    val dosage: String,
    val time: String,
    val notes: String = "",
    val takenDate: String = ""
)

private enum class DoseStatus { TAKEN, LATE, PENDING }

private fun todayKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun nowMinutes(): Int {
    val c = Calendar.getInstance()
    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
}

// Converte "8:00" ou "08:00" em minutos desde meia-noite. Retorna null se for inválido.
private fun parseTime(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

private fun formatTime(minutes: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", minutes / 60, minutes % 60)

private fun sampleMedicines(): List<Medicine> = listOf(
    Medicine(1, "Vitamina C", "1 comprimido", "08:00", "Após o café da manhã"),
    Medicine(2, "Medicamento prescrito", "Conforme receita", "14:00", "Exemplo demonstrativo"),
    Medicine(3, "Vitamina D", "1 cápsula", "20:00", "Após o jantar")
)

// ---- Armazenamento local (por usuário logado) ----
private fun storageKey(): String =
    "meds_" + (FirebaseAuth.getInstance().currentUser?.uid ?: "local")

private fun loadMedicines(context: Context): List<Medicine> {
    val prefs = context.getSharedPreferences("saude_em_dia", Context.MODE_PRIVATE)
    val raw = prefs.getString(storageKey(), null) ?: return sampleMedicines()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Medicine(
                id = o.getInt("id"),
                name = o.getString("name"),
                dosage = o.getString("dosage"),
                time = o.getString("time"),
                notes = o.optString("notes", ""),
                takenDate = o.optString("takenDate", "")
            )
        }
    } catch (e: Exception) {
        sampleMedicines()
    }
}

private fun saveMedicines(context: Context, list: List<Medicine>) {
    val arr = JSONArray()
    list.forEach { m ->
        val o = JSONObject()
        o.put("id", m.id)
        o.put("name", m.name)
        o.put("dosage", m.dosage)
        o.put("time", m.time)
        o.put("notes", m.notes)
        o.put("takenDate", m.takenDate)
        arr.put(o)
    }
    context.getSharedPreferences("saude_em_dia", Context.MODE_PRIVATE)
        .edit().putString(storageKey(), arr.toString()).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelasScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current

    // Abre primeiro na funcionalidade principal do app: acompanhar a rotina diária.
    var selectedTab by remember { mutableIntStateOf(1) }
    var medicines by remember { mutableStateOf(loadMedicines(context)) }

    // Salva sempre que a lista mudar
    LaunchedEffect(medicines) { saveMedicines(context, medicines) }

    // Relógio: atualiza a cada 30s para a dose virar "atrasada" na hora certa
    var now by remember { mutableIntStateOf(nowMinutes()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = nowMinutes()
            delay(30_000)
        }
    }
    val today = todayKey()

    fun statusOf(m: Medicine): DoseStatus = when {
        m.takenDate == today -> DoseStatus.TAKEN
        (parseTime(m.time) ?: 0) < now -> DoseStatus.LATE
        else -> DoseStatus.PENDING
    }

    fun toggleTaken(m: Medicine) {
        medicines = medicines.map {
            if (it.id == m.id) it.copy(takenDate = if (it.takenDate == today) "" else today) else it
        }
    }

    val sorted = medicines.sortedBy { parseTime(it.time) ?: 0 }
    val takenCount = medicines.count { statusOf(it) == DoseStatus.TAKEN }
    val lateCount = medicines.count { statusOf(it) == DoseStatus.LATE }
    val nextDose = sorted.firstOrNull { statusOf(it) == DoseStatus.PENDING }

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Medicine?>(null) }
    var toDelete by remember { mutableStateOf<Medicine?>(null) }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Saúde em Dia", fontWeight = FontWeight.Bold)
                        Text(
                            when (selectedTab) {
                                0 -> "Seus medicamentos"
                                1 -> "Acompanhamento diário"
                                2 -> "Dicas de cuidado"
                                else -> "Seu perfil"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) { Text("Sair", color = Green) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Medication, null) },
                    label = { Text("Medicamentos", fontSize = 11.sp, maxLines = 1) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Today, null) },
                    label = { Text("Rotina", fontSize = 11.sp, maxLines = 1) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Lightbulb, null) },
                    label = { Text("Dicas", fontSize = 11.sp, maxLines = 1) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil", fontSize = 11.sp, maxLines = 1) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) FloatingActionButton(
                onClick = {
                    editing = null; name = ""; dosage = ""; time = ""; notes = ""; showForm = true
                },
                containerColor = Green,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Adicionar medicamento") }
        },
        containerColor = Color(0xFFF6F8F6)
    ) { padding ->
        if (selectedTab == 0) {
            // ---------------- TELA 1: MEDICAMENTOS ----------------
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(LightGreen, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Text(
                            "Organize seu cuidado",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Cadastre seus medicamentos e acompanhe os horários em um só lugar.",
                            color = Color(0xFF496054)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = Green, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${medicines.size} medicamento(s) cadastrado(s)",
                                color = Green,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                item {
                    Text(
                        "MEUS MEDICAMENTOS",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (medicines.isEmpty()) item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(
                            Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Medication, null, tint = Green, modifier = Modifier.size(40.dp))
                            Text("Nenhum medicamento ainda", fontWeight = FontWeight.SemiBold)
                            Text("Toque no botão + para cadastrar o primeiro.", color = Color.Gray)
                        }
                    }
                }
                items(sorted, key = { it.id }) { medicine ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(44.dp).background(LightGreen, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Medication, null, tint = Green) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        medicine.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        medicine.dosage,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(medicine.time, color = Green, fontWeight = FontWeight.Bold)
                            }
                            if (medicine.notes.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(medicine.notes, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color(0xFFEEF1EE))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = {
                                    editing = medicine
                                    name = medicine.name
                                    dosage = medicine.dosage
                                    time = medicine.time
                                    notes = medicine.notes
                                    showForm = true
                                }) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Editar")
                                }
                                TextButton(onClick = { toDelete = medicine }) {
                                    Icon(Icons.Default.Delete, null, tint = Red, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Excluir", color = Red)
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Importante: siga sempre a prescrição e as orientações de um profissional de saúde.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                }
            }
        } else if (selectedTab == 1) {
            // ---------------- TELA 2: MINHA ROTINA ----------------
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(Green, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            "Sua rotina de hoje",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Um passo de cada vez. Cuide de você!", color = Color.White.copy(alpha = .9f))
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$takenCount de ${medicines.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("doses tomadas", color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (medicines.isEmpty()) 0f else takenCount.toFloat() / medicines.size
                            },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = .25f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            when {
                                medicines.isEmpty() -> "Cadastre um medicamento para começar."
                                takenCount == medicines.size -> "Rotina de hoje completa!"
                                nextDose != null -> "Próxima dose: ${nextDose.name} às ${nextDose.time}"
                                else -> "Nenhuma dose pendente para o resto do dia."
                            },
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (lateCount > 0) item {
                    Row(
                        Modifier.fillMaxWidth()
                            .background(LightRed, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Red, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (lateCount == 1) "1 dose atrasada. Já tomou? Marque abaixo."
                            else "$lateCount doses atrasadas. Já tomou? Marque abaixo.",
                            color = Red,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                item {
                    Text(
                        "HORÁRIOS DE HOJE",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (medicines.isEmpty()) item {
                    Text("Cadastre um medicamento na aba Medicamentos para montar sua rotina.", color = Color.Gray)
                }
                items(sorted, key = { it.id }) { medicine ->
                    val status = statusOf(medicine)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (status == DoseStatus.LATE) LightRed else Color.White
                        )
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    medicine.time,
                                    color = if (status == DoseStatus.LATE) Red else Green,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Icon(
                                    Icons.Default.Schedule,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(medicine.name, fontWeight = FontWeight.Bold, color = Ink)
                                Text(
                                    medicine.dosage,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                when (status) {
                                    DoseStatus.TAKEN -> Text(
                                        "Tomada",
                                        color = Green,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    DoseStatus.LATE -> Text(
                                        "Atrasada",
                                        color = Red,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    DoseStatus.PENDING -> Text(
                                        "Pendente",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            if (status == DoseStatus.TAKEN) {
                                // Toque no check para desfazer a marcação
                                IconButton(onClick = { toggleTaken(medicine) }) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Dose tomada (toque para desfazer)",
                                        tint = Green,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { toggleTaken(medicine) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) { Text("Tomei") }
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Os dados ficam salvos neste aparelho e as doses voltam a ficar pendentes a cada novo dia. Esta versão não envia notificações.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else if (selectedTab == 2) {
            // ---------------- TELA 3: DICAS ----------------
            DicasScreen(Modifier.padding(padding))
        } else {
            // ---------------- TELA 4: PERFIL ----------------
            PerfilScreen(
                total = medicines.size,
                taken = takenCount,
                late = lateCount,
                modifier = Modifier.padding(padding),
                onLogout = onLogout
            )
        }
    }

    // ---------------- Formulário (novo / editar) ----------------
    if (showForm) {
        val parsedTime = parseTime(time)
        val timeInvalid = time.isNotBlank() && parsedTime == null
        AlertDialog(
            onDismissRequest = { showForm = false },
            title = {
                Text(
                    if (editing == null) "Novo medicamento" else "Editar medicamento",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do medicamento") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosagem / instrução") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Horário (ex.: 08:00)") },
                        singleLine = true,
                        isError = timeInvalid,
                        supportingText = { if (timeInvalid) Text("Use o formato HH:MM, de 00:00 a 23:59") }
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações (opcional)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && dosage.isNotBlank() && parsedTime != null,
                    onClick = {
                        parsedTime?.let { minutes ->
                            val cleanTime = formatTime(minutes)
                            val old = editing
                            medicines = if (old == null) {
                                val newId = (medicines.maxOfOrNull { it.id } ?: 0) + 1
                                medicines + Medicine(newId, name.trim(), dosage.trim(), cleanTime, notes.trim())
                            } else {
                                medicines.map {
                                    if (it.id == old.id) old.copy(
                                        name = name.trim(),
                                        dosage = dosage.trim(),
                                        time = cleanTime,
                                        notes = notes.trim()
                                    ) else it
                                }
                            }
                            showForm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { showForm = false }) { Text("Cancelar") } }
        )
    }

    // ---------------- Confirmação de exclusão ----------------
    toDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Excluir medicamento?", fontWeight = FontWeight.Bold) },
            text = { Text("${target.name} será removido da sua rotina.") },
            confirmButton = {
                TextButton(onClick = {
                    medicines = medicines.filterNot { it.id == target.id }
                    toDelete = null
                }) { Text("Excluir", color = Red) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
