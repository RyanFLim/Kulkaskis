package org.umn.kulkaskis.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity(tableName = "fridge_items")
data class FridgeItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int,
    val expiry: LocalDate,
    val category: String = "Other",
    val purchaseDate: LocalDate = LocalDate.now(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
    val dirty: Boolean = true
) {
    val daysLeft: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expiry)

    val isExpiringSoon: Boolean
        get() = daysLeft in 0..3
}

private fun day(offset: Long): LocalDate = LocalDate.now().plusDays(offset)

/**
 * Dummy data: 10 realistic items covering every state
 * (fresh, expiring soon, expired) and every category.
 * Fixed ids so seeding twice never creates duplicates.
 * dirty = false so demo data is never uploaded to the server.
 */
val sampleItems = listOf(
    FridgeItem("demo-01", "Fresh Milk", 1, day(2), "Dairy", day(-5), dirty = false),
    FridgeItem("demo-02", "Eggs", 10, day(7), "Dairy", day(-3), dirty = false),
    FridgeItem("demo-03", "Chicken Breast", 1, day(1), "Meat", day(-2), dirty = false),
    FridgeItem("demo-04", "Spinach", 2, day(3), "Vegetables", day(-4), dirty = false),
    FridgeItem("demo-05", "Apple", 6, day(10), "Fruits", day(-1), dirty = false),
    FridgeItem("demo-06", "Yogurt", 3, day(3), "Dairy", day(-6), dirty = false),
    FridgeItem("demo-07", "Cheddar Cheese", 1, day(21), "Dairy", day(-8), dirty = false),
    FridgeItem("demo-08", "Tomato", 4, day(-2), "Vegetables", day(-9), dirty = false),
    FridgeItem("demo-09", "Banana", 5, day(5), "Fruits", day(-2), dirty = false),
    FridgeItem("demo-10", "Rice", 1, day(180), "Other", day(-30), dirty = false)
)