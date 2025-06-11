package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ac.binus.recipeapp.adapter.FavoriteAdapter

class FavoritePage : AppCompatActivity() {
    private var favoriteList: MutableList<Favorite> = mutableListOf()
    private lateinit var adapter: FavoriteAdapter
    private val db = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var recentlyDeleted: Favorite? = null
    private var recentlyDeletedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorite_page)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerFavorite)
        val ivBack: ImageView = findViewById(R.id.ivBack)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = FavoriteAdapter(favoriteList,
            onItemClick = { /* arah ke detail jika mau */ },
            onFavoriteClick = { fav, pos ->
                deleteFavorite(fav, pos)
            }
        )
        recyclerView.adapter = adapter

        ivBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        fetchFavorites()
    }

    private fun fetchFavorites() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            db.collection("favorite").document(user.uid).collection("recipes")
                .get()
                .addOnSuccessListener { documents ->
                    favoriteList.clear()
                    for (document in documents) {
                        val name = document.getString("name") ?: ""
                        val category = document.getString("category") ?: ""
                        val rating = document.getDouble("rating") ?: 0.0
                        val duration = document.getString("duration") ?: ""
                        val imageResId = document.getString("image") ?: ""

                        val favorite = Favorite(name, category, rating, duration, imageResId)
                        favoriteList.add(favorite)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteFavorite(fav: Favorite, position: Int) {
        if (uid.isEmpty()) return
        db.collection("favorite").document(uid).collection("recipes")
            .document(fav.name)
            .delete()
            .addOnSuccessListener {
                recentlyDeleted = fav
                recentlyDeletedPosition = position
                favoriteList.removeAt(position)
                adapter.notifyItemRemoved(position)

                Snackbar.make(findViewById(R.id.recyclerFavorite), "Removed from favorites", Snackbar.LENGTH_LONG)
                    .setAction("Undo") { undoDelete() }
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show()
            }
    }

    private fun undoDelete() {
        val fav = recentlyDeleted ?: return
        if (uid.isEmpty()) return

        db.collection("favorite").document(uid).collection("recipes")
            .document(fav.name)
            .set(hashMapOf(
                "name" to fav.name,
                "category" to fav.category,
                "rating" to fav.rating,
                "duration" to fav.duration,
                "image" to fav.imageResId
            ))
            .addOnSuccessListener {
                favoriteList.add(recentlyDeletedPosition, fav)
                adapter.notifyItemInserted(recentlyDeletedPosition)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to undo", Toast.LENGTH_SHORT).show()
            }
    }
}