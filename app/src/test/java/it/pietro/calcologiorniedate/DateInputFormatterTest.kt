package it.pietro.calcologiorniedate

import org.junit.Assert.assertEquals
import org.junit.Test

class DateInputFormatterTest {
    @Test
    fun insertsSlashesAsDigitsAreEntered() {
        assertEquals("1", formatDateInput("1"))
        assertEquals("10/", formatDateInput("10"))
        assertEquals("10/1", formatDateInput("101"))
        assertEquals("10/11/", formatDateInput("1011"))
        assertEquals("10/11/2", formatDateInput("10112"))
        assertEquals("10/11/2026", formatDateInput("10112026"))
    }

    @Test
    fun preservesCursorAfterAutomaticallyInsertedSeparators() {
        assertEquals(
            FormattedDateInput("10/", 3, 3),
            formatDateInput("10", 2, 2)
        )
        assertEquals(
            FormattedDateInput("10/1", 4, 4),
            formatDateInput("101", 3, 3)
        )
        assertEquals(
            FormattedDateInput("10/11/", 6, 6),
            formatDateInput("1011", 4, 4)
        )
    }

    @Test
    fun preservesCursorWhenEditingInTheMiddle() {
        assertEquals(
            FormattedDateInput("10/21/2026", 4, 4),
            formatDateInput("1021/2026", 3, 3)
        )
        assertEquals(
            FormattedDateInput("10/11/2026", 1, 1),
            formatDateInput("1011/2026", 1, 1)
        )
    }

    @Test
    fun mapsSelectionsAndClampsOffsets() {
        assertEquals(
            FormattedDateInput("10/11/2026", 3, 6),
            formatDateInput("10112026", 2, 4)
        )
        assertEquals(
            FormattedDateInput("10/11/2026", 10, 10),
            formatDateInput("10112026", 99, 99)
        )
    }

    @Test
    fun acceptsExistingSlashesAndReformatsConsistently() {
        assertEquals("10/11/2026", formatDateInput("10/11/2026"))
    }

    @Test
    fun ignoresNonDigitsAndLimitsInputToEightDigits() {
        assertEquals("10/11/2026", formatDateInput("10a/11-202699"))
    }

    @Test
    fun handlesEmptyAndPartialInput() {
        assertEquals("", formatDateInput(""))
        assertEquals("1", formatDateInput("1"))
        assertEquals("10/1", formatDateInput("10/1"))
    }
}
