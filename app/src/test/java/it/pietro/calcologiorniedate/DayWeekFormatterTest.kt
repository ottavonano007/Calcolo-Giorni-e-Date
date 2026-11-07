package it.pietro.calcologiorniedate

import org.junit.Assert.assertEquals
import org.junit.Test

class DayWeekFormatterTest {
    @Test
    fun formatsZeroDays() {
        assertEquals("0 settimane", formatDaysAsWeeks(0))
    }

    @Test
    fun formatsOneToSixRemainingDays() {
        assertEquals("0 settimane e 1 giorno", formatDaysAsWeeks(1))
        assertEquals("0 settimane e 6 giorni", formatDaysAsWeeks(6))
    }

    @Test
    fun formatsExactWeeksWithoutRemainingDays() {
        assertEquals("1 settimana", formatDaysAsWeeks(7))
        assertEquals("2 settimane", formatDaysAsWeeks(14))
    }

    @Test
    fun formatsWeeksAndRemainingDays() {
        assertEquals("1 settimana e 1 giorno", formatDaysAsWeeks(8))
        assertEquals("3 settimane e 3 giorni", formatDaysAsWeeks(24))
    }
}
