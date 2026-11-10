package it.pietro.calcologiorniedate

/**
 * Risultato della formattazione con la posizione del cursore e della selezione
 * riportate correttamente nel testo che contiene le barre.
 */
internal data class FormattedDateInput(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int
)

/**
 * Mantiene solo le prime otto cifre e inserisce automaticamente le barre
 * dopo il giorno e il mese.
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

/**
 * Formatta il testo e rimappa entrambe le estremità della selezione.
 * In questo modo il cursore non salta alla fine quando si corregge una cifra
 * nel mezzo della data.
 */
internal fun formatDateInput(input: String, selectionStart: Int, selectionEnd: Int): FormattedDateInput {
    val formatted = formatDateInput(input)

    fun mapOffset(offset: Int): Int {
        val safeOffset = offset.coerceIn(0, input.length)
        val digitsBeforeCursor = input.take(safeOffset).count(Char::isDigit).coerceAtMost(8)
        val separatorsBeforeCursor = (if (digitsBeforeCursor >= 2) 1 else 0) +
            (if (digitsBeforeCursor >= 4) 1 else 0)
        return (digitsBeforeCursor + separatorsBeforeCursor).coerceIn(0, formatted.length)
    }

    return FormattedDateInput(
        text = formatted,
        selectionStart = mapOffset(selectionStart),
        selectionEnd = mapOffset(selectionEnd)
    )
}
