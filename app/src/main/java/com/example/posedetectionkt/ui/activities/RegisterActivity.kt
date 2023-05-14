package com.example.posedetectionkt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.utils.Loading
import com.example.posedetectionkt.R
import com.example.posedetectionkt.utils.WindowManager
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;
    private lateinit var loginButton: TextView
    private lateinit var registerButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loading: Loading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        WindowManager.statusBarManager(this,
            R.color.white,true)
        loading = Loading(this)

        auth = FirebaseAuth.getInstance()

        loginButton = findViewById(R.id.login)

        registerButton = findViewById(R.id.register)

        emailEditText = findViewById(R.id.email)

        passwordEditText = findViewById(R.id.password)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            loading.show()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        /*val user = auth.currentUser*/
                        // go to the main activity
                        loading.show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        loading.dismiss()
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                    }
                }
        }

        loginButton.setOnClickListener {
            // go back to the login activity without intent
            finish()
        }

    }
}