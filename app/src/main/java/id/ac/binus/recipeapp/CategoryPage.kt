package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import id.ac.binus.recipeapp.adapter.CategoriesAdapter
import id.ac.binus.recipeapp.model.Category

class CategoryPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_page)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerCategory)
        val ivBack: ImageView = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val categoryList = mutableListOf<Category>()
        val adapter = CategoriesAdapter(categoryList) { categoryName ->

        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

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
}