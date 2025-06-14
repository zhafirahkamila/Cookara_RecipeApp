package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ac.binus.recipeapp.adapter.CategoryAdapter
import id.ac.binus.recipeapp.adapter.DB.Companion.getOrCreateRecipe
import id.ac.binus.recipeapp.adapter.RecipeAdapter
import id.ac.binus.recipeapp.model.Category
import id.ac.binus.recipeapp.model.Ingredients
import id.ac.binus.recipeapp.model.Recipe
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var recipeList: MutableList<Recipe>
    private lateinit var adapter2: RecipeAdapter
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerCategory)
        val recyclerRecipe: RecyclerView = findViewById(R.id.recyclerRecipe)
        val ivHome: ImageView = findViewById(R.id.ivHome)
        val seeAllCat: TextView = findViewById(R.id.seeAllCat)
        val ivCategory: ImageView = findViewById(R.id.ivCategory)
        val ivProfile: ImageView = findViewById(R.id.ivProfile)
        val tvUser: TextView = findViewById(R.id.tvUser)
        val edtSearch: EditText = findViewById(R.id.edtSearch)
        val ivFavorite: ImageView = findViewById(R.id.ivFavorite)
        val tvRecipes: TextView = findViewById(R.id.tvRecipes)

        ivFavorite.setOnClickListener {
            val intent = Intent(this, FavoritePage::class.java)
            startActivity(intent)
        }

        val categoryList = mutableListOf<Category>()
        adapter = CategoryAdapter(categoryList) { categoryName ->
            if (categoryName == "All") {
                fetchRecipes("", recipeList, adapter2)
                tvRecipes.text = "All Recipes"
            } else {
                fetchRecipesByCategory(categoryName, recipeList, adapter2)
                tvRecipes.text = "$categoryName Recipes"
            }
        }

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        recipeList = mutableListOf()
        adapter2 = RecipeAdapter(recipeList,
            onItemClick =  { recipe ->
                val intent = Intent(this, DetailPage::class.java)
                intent.putExtra("name", recipe.name)
                intent.putExtra("category", recipe.category)
                intent.putExtra("image", recipe.imageResId)
                intent.putExtra("rating", recipe.rating)
                intent.putExtra("duration", recipe.duration)
                intent.putExtra("instructions", recipe.instructions)
                intent.putExtra("videos", recipe.videos)
                intent.putParcelableArrayListExtra("ingredients", ArrayList(recipe.ingredients))
                startActivity(intent)
            },
            onFavoriteClick = { recipe ->
                toggleFavorite(recipe)
            }
        )

        recyclerRecipe.layoutManager = GridLayoutManager(this, 2)
        recyclerRecipe.adapter = adapter2

        fetchRecipes("", recipeList, adapter2)
        fetchCategories(categoryList, adapter)

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                fetchRecipes("", recipeList, adapter2)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(uid)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "User"
                    tvUser.text = "Hi, $username"
                } else {
                    tvUser.text = "Hi, User"
                }
            }.addOnFailureListener {
                tvUser.text = "Hi, User"
            }
        } else {
            tvUser.text = "Hi, User"
        }

        ivProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        ivCategory.setOnClickListener {
            val intent = Intent(this, CategoryPage::class.java)
            startActivity(intent)
        }

        seeAllCat.setOnClickListener {
            val intent = Intent(this, CategoryPage::class.java)
            startActivity(intent)
        }

        ivHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }
    }

    private fun fetchRecipes(keyword: String, recipeList: MutableList<Recipe>, adapter: RecipeAdapter) {
        val url = "https://www.themealdb.com/api/json/v1/1/search.php?s=$keyword"
        val requestQueue = Volley.newRequestQueue(this)

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                recipeList.clear()
                val meals = response.optJSONArray("meals")
                if (meals != null) {
                    val fetchedRecipes = mutableListOf<Recipe>()

                    for (i in 0 until meals.length()) {
                        val meal = meals.getJSONObject(i)
                        val name = meal.getString("strMeal")
                        val category = meal.getString("strCategory")
                        val imageResId = meal.getString("strMealThumb")
                        val instructions = meal.getString("strInstructions")
                        val videos = meal.getString("strYoutube")

                        val ingredientsList = mutableListOf<Ingredients>()
                        for (index in 1..20) {  // TheMealDB max 20 ingredients
                            val ingredient = meal.optString("strIngredient$index", "").trim()
                            val measure = meal.optString("strMeasure$index", "").trim()

                            if (ingredient.isNotEmpty() && ingredient != "null") {
                                ingredientsList.add(Ingredients(name = ingredient, gram = measure))
                            }
                        }

                        val recipe = getOrCreateRecipe(this, name, category, imageResId, instructions, videos, ingredientsList)
                        fetchedRecipes.add(recipe)
                    }
                    if (user != null) {
                        db.collection("favorite").document(user.uid).collection("recipes")
                            .get().addOnSuccessListener { favorites ->
                                val favoriteNames = favorites.map { it.id } // assuming 'name' is used as doc ID

                                for (recipe in fetchedRecipes) {
                                    if (recipe.name in favoriteNames) {
                                        recipe.isFavorite = true
                                    }
                                }

                                recipeList.addAll(fetchedRecipes)
                                adapter.notifyDataSetChanged()
                            }
                    } else {
                        recipeList.addAll(fetchedRecipes)
                        adapter.notifyDataSetChanged()
                    }
                }
            },
            { error -> error.printStackTrace() }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchCategories(categoryList: MutableList<Category>, adapter: CategoryAdapter) {
        val url = "https://www.themealdb.com/api/json/v1/1/categories.php"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                categoryList.clear()

                // ➕ Tambahkan kategori "All" manual
                val allCategory = Category(name = "All", imageResId = "allrecipes") // image kosong, bisa diisi icon lokal
                categoryList.add(allCategory)

                val categories = response.getJSONArray("categories")
                for (i in 0 until categories.length()) {
                    val category = categories.getJSONObject(i)
                    val name = category.getString("strCategory")
                    val imageResId = category.getString("strCategoryThumb")

                    val categoryObj = Category(
                        name = name,
                        imageResId = imageResId
                    )
                    categoryList.add(categoryObj)
                }
                adapter.notifyDataSetChanged()
            },
            { error -> error.printStackTrace() }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun toggleFavorite(recipe: Recipe) {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            val uid = user.uid
            val favRef = db.collection("favorite").document(uid).collection("recipes").document(recipe.name)

            favRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    favRef.delete().addOnSuccessListener {
                        recipe.isFavorite = false
                        adapter2.notifyDataSetChanged() // refresh
                    }
                } else {
                    val favorite = hashMapOf(
                        "name" to recipe.name,
                        "image" to recipe.imageResId,
                        "category" to recipe.category,
                        "duration" to recipe.duration,
                        "rating" to recipe.rating
                    )
                    favRef.set(favorite).addOnSuccessListener {
                        recipe.isFavorite = true
                        adapter2.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun fetchRecipesByCategory(category: String, recipeList: MutableList<Recipe>, adapter: RecipeAdapter) {
        val url = "https://www.themealdb.com/api/json/v1/1/filter.php?c=$category"
        val requestQueue = Volley.newRequestQueue(this)

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                recipeList.clear()
                val meals = response.optJSONArray("meals")
                if (meals != null) {
                    val fetchedRecipes = mutableListOf<Recipe>()

                    for (i in 0 until meals.length()) {
                        val meal = meals.getJSONObject(i)
                        val name = meal.getString("strMeal")
                        val imageResId = meal.getString("strMealThumb")
//                        val idMeal = meal.getString("idMeal")

                        val recipe = getOrCreateRecipe(
                            this,
                            name,
                            category,
                            imageResId,
                            instructions = "",
                            videos = "",
                            ingredients = listOf()
                        )
                        fetchedRecipes.add(recipe)
                    }

                    if (user != null) {
                        db.collection("favorite").document(user.uid).collection("recipes")
                            .get().addOnSuccessListener { favorites ->
                                val favoriteNames = favorites.map { it.id }
                                for (recipe in fetchedRecipes) {
                                    if (recipe.name in favoriteNames) {
                                        recipe.isFavorite = true
                                    }
                                }
                                recipeList.addAll(fetchedRecipes)
                                adapter.notifyDataSetChanged()
                            }
                    } else {
                        recipeList.addAll(fetchedRecipes)
                        adapter.notifyDataSetChanged()
                    }
                }
            },
            { error -> error.printStackTrace() }
        )
        requestQueue.add(jsonObjectRequest)
    }

}
