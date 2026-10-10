package com.my.moneyverse.data

import com.my.moneyverse.data.db.BudgetEntity
import com.my.moneyverse.data.db.BudgetPeriod
import com.my.moneyverse.data.db.PaymentMode
import com.my.moneyverse.data.db.PaymentMode.CARD
import com.my.moneyverse.data.db.PaymentMode.CASH
import com.my.moneyverse.data.db.PaymentMode.NETBANKING
import com.my.moneyverse.data.db.PaymentMode.UPI
import com.my.moneyverse.data.db.PaymentMode.WALLET
import com.my.moneyverse.data.db.TransactionEntity
import com.my.moneyverse.data.db.TxnType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.roundToLong

/**
 * Demo profile: 23-year-old software engineer living in Bengaluru, sharing a flat,
 * covering the current month and the two before it.
 */
object DemoData {
    private const val FOOD = "sys_exp_0"
    private const val GROCERIES = "sys_exp_1"
    private const val TRANSPORT = "sys_exp_2"
    private const val SHOPPING = "sys_exp_3"
    private const val BILLS = "sys_exp_4"
    private const val RENT = "sys_exp_5"
    private const val FUN = "sys_exp_6"
    private const val HEALTH = "sys_exp_7"
    private const val EDUCATION = "sys_exp_8"
    private const val TRAVEL = "sys_exp_9"
    private const val OTHER = "sys_exp_10"
    private const val SALARY = "sys_inc_0"
    private const val FREELANCE = "sys_inc_1"
    private const val GIFTS = "sys_inc_2"

    private class Item(
        val day: Int,
        val hour: Int,
        val rupees: Int,
        val categoryId: String,
        val mode: PaymentMode,
        val merchant: String,
        val note: String? = null,
        val type: TxnType = TxnType.EXPENSE,
        val fixed: Boolean = false,
        val months: Set<Int> = setOf(0, 1, 2),
    )

