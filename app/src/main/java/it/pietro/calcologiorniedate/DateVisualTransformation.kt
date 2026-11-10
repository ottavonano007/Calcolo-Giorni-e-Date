package it.pietro.calcologiorniedate

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Mostra le barre GG/MM/AAAA senza inserirle nel testo effettivamente modificato.
 * Il campo conserva solo le cifre: digitazione, selezione e cancellazione
 * restano quindi naturali anche quando si tiene premuto Backspace.
 */
internal class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { index, digit ->
                append(digit)
                if (index == 1 && digits.length >= 2) append('/')
                if (index == 3 && digits.length >= 4) append('/')
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, digits.length)
                val firstSlash = if (digits.length >= 2 && safeOffset >= 2) 1 else 0
                val secondSlash = if (digits.length >= 4 && safeOffset >= 4) 1 else 0
                return (safeOffset + firstSlash + secondSlash).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                val firstSlash = if (digits.length >= 2 && safeOffset > 2) 1 else 0
                val secondSlash = if (digits.length >= 4 && safeOffset > 5) 1 else 0
                return (safeOffset - firstSlash - secondSlash).coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
