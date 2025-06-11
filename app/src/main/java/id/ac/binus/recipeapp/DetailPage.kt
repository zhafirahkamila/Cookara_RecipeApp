package id.ac.binus.recipeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.binus.recipeapp.adapter.IngredientsAdapter
import id.ac.binus.recipeapp.model.Ingredients
import id.ac.binus.recipeapp.model.Recipe
import org.w3c.dom.Text

class DetailPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_page)

        val name = intent.getStringExtra("name")
        val category = intent.getStringExtra("category")
        val image = intent.getStringExtra("image")
        val rating = intent.getDoubleExtra("rating", 0.0)
        val duration = intent.getStringExtra("duration")
        val instructions = intent.getStringExtra("instructions")
        val videos = intent.getStringExtra("videos")
        val ingredients = intent.getParcelableArrayListExtra<Ingredients>("ingredients") ?: arrayListOf()

        val recyclerIngredients: RecyclerView = findViewById(R.id.recyclerIngredients)
        val ingredientsAdapter = IngredientsAdapter(ingredients)
        recyclerIngredients.layoutManager = LinearLayoutManager(this)
        recyclerIngredients.adapter = ingredientsAdapter


        val backBtn: ImageView = findViewById(R.id.backBtn)
        val tvNameRecipe: TextView = findViewById(R.id.tvNameRecipe)
        val tvCategory: TextView = findViewById(R.id.tvCategory)
        val ivImage: ImageView = findViewById(R.id.ivImage)
        val tvTimer: TextView = findViewById(R.id.tvTimer)
        val tvRating: TextView = findViewById(R.id.tvRating)
        val tvInstruction: TextView = findViewById(R.id.tvInstruction)
        val btnToggle: TextView = findViewById(R.id.btnToggleInstructions)
        val btnVideos: AppCompatButton = findViewById(R.id.btnVideos)


        val formattedInstructions = instructions?.replace("\\r\\n", "\n") ?: ""

        val spannable = SpannableStringBuilder(formattedInstructions)

        val stepRegex = Regex("(STEP \\d+)")
        val matches = stepRegex.findAll(formattedInstructions)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start,
                end,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        var isExpanded = false
        btnToggle.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                tvInstruction.maxLines = Int.MAX_VALUE
                tvInstruction.ellipsize = null
                btnToggle.text = "See less"
            } else {
                tvInstruction.maxLines = 10
                tvInstruction.ellipsize = android.text.TextUtils.TruncateAt.END
                btnToggle.text = "See more..."
            }
        }

        tvNameRecipe.text = name
        tvCategory.text = category
        tvTimer.text = duration
        tvRating.text = String.format("%.1f", rating)
        tvInstruction.text = spannable
        tvInstruction.maxLines = 5
        tvInstruction.ellipsize = android.text.TextUtils.TruncateAt.END

        btnVideos.setOnClickListener {
            videos?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "No video available", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        Glide.with(this)
            .load(image)
            .into(ivImage)
    }
}