package it.pietro.calcologiorniedate

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY)

private enum class Calculation(val label: String) {
    BETWEEN("Giorni tra due date"),
    ADD("Aggiungi giorni a una data"),
    PAST("Giorni trascorsi da una data"),
    FUTURE("Giorni mancanti a una data")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DateCalculatorApp() }
    }
}

@Composable
private fun DateCalculatorApp() {
    var calculation by remember { mutableStateOf(Calculation.BETWEEN) }
    var menuExpanded by remember { mutableStateOf(false) }
    var firstDate by remember { mutableStateOf(LocalDate.now().format(dateFormat)) }
    var secondDate by remember { mutableStateOf(LocalDate.now().format(dateFormat)) }
    var daysInput by remember { mutableStateOf("30") }
    var result by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value.trim(), dateFormat)
    } catch (_: DateTimeParseException) {
        null
    }

    fun showDatePicker(current: String, onDate: (String) -> Unit) {
        val parsed = parseDate(current) ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onDate(LocalDate.of(year, month + 1, day).format(dateFormat))
            },
            parsed.year,
            parsed.monthValue - 1,
            parsed.dayOfMonth
        ).show()
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Calcolo Giorni e Date", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Scegli il tipo di calcolo", style = MaterialTheme.typography.titleMedium)

                Column {
                    Button(onClick = { menuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(calculation.label)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        Calculation.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    calculation = option
                                    result = null
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                when (calculation) {
                    Calculation.BETWEEN -> {
                        DateInput("Data iniziale", firstDate, { firstDate = it }) { showDatePicker(firstDate, it) }
                        DateInput("Data finale", secondDate, { secondDate = it }) { showDatePicker(secondDate, it) }
                    }
                    Calculation.ADD -> {
                        DateInput("Data di partenza", firstDate, { firstDate = it }) { showDatePicker(firstDate, it) }
                        OutlinedTextField(
                            value = daysInput,
                            onValueChange = { daysInput = it.filter(Char::isDigit) },
                            label = { Text("Numero di giorni") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Calculation.PAST, Calculation.FUTURE -> {
                        DateInput("Seleziona la data", secondDate, { secondDate = it }) { showDatePicker(secondDate, it) }
                    }
                }

                Button(
                    onClick = {
                        val start = parseDate(firstDate)
                        val end = parseDate(secondDate)
                        result = when (calculation) {
                            Calculation.BETWEEN -> if (start == null || end == null) {
                                "Controlla le date inserite."
                            } else {
                                "${kotlin.math.abs(ChronoUnit.DAYS.between(start, end))} giorni"
                            }
                            Calculation.ADD -> {
                                val amount = daysInput.toLongOrNull()
                                if (start == null || amount == null) "Controlla la data e il numero di giorni."
                                else "Data di scadenza: ${start.plusDays(amount).format(dateFormat)}"
                            }
                            Calculation.PAST -> if (end == null) {
                                "Controlla la data inserita."
                            } else {
                                val difference = ChronoUnit.DAYS.between(end, LocalDate.now())
                                if (difference < 0) "La data indicata è nel futuro." else "Sono trascorsi $difference giorni"
                            }
                            Calculation.FUTURE -> if (end == null) {
                                "Controlla la data inserita."
                            } else {
                                val difference = ChronoUnit.DAYS.between(LocalDate.now(), end)
                                if (difference < 0) "La data indicata è nel passato." else "Mancano $difference giorni"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Calcola") }

                result?.let { answer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Risultato", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            SelectionContainer { Text(answer, style = MaterialTheme.typography.headlineSmall) }
                            Text("Puoi selezionare il testo del risultato per copiarlo.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Le date si possono digitare nel formato GG/MM/AAAA oppure scegliere dal calendario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DateInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onCalendarClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text("GG/MM/AAAA") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onCalendarClick, modifier = Modifier.padding(top = 8.dp)) {
            Text("Calendario")
        }
    }
}
