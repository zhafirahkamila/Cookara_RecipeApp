package id.ac.binus.recipeapp.model

data class Recipe(
    val name: String,
    val category: String,
    val rating: Double,
    val duration: String,
    val imageResId: String,
    val instructions: String,
    val videos: String,
    val ingredients: List<Ingredients> = listOf(),
    var isFavorite: Boolean = false
)
