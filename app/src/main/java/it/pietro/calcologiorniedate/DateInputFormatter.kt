package it.pietro.calcologiorniedate

/**
 * Mantiene solo le prime otto cifre e inserisce automaticamente le barre
 * dopo il giorno e il mese durante la digitazione.
 */
internal fun formatDateInput(input: String): String {
    val digits = input.filter(Char::isDigit).take(8)
    return when {
        digits.length < 2 -> digits
        digits.length == 2 -> "$digits/"
        digits.length < 4 -> digits.substring(0, 2) + "/" + digits.substring(2)
        digits.length == 4 -> digits.substring(0, 2) + "/" + digits.substring(2) + "/"
        else -> digits.substring(0, 2) + "/" + digits.substring(2, 4) + "/" + digits.substring(4)
    }
}
