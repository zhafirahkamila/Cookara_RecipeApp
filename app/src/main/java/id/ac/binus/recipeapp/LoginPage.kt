package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    startMainActivity(auth.currentUser)
                                } else {
                                    Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Sign In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Setup views
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvPassword = findViewById<TextView>(R.id.tvPassword)
        val txtHaveAcc = findViewById<TextView>(R.id.txtHaveAcc)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val googleIcon = findViewById<ImageView>(R.id.google_icon)
        val tvForget = findViewById<TextView>(R.id.tvForget)
        val db = FirebaseFirestore.getInstance()

        // Set colored asterisks or links
        setColoredText(tvUsername, "Username*", "*")
        setColoredText(tvPassword, "Password*", "*")
        setColoredText(txtHaveAcc, "Didn’t have an account? Register Here!", "Register Here!")

        tvForget.setOnClickListener {
            val username = edtUsername.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter your username first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Ambil email berdasarkan username dari Firestore
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val email = document.getString("email")

                        if (email != null) {
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Email not found for this username", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Handle Email/Password Login
        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString()
            val password = edtPassword.text.toString()

            when {
                username.isEmpty() && password.isEmpty() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                username.isEmpty() -> {
                    Toast.makeText(this, "Username must be filled", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Password must be filled", Toast.LENGTH_SHORT).show()
                }
                password.length < 8 -> {
                    Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Ambil email berdasarkan username dari Firestore
                    db.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val document = documents.documents[0]
                                val email = document.getString("email")
                                if (email != null) {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                startMainActivity(auth.currentUser)
                                            } else {
                                                Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(this, "Email not found for this username", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        // Google Sign-In setup
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id)) // from google-services.json
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        googleIcon.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Google Sign In Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun startMainActivity(user: FirebaseUser?) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val uid = user.uid

            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "Unknown"
                        val email = document.getString("email") ?: "No email"

                        Toast.makeText(this, "Welcome $username", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("username", username)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "User data not found in Firestore.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setColoredText(textView: TextView, fullText: String, coloredPart: String) {
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(coloredPart)
        val end = start + coloredPart.length

        if (start != -1) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),
                0, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.required)),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
    }
}