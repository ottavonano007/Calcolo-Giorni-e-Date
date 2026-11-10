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
