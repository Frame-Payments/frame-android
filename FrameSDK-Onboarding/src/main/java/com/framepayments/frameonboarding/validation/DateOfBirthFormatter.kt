package com.framepayments.frameonboarding.validation

/**
 * Assembles ISO 8601 "YYYY-MM-DD" with zero-padded month/day.
 * Returns an empty string if any component is empty. 1:1 port of iOS DateOfBirthFormatter.
 */
object DateOfBirthFormatter {
    /**
     * Assembles a zero-padded ISO 8601 date string from separate year, month, and day components.
     *
     * @param year 4-digit year (e.g. "1990").
     * @param month 1- or 2-digit month; single digits are zero-padded (e.g. "3" → "03").
     * @param day 1- or 2-digit day; single digits are zero-padded (e.g. "5" → "05").
     * @return An ISO 8601 string in the form "YYYY-MM-DD", or an empty string if any component
     *   is empty.
     */
    fun format(year: String, month: String, day: String): String {
        if (year.isEmpty() || month.isEmpty() || day.isEmpty()) return ""
        val paddedYear = if (year.length >= 4) year else "0".repeat(4 - year.length) + year
        val paddedMonth = if (month.length == 2) month else "0$month"
        val paddedDay = if (day.length == 2) day else "0$day"
        return "$paddedYear-$paddedMonth-$paddedDay"
    }
}
