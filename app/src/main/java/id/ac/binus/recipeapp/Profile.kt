package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val ivHome: ImageView = findViewById(R.id.ivHome)
        val ivProfile: ImageView = findViewById(R.id.ivProfile)
        val ivFavorite: ImageView = findViewById(R.id.ivFavorite)
        val ivCategory: ImageView = findViewById(R.id.ivCategory)
        val tvName: TextView = findViewById(R.id.tvName)
        val tvUsername: TextView = findViewById(R.id.tvUsername)

        val name = FirebaseAuth.getInstance().currentUser
        val username = name?.displayName ?: "User"
        tvName.text = "$username"

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(uid)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "User"
                    tvUsername.text = "@$username"
                } else {
                    tvUsername.text = "@Username"
                }
            }.addOnFailureListener {
                tvUsername.text = "@Username"
            }
        } else {
            tvUsername.text = "@Username"
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
    }
}