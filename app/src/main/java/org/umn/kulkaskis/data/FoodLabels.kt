package org.umn.kulkaskis.data

data class FoodInfo(val category: String, val days: Int)

private val foodLabels = mapOf(
    // Fruits
    "apple" to FoodInfo("Fruits", 10),
    "banana" to FoodInfo("Fruits", 5),
    "orange" to FoodInfo("Fruits", 10),
    "lemon" to FoodInfo("Fruits", 14),
    "strawberry" to FoodInfo("Fruits", 4),
    "grape" to FoodInfo("Fruits", 7),
    "pineapple" to FoodInfo("Fruits", 5),
    "watermelon" to FoodInfo("Fruits", 5),
    "mango" to FoodInfo("Fruits", 5),
    "pear" to FoodInfo("Fruits", 7),
    "fruit" to FoodInfo("Fruits", 7),
    // Vegetables
    "vegetable" to FoodInfo("Vegetables", 6),
    "tomato" to FoodInfo("Vegetables", 6),
    "carrot" to FoodInfo("Vegetables", 14),
    "broccoli" to FoodInfo("Vegetables", 5),
    "cucumber" to FoodInfo("Vegetables", 6),
    "potato" to FoodInfo("Vegetables", 21),
    "onion" to FoodInfo("Vegetables", 21),
    "cabbage" to FoodInfo("Vegetables", 10),
    "lettuce" to FoodInfo("Vegetables", 5),
    "garlic" to FoodInfo("Vegetables", 30),
    "mushroom" to FoodInfo("Vegetables", 5),
    "corn" to FoodInfo("Vegetables", 5),
    "salad" to FoodInfo("Vegetables", 3),
    // Dairy & eggs
    "milk" to FoodInfo("Dairy", 5),
    "egg" to FoodInfo("Dairy", 21),
    "cheese" to FoodInfo("Dairy", 14),
    "yogurt" to FoodInfo("Dairy", 7),
    "butter" to FoodInfo("Dairy", 30),
    // Meat & fish
    "meat" to FoodInfo("Meat", 2),
    "chicken" to FoodInfo("Meat", 2),
    "beef" to FoodInfo("Meat", 2),
    "fish" to FoodInfo("Meat", 2),
    "seafood" to FoodInfo("Meat", 2),
    "sausage" to FoodInfo("Meat", 5),
    "bacon" to FoodInfo("Meat", 7),
    // Other
    "bread" to FoodInfo("Other", 5),
    "food" to FoodInfo("Other", 5),
    "drink" to FoodInfo("Other", 7),
    "juice" to FoodInfo("Other", 7),
    "cake" to FoodInfo("Other", 3),
    "pizza" to FoodInfo("Other", 3),
    "dish" to FoodInfo("Other", 2)
)

fun lookupFood(label: String): FoodInfo? = foodLabels[label.lowercase()]