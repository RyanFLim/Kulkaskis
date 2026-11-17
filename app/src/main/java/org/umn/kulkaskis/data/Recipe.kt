package org.umn.kulkaskis.data

data class Recipe(
    val id: Int,
    val name: String,
    val emoji: String,
    val minutes: Int,
    val ingredients: List<String>,
    val steps: List<String>
)

val sampleRecipes = listOf(
    Recipe(
        1, "Spinach Omelette", "🍳", 10,
        listOf("egg", "spinach", "milk"),
        listOf(
            "Kocok telur dengan sedikit susu.",
            "Tumis bayam sebentar di wajan.",
            "Tuang telur, masak hingga matang, lipat dan sajikan."
        )
    ),
    Recipe(
        2, "Chicken Spinach Stir-fry", "🍗", 20,
        listOf("chicken", "spinach", "garlic", "rice"),
        listOf(
            "Potong ayam dan tumis dengan bawang putih hingga matang.",
            "Masukkan bayam, aduk sampai layu.",
            "Sajikan dengan nasi hangat."
        )
    ),
    Recipe(
        3, "Yogurt Apple Bowl", "🍎", 5,
        listOf("yogurt", "apple", "oats"),
        listOf(
            "Potong apel kecil-kecil.",
            "Tuang yogurt ke mangkuk, taruh apel dan oat di atasnya."
        )
    ),
    Recipe(
        4, "Chicken Cream Soup", "🍲", 35,
        listOf("chicken", "milk", "onion", "garlic"),
        listOf(
            "Rebus ayam hingga empuk, suwir dagingnya.",
            "Tumis bawang bombay dan bawang putih, masukkan ke kaldu.",
            "Tambahkan susu dan ayam suwir, masak 5 menit."
        )
    ),
    Recipe(
        5, "French Toast", "🍞", 15,
        listOf("bread", "egg", "milk"),
        listOf(
            "Campur telur dan susu di piring lebar.",
            "Celupkan roti ke campuran.",
            "Panggang di wajan hingga kedua sisi kecokelatan."
        )
    ),
    Recipe(
        6, "Apple Pancakes", "🥞", 20,
        listOf("apple", "egg", "milk", "flour"),
        listOf(
            "Campur tepung, telur, dan susu jadi adonan.",
            "Masukkan parutan apel.",
            "Tuang ke wajan, masak per sisi 2-3 menit."
        )
    ),
    Recipe(
        7, "Green Smoothie", "🥤", 5,
        listOf("spinach", "yogurt", "apple"),
        listOf(
            "Masukkan semua bahan ke blender.",
            "Blender hingga halus dan sajikan dingin."
        )
    ),
    Recipe(
        8, "Egg Fried Rice", "🍚", 15,
        listOf("egg", "rice", "spinach"),
        listOf(
            "Orak-arik telur, sisihkan.",
            "Tumis nasi dengan bayam.",
            "Campur telur kembali dan bumbui."
        )
    )
)