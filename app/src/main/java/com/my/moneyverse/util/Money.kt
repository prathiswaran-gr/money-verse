package com.my.moneyverse.util

import java.security.SecureRandom
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import com.my.moneyverse.data.db.BudgetPeriod

private val indiaLocale = Locale.Builder().setLanguage("en").setRegion("IN").build()

/** Formats paise as an INR string, e.g. 123450 -> "₹1,234.50". */
fun formatMoney(amountMinor: Long, showDecimals: Boolean = true): String {
    val format = NumberFormat.getCurrencyInstance(indiaLocale).apply {
        maximumFractionDigits = if (showDecimals) 2 else 0
        minimumFractionDigits = if (showDecimals && amountMinor % 100 != 0L) 2 else 0
    }
    val formatted = format.format(abs(amountMinor) / 100.0)
    return if (amountMinor < 0) "-$formatted" else formatted
}

/** Parses keypad input such as "1234.5" into paise. Returns null when invalid. */
fun parseToMinor(input: String): Long? {
    if (input.isBlank()) return null
    val value = input.toBigDecimalOrNull() ?: return null
    return value.movePointRight(2).toLong()
}

/** Converts paise back to editable keypad text. */
fun minorToInput(amountMinor: Long): String {
    val rupees = amountMinor / 100
    val paise = amountMinor % 100
    return if (paise == 0L) rupees.toString() else "%d.%02d".format(rupees, paise).trimEnd('0')
}

private val random = SecureRandom()

/** Time-ordered UUIDv7. */
fun uuidV7(now: Long = System.currentTimeMillis()): String {
    val randA = random.nextInt(1 shl 12).toLong()
    val msb = (now shl 16) or (0x7L shl 12) or randA
    val lsb = (random.nextLong() and 0x3FFFFFFFFFFFFFFFL) or Long.MIN_VALUE
    return UUID(msb, lsb).toString()
}

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun LocalDate.startMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()

/** Returns [start, end) epoch-ms bounds of the period containing [today]. */
fun periodBounds(
    period: BudgetPeriod,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): LongRange {
    val start = when (period) {
        BudgetPeriod.WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        BudgetPeriod.MONTHLY -> today.withDayOfMonth(1)
    }
    val end = when (period) {
        BudgetPeriod.WEEKLY -> start.plusWeeks(1)
        BudgetPeriod.MONTHLY -> start.plusMonths(1)
    }
    return start.startMillis(zone) until end.startMillis(zone)
}

private val dayHeaderFormat = DateTimeFormatter.ofPattern("EEE, d MMM", indiaLocale)
private val fullDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", indiaLocale)
private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy", indiaLocale)

fun dayHeader(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(dayHeaderFormat)
}

fun formatFullDate(date: LocalDate): String = date.format(fullDateFormat)

fun formatMonth(date: LocalDate): String = date.format(monthFormat)