    private val items = listOf(
        // Income
        Item(1, 9, 68_000, SALARY, NETBANKING, "TechNova Solutions", "Monthly salary", TxnType.INCOME, fixed = true),
        Item(18, 19, 6_000, FREELANCE, UPI, "Freelance UI project", "Landing page design", TxnType.INCOME, months = setOf(1)),
        Item(14, 12, 3_000, GIFTS, UPI, "Mom", "Pocket money 💕", TxnType.INCOME, months = setOf(2)),
        // Fixed monthly
        Item(3, 10, 14_500, RENT, UPI, "Flat rent (2BHK share)", "Koramangala", fixed = true),
        Item(2, 7, 1_499, HEALTH, CARD, "Cult.fit", "Gym membership", fixed = true),
        Item(5, 21, 599, BILLS, UPI, "Airtel Postpaid", fixed = true),
        Item(8, 20, 820, BILLS, UPI, "BESCOM", "Electricity share"),
        Item(10, 20, 500, BILLS, UPI, "ACT Fibernet", "WiFi split", fixed = true),
        Item(12, 23, 199, FUN, CARD, "Netflix", "Subscription", fixed = true),
        Item(15, 8, 119, FUN, UPI, "Spotify Premium", fixed = true),
        Item(4, 9, 3_000, OTHER, NETBANKING, "SIP - Index Fund", "Monthly investment", fixed = true),
        // Food & coffee
        Item(2, 13, 180, FOOD, UPI, "Office cafeteria"),
        Item(4, 21, 420, FOOD, UPI, "Swiggy", "Biryani night"),
        Item(6, 10, 310, FOOD, UPI, "Third Wave Coffee"),
        Item(9, 20, 1_250, FOOD, CARD, "Toit", "Friday dinner with team"),
        Item(11, 22, 389, FOOD, UPI, "Zomato", "Late-night pizza"),
        Item(14, 10, 345, FOOD, UPI, "Starbucks", "Caramel macchiato"),
        Item(17, 13, 160, FOOD, UPI, "Office cafeteria"),
        Item(20, 21, 540, FOOD, UPI, "Swiggy", "Momos & noodles"),
        Item(23, 12, 890, FOOD, CARD, "Truffles", "Brunch with roommates"),
        Item(26, 20, 299, FOOD, UPI, "Zomato", "Salad bowl"),
        Item(28, 17, 120, FOOD, CASH, "Chai Point"),
        // Groceries
        Item(3, 19, 860, GROCERIES, UPI, "Blinkit", "Weekly essentials"),
        Item(10, 18, 640, GROCERIES, UPI, "Zepto", "Fruits & milk"),
        Item(17, 19, 1_120, GROCERIES, UPI, "BigBasket", "Monthly stock-up"),
        Item(24, 20, 480, GROCERIES, UPI, "Blinkit", "Snacks"),
        // Transport
        Item(2, 9, 210, TRANSPORT, UPI, "Rapido", "Bike to office"),
        Item(7, 19, 340, TRANSPORT, UPI, "Uber", "Cab home (rain)"),
        Item(11, 9, 500, TRANSPORT, UPI, "Namma Metro", "Card recharge"),
        Item(16, 22, 285, TRANSPORT, UPI, "Ola", "Back from Indiranagar"),
        Item(22, 9, 190, TRANSPORT, UPI, "Rapido"),
        Item(27, 18, 260, TRANSPORT, WALLET, "Uber"),
        // Shopping & self-care
        Item(6, 22, 1_899, SHOPPING, CARD, "Myntra", "Kurta set", months = setOf(0, 2)),
        Item(13, 21, 1_340, SHOPPING, UPI, "Nykaa", "Skincare restock"),
        Item(19, 17, 2_490, SHOPPING, CARD, "Zara", "Office blazer", months = setOf(1)),
        Item(21, 20, 799, SHOPPING, UPI, "Amazon", "Phone case & charger"),
        Item(25, 16, 1_200, HEALTH, UPI, "Salon - Naturals", "Haircut & spa", months = setOf(0, 2)),
        Item(18, 11, 450, HEALTH, UPI, "Apollo Pharmacy", months = setOf(1)),
        // Fun, learning, travel, gifts
        Item(8, 18, 650, FUN, UPI, "PVR Cinemas", "Movie + popcorn"),
        Item(22, 19, 1_499, FUN, UPI, "BookMyShow", "Stand-up comedy show", months = setOf(1)),
        Item(20, 21, 499, EDUCATION, CARD, "Udemy", "System Design course", months = setOf(0, 2)),
        Item(9, 12, 6_800, TRAVEL, CARD, "IndiGo", "BLR → GOA weekend trip", months = setOf(1)),
        Item(11, 15, 4_200, TRAVEL, UPI, "Airbnb Goa", "2 nights, split with friends", months = setOf(1)),
        Item(12, 20, 1_600, TRAVEL, CASH, "Goa beach shacks", months = setOf(1)),
        Item(16, 18, 1_100, OTHER, UPI, "Archies", "Bestie's birthday gift", months = setOf(2)),
    )

    private val variation = listOf(1.0, 0.9, 1.1)

    fun transactions(today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): List<TransactionEntity> {
        val now = LocalDateTime.now(zone)
        return (0..2).flatMap { offset ->
            val month = today.minusMonths(offset.toLong())
            items.withIndex().filter { offset in it.value.months }.mapNotNull { (index, item) ->
                val date = month.withDayOfMonth(item.day.coerceAtMost(month.lengthOfMonth()))
                val at = date.atTime(item.hour, (index * 7) % 60)
                if (at.isAfter(now)) return@mapNotNull null
                val factor = if (item.fixed) 1.0 else variation[(offset + index) % variation.size]
                val millis = at.atZone(zone).toInstant().toEpochMilli()
                TransactionEntity(
                    id = "demo_${offset}_$index",
                    amountMinor = (item.rupees * factor).roundToLong() * 100,
                    type = item.type,
                    categoryId = item.categoryId,
                    merchant = item.merchant,
                    note = item.note,
                    paymentMode = item.mode,
                    occurredAt = millis,
                    zoneId = zone.id,
                    createdAt = millis,
                    updatedAt = millis,
                )
            }
        }
    }

    fun budgets(now: Long = System.currentTimeMillis()): List<BudgetEntity> = listOf(
        BudgetEntity("demo_budget_overall", null, 45_000_00, BudgetPeriod.MONTHLY, now, now, now),
        BudgetEntity("demo_budget_food", FOOD, 6_000_00, BudgetPeriod.MONTHLY, now, now + 1, now),
        BudgetEntity("demo_budget_shopping", SHOPPING, 4_000_00, BudgetPeriod.MONTHLY, now, now + 2, now),
        BudgetEntity("demo_budget_transport", TRANSPORT, 1_000_00, BudgetPeriod.WEEKLY, now, now + 3, now),
    )
}
