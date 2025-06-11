package id.ac.binus.recipeapp

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleIcon = findViewById<ImageView>(R.id.google_icon)
        googleIcon.setOnClickListener {
            signInWithGoogle()
        }

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvPassword = findViewById<TextView>(R.id.tvPassword)
        val tvConfirm = findViewById<TextView>(R.id.tvConfirm)
        val txtHaveAcc = findViewById<TextView>(R.id.txtHaveAcc)
        val btnSignUp = findViewById<AppCompatButton>(R.id.btnSignUp)
        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirm = findViewById<EditText>(R.id.edtConfirm)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)

        setColoredText(tvEmail, "Gmail*", "*")
        setColoredText(tvUsername, "Username*", "*")
        setColoredText(tvPassword, "Password*", "*")
        setColoredText(tvConfirm, "Confirm Password*", "*")
        setColoredText(txtHaveAcc, "Already have an account? Login Here!", "Login Here!")

        btnSignUp.setOnClickListener {
            Log.d("SignUp", "SignUp button clicked")

            val username = edtUsername.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmPass = edtConfirm.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirmPass.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(this, "Use a valid Gmail address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUp", "Auth successful")

                        val uid = auth.currentUser?.uid
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "email" to email
                        )

                        uid?.let {
                            db.collection("users").document(it).set(userMap)
                                .addOnSuccessListener {
                                    Log.d("SignUp", "Firestore save success")
                                    navigateToLoginPage(username, email)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("SignUp", "Firestore error: ${e.message}", e)
                                }
                        } ?: run {
                            Log.e("SignUp", "UID null")
                        }
                    } else {
                        Log.e("SignUp", "Auth failed: ${task.exception?.message}")
                    }
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
                0, fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.required)),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Failed: ${e.message}")
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val email = user?.email ?: "no-email"
                    val username = user?.displayName ?: "Unknown"

                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true

                    if (isNewUser && uid != null) {
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "email" to email
                        )

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                Log.d("SignUp", "Google user saved to Firestore")
                                navigateToLoginPage(username, email)
                            }
                            .addOnFailureListener { e ->
                                Log.e("SignUp", "Firestore error: ${e.message}")
                                Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        navigateToLoginPage(username, email)
                    }
                } else {
                    Log.e("SignUp", "Google Auth failed: ${task.exception?.message}")
                    Toast.makeText(this, "Google sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToLoginPage(username: String?, email: String?) {
        Log.d("SignUp", "navigateToLoginPage() CALLED")
        Toast.makeText(this, "Navigating to login...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginPage::class.java)
        intent.putExtra("username", username)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }
}