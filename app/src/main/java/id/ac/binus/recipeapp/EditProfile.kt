package id.ac.binus.recipeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text

class EditProfile : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        val edtUsername: EditText = findViewById(R.id.edtUsername)
        val edtEmail: EditText = findViewById(R.id.edtEmail)
        val edtOldPassword: EditText = findViewById(R.id.edtOldPassword)
        val edtNewPassword: EditText = findViewById(R.id.edtNewPassword)
        val btnSave: AppCompatButton = findViewById(R.id.btnSave)
        val tvChange: TextView = findViewById(R.id.tvChange)
        val ivBack: ImageView = findViewById(R.id.ivBack)

        profileImage = findViewById(R.id.profileImage)
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    profileImage.setImageURI(selectedImageUri)

                    // Upload ke Firebase Storage
                    val imageRef = storage.reference.child("profile_images/$uid.jpg")
                    imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                db.collection("users").document(uid)
                                    .update("profileImageUrl", uri.toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        tvChange.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        ivBack.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        if (user != null) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(uid)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "User"
                    val email = document.getString("email") ?: "email@example.com"

                    edtUsername.setText("$username")
                    edtEmail.setText(email)
                }
            }.addOnFailureListener {
                edtUsername.setText("Username")
                edtEmail.setText("email@example.com")
            }
            btnSave.setOnClickListener {
                val newUsername = edtUsername.text.toString().trim()
                val newEmail = edtEmail.text.toString().trim()
                val oldPassword = edtOldPassword.text.toString()
                val newPassword = edtNewPassword.text.toString()

                if (newUsername.isEmpty() || newEmail.isEmpty() || oldPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Show confirmation dialog
                AlertDialog.Builder(this)
                    .setTitle("Confirm Changes")
                    .setMessage("Are you sure you want to update your profile?")
                    .setPositiveButton("Yes") { _, _ ->
                        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                if (newEmail != user.email) {
                                    user.updateEmail(newEmail)
                                        .addOnSuccessListener {
                                            updateFirestore(uid, db, newUsername, newEmail)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to update email: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    updateFirestore(uid, db, newUsername, newEmail)
                                }

                                if (newPassword.isNotEmpty()) {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to update password: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Authentication failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun updateFirestore(uid: String, db: FirebaseFirestore, username: String, email: String) {
        val updates = mapOf(
            "username" to username,
            "email" to email
        )
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}