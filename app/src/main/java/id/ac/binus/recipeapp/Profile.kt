package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ac.binus.recipeapp.adapter.FavoriteAdapter
import kotlin.math.log

class Profile : AppCompatActivity() {
    private var favoriteList: MutableList<Favorite> = mutableListOf()
    private lateinit var adapter: FavoriteAdapter
    private val db = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var recentlyDeleted: Favorite? = null
    private var recentlyDeletedPosition = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val ivHome: ImageView = findViewById(R.id.ivHome)
        val ivFavorite: ImageView = findViewById(R.id.ivFavorite)
        val ivCategory: ImageView = findViewById(R.id.ivCategory)
        val tvName: TextView = findViewById(R.id.tvName)
        val tvUsername: TextView = findViewById(R.id.tvUsername)
        val btnEditProfile: AppCompatButton = findViewById(R.id.btnEditProfile)
        val btnLogOut: AppCompatButton = findViewById(R.id.btnLogOut)
        val profileImage: ImageView = findViewById(R.id.profileImage)
        val countFollowing: TextView = findViewById(R.id.countFollowing)
        val countFollowers: TextView = findViewById(R.id.countFollowers)
        val countFavorite: TextView = findViewById(R.id.countFavorite)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerFavorite)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = FavoriteAdapter(favoriteList,
            onItemClick = { /* arah ke detail jika mau */ },
            onFavoriteClick = { fav, pos ->
                deleteFavorite(fav, pos)
            }
        )
        recyclerView.adapter = adapter

        fetchFavorites()

        val name = FirebaseAuth.getInstance().currentUser
        val username = name?.displayName ?: "User"
        tvName.text = "$username"

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val docRef = db.collection("users").document(uid)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "User"
                    val profileImageUrl = document.getString("profileImageUrl")

                    tvUsername.text = "@$username"
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.kamari) // optional placeholder
                            .error(R.drawable.kamari) // in case of error
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.kamari)
                    }

                    val followers = document.getLong("followers")
                    if (followers != null) {
                        countFollowers.text = followers.toString()
                    } else {
                        val ranFollowers = (25..500).random()
                        countFollowers.text = ranFollowers.toString()
                        docRef.update("followers", ranFollowers).addOnSuccessListener {
                            Log.e("Profile", "Failed to save followers")
                        }
                    }

                    val following = document.getLong("following")
                    if (following != null) {
                        countFollowing.text = following.toString()
                    } else {
                        val ranFollowing = (25..500).random()
                        countFollowing.text = ranFollowing.toString()
                        docRef.update("following", ranFollowing).addOnSuccessListener {
                            Log.e("Profile", "Failed to save following")
                        }
                    }
                } else {
                    tvUsername.text = "@Username"
                    countFollowers.text = "0"
                    countFollowing.text = "0"
                }
            }.addOnFailureListener {
                tvUsername.text = "@Username"
                countFollowers.text = "0"
                countFollowing.text = "0"
            }

            db.collection("favorite").document(uid).collection("recipes")
                .get()
                .addOnSuccessListener { document ->
                    countFavorite.text = document.size().toString()
                }
                .addOnFailureListener{
                    countFavorite.text = "0"
                }
        }

        ivHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ivFavorite.setOnClickListener {
            val intent = Intent(this, FavoritePage::class.java)
            startActivity(intent)
        }

        ivCategory.setOnClickListener {
            val intent = Intent(this, CategoryPage::class.java)
            startActivity(intent)
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        btnLogOut.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure you want to log out from your account?")
            builder.setIcon(R.drawable.logout)

            builder.setPositiveButton("Yes") { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        }

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