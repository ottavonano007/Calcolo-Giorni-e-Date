package it.pietro.calcologiorniedate

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY)
private enum class Calculation(val label: String) {
    BETWEEN("Giorni tra due date"), ADD("Aggiungi giorni a una data"),
    PAST("Giorni trascorsi da una data"), FUTURE("Giorni mancanti a una data")
}
private enum class AppTab(val label: String) { CALCULATOR("Calcolo"), FAVORITES("Preferiti"), HISTORY("Cronologia"), SETTINGS("Impostazioni") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DateCalculatorApp() }
    }
}

@Composable
private fun DateCalculatorApp() {
    val context = LocalContext.current
    val storage = remember { CalculationStorage(context) }
    var tab by remember { mutableStateOf(AppTab.CALCULATOR) }
    var calculation by remember { mutableStateOf(Calculation.BETWEEN) }
    var calculationMenu by remember { mutableStateOf(false) }
    var firstDate by remember { mutableStateOf(LocalDate.now().format(dateFormat)) }
    var secondDate by remember { mutableStateOf(LocalDate.now().format(dateFormat)) }
    var days by remember { mutableStateOf("30") }
    var result by remember { mutableStateOf<String?>(null) }
    var lastRecord by remember { mutableStateOf<CalculationRecord?>(null) }
    var history by remember { mutableStateOf(storage.history()) }
    var favorites by remember { mutableStateOf(storage.favorites()) }
    var theme by remember { mutableStateOf(storage.theme()) }
    var favoriteDialog by remember { mutableStateOf(false) }
    var favoriteName by remember { mutableStateOf("") }
    var clearTarget by remember { mutableStateOf<String?>(null) }
    var deleteFavoriteId by remember { mutableStateOf<Long?>(null) }

    val dark = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    fun refresh() {
        history = storage.history()
        favorites = storage.favorites()
    }
    fun parse(value: String): LocalDate? = try {
        LocalDate.parse(value.trim(), dateFormat)
    } catch (_: DateTimeParseException) { null }
    fun pickDate(value: String, update: (String) -> Unit) {
        val date = parse(value) ?: LocalDate.now()
        DatePickerDialog(context, { _, y, m, d ->
            update(LocalDate.of(y, m + 1, d).format(dateFormat))
        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }
    fun reuse(record: CalculationRecord) {
        calculation = try { Calculation.valueOf(record.type) } catch (_: Exception) { Calculation.BETWEEN }
        firstDate = record.firstDate
        secondDate = record.secondDate
        days = record.days
        result = record.result
        lastRecord = record
        tab = AppTab.CALCULATOR
    }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppTab.values().forEach { item ->
                        val icon = when (item) {
                            AppTab.CALCULATOR -> Icons.Filled.Calculate
                            AppTab.FAVORITES -> Icons.Filled.Star
                            AppTab.HISTORY -> Icons.Filled.History
                            AppTab.SETTINGS -> Icons.Filled.Settings
                        }
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Calcolo Giorni e Date", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                when (tab) {
                    AppTab.CALCULATOR -> {
                        Text("Scegli il tipo di calcolo", style = MaterialTheme.typography.titleMedium)
                        Column {
                            Button(onClick = { calculationMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(calculation.label) }
                            DropdownMenu(expanded = calculationMenu, onDismissRequest = { calculationMenu = false }) {
                                Calculation.values().forEach { option ->
                                    DropdownMenuItem(text = { Text(option.label) }, onClick = {
                                        calculation = option
                                        result = null
                                        lastRecord = null
                                        calculationMenu = false
                                    })
                                }
                            }
                        }
                        when (calculation) {
                            Calculation.BETWEEN -> {
                                DateInput("Data iniziale", firstDate, { firstDate = it; result = null; lastRecord = null }) { pickDate(firstDate) { firstDate = it; result = null; lastRecord = null } }
                                DateInput("Data finale", secondDate, { secondDate = it; result = null; lastRecord = null }) { pickDate(secondDate) { secondDate = it; result = null; lastRecord = null } }
                            }
                            Calculation.ADD -> {
                                DateInput("Data di partenza", firstDate, { firstDate = it; result = null; lastRecord = null }) { pickDate(firstDate) { firstDate = it; result = null; lastRecord = null } }
                                OutlinedTextField(
                                    value = days,
                                    onValueChange = { days = it.filter(Char::isDigit); result = null; lastRecord = null },
                                    label = { Text("Numero di giorni") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Calculation.PAST, Calculation.FUTURE ->
                                DateInput("Seleziona la data", secondDate, { secondDate = it; result = null; lastRecord = null }) { pickDate(secondDate) { secondDate = it; result = null; lastRecord = null } }
                        }

                        Button(onClick = {
                            val start = parse(firstDate)
                            val end = parse(secondDate)
                            val answer = try {
                                when (calculation) {
                                    Calculation.BETWEEN ->
                                        if (start == null || end == null) "Controlla le date inserite."
                                        else { val n = kotlin.math.abs(ChronoUnit.DAYS.between(start, end)); "$n giorni (${formatDaysAsWeeks(n)})" }
                                    Calculation.ADD -> {
                                        val amount = days.toLongOrNull()
                                        if (start == null || amount == null) "Controlla la data e il numero di giorni."
                                        else "Data di scadenza: " + start.plusDays(amount).format(dateFormat) + "\nGiorni aggiunti: " + amount + " (${formatDaysAsWeeks(amount)})"
                                    }
                                    Calculation.PAST -> if (end == null) "Controlla la data inserita." else {
                                        val n = ChronoUnit.DAYS.between(end, LocalDate.now())
                                        if (n < 0) "La data indicata è nel futuro." else "Sono trascorsi " + n + " giorni (" + formatDaysAsWeeks(n) + ")"
                                    }
                                    Calculation.FUTURE -> if (end == null) "Controlla la data inserita." else {
                                        val n = ChronoUnit.DAYS.between(LocalDate.now(), end)
                                        if (n < 0) "La data indicata è nel passato." else "Mancano " + n + " giorni (" + formatDaysAsWeeks(n) + ")"
                                    }
                                }
                            } catch (_: DateTimeException) {
                                "Il calcolo supera l'intervallo di date consentito."
                            } catch (_: ArithmeticException) {
                                "Il numero di giorni è troppo grande."
                            }
                            result = answer
                            val invalid = answer.startsWith("Controlla") || answer.startsWith("La data indicata") ||
                                answer.startsWith("Il calcolo") || answer.startsWith("Il numero")
                            if (!invalid) {
                                val now = System.currentTimeMillis()
                                val record = CalculationRecord(now, calculation.label, calculation.name, firstDate, secondDate, days, answer, now)
                                lastRecord = record
                                storage.addToHistory(record)
                                refresh()
                            } else lastRecord = null
                        }, modifier = Modifier.fillMaxWidth()) { Text("Calcola") }

                        result?.let { answer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Risultato", style = MaterialTheme.typography.titleMedium)
                                    SelectionContainer { Text(answer, style = MaterialTheme.typography.headlineSmall) }
                                    Text("Puoi selezionare il testo del risultato per copiarlo.", style = MaterialTheme.typography.bodySmall)
                                    if (lastRecord != null) {
                                        OutlinedButton(onClick = { favoriteName = calculation.label; favoriteDialog = true }) {
                                            Text("★  Aggiungi ai preferiti")
                                        }
                                    }
                                }
                            }
                        }
                        Text("Le date si possono digitare nel formato GG/MM/AAAA oppure scegliere dal calendario.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    AppTab.FAVORITES -> {
                        Text("I tuoi calcoli salvati", style = MaterialTheme.typography.titleMedium)
                        if (favorites.isEmpty()) Text("Non hai ancora salvato preferiti. Esegui un calcolo e tocca «Aggiungi ai preferiti».")
                        favorites.sortedByDescending { it.createdAt }.forEach { record ->
                            RecordCard(record, true, { reuse(record) }, { deleteFavoriteId = record.id })
                        }
                        if (favorites.isNotEmpty()) {
                            OutlinedButton(onClick = { clearTarget = "favorites" }, modifier = Modifier.fillMaxWidth()) { Text("Cancella tutti i preferiti") }
                        }
                    }

                    AppTab.HISTORY -> {
                        Text("Ultimi 10 calcoli", style = MaterialTheme.typography.titleMedium)
                        if (history.isEmpty()) Text("La cronologia è vuota. I calcoli validi appariranno qui automaticamente.")
                        history.sortedByDescending { it.createdAt }.forEach { record ->
                            RecordCard(record, false, { reuse(record) }, { storage.deleteHistoryItem(record.id); refresh() })
                        }
                        if (history.isNotEmpty()) {
                            OutlinedButton(onClick = { clearTarget = "history" }, modifier = Modifier.fillMaxWidth()) { Text("Cancella tutta la cronologia") }
                        }
                    }

                    AppTab.SETTINGS -> {
                        Text("Aspetto", style = MaterialTheme.typography.titleMedium)
                        Text("Scegli come visualizzare l'app.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = theme == "system", onClick = { theme = "system"; storage.setTheme(theme) }, label = { Text("Sistema") })
                            FilterChip(selected = theme == "light", onClick = { theme = "light"; storage.setTheme(theme) }, label = { Text("Chiaro") })
                            FilterChip(selected = theme == "dark", onClick = { theme = "dark"; storage.setTheme(theme) }, label = { Text("Scuro") })
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Dati e privacy", style = MaterialTheme.typography.titleMedium)
                        Text("Preferiti, cronologia e impostazioni vengono conservati esclusivamente sul dispositivo.")
                        OutlinedButton(onClick = { clearTarget = "history" }, modifier = Modifier.fillMaxWidth()) { Text("Cancella tutta la cronologia") }
                        OutlinedButton(onClick = { clearTarget = "favorites" }, modifier = Modifier.fillMaxWidth()) { Text("Cancella tutti i preferiti") }
                    }
                }
            }
        }

        if (favoriteDialog) {
            AlertDialog(
                onDismissRequest = { favoriteDialog = false },
                title = { Text("Salva nei preferiti") },
                text = {
                    OutlinedTextField(value = favoriteName, onValueChange = { favoriteName = it },
                        label = { Text("Nome del preferito") }, singleLine = true)
                },
                confirmButton = {
                    TextButton(onClick = {
                        val record = lastRecord
                        val name = favoriteName.trim()
                        if (record != null && name.isNotEmpty()) {
                            storage.addFavorite(record.copy(id = System.currentTimeMillis(), title = name))
                            refresh()
                            favoriteDialog = false
                        }
                    }) { Text("Salva") }
                },
                dismissButton = { TextButton(onClick = { favoriteDialog = false }) { Text("Annulla") } }
            )
        }

        clearTarget?.let { target ->
            val label = if (target == "history") "la cronologia" else "i preferiti"
            AlertDialog(
                onDismissRequest = { clearTarget = null },
                title = { Text("Conferma cancellazione") },
                text = { Text("Vuoi davvero cancellare " + label + "? Questa operazione non può essere annullata.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (target == "history") storage.clearHistory() else storage.clearFavorites()
                        refresh()
                        clearTarget = null
                    }) { Text("Cancella") }
                },
                dismissButton = { TextButton(onClick = { clearTarget = null }) { Text("Annulla") } }
            )
        }

        deleteFavoriteId?.let { id ->
            AlertDialog(
                onDismissRequest = { deleteFavoriteId = null },
                title = { Text("Elimina preferito") },
                text = { Text("Vuoi eliminare questo elemento dai preferiti?") },
                confirmButton = {
                    TextButton(onClick = { storage.deleteFavorite(id); refresh(); deleteFavoriteId = null }) { Text("Elimina") }
                },
                dismissButton = { TextButton(onClick = { deleteFavoriteId = null }) { Text("Annulla") } }
            )
        }
    }
}

@Composable
private fun RecordCard(record: CalculationRecord, favorite: Boolean, onReuse: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(record.result, style = MaterialTheme.typography.bodyLarge)
            Text("Salvato: " + java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(record.createdAt)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onReuse) { Text("Riutilizza") }
                TextButton(onClick = onDelete) { Text(if (favorite) "Elimina" else "Rimuovi") }
            }
        }
    }
}

@Composable
private fun DateInput(label: String, value: String, onValueChange: (String) -> Unit, onCalendarClick: () -> Unit) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    // Aggiorna il campo quando la data cambia dall'esterno (calendario o riutilizzo).
    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = { typed ->
                val formatted = formatDateInput(typed.text, typed.selection.start, typed.selection.end)
                fieldValue = TextFieldValue(
                    text = formatted.text,
                    selection = TextRange(formatted.selectionStart, formatted.selectionEnd)
                )
                onValueChange(formatted.text)
            },
            label = { Text(label) }, placeholder = { Text("GG/MM/AAAA") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onCalendarClick, modifier = Modifier.padding(top = 8.dp)) { Text("Calendario") }
    }
}
