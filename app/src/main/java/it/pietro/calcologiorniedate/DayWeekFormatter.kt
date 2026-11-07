package it.pietro.calcologiorniedate

/**
 * Converte un numero di giorni non negativo in settimane intere e giorni residui.
 * Esempi: 7 -> "1 settimana", 8 -> "1 settimana e 1 giorno".
 */
internal fun formatDaysAsWeeks(days: Long): String {
    require(days >= 0) { "Il numero di giorni non può essere negativo." }
    val weeks = days / 7
    val remainder = (days % 7).toInt()
    val weeksLabel = if (weeks == 1L) "settimana" else "settimane"
    if (remainder == 0) return "$weeks $weeksLabel"
    val daysLabel = if (remainder == 1) "giorno" else "giorni"
    return "$weeks $weeksLabel e $remainder $daysLabel"
}
