package com.example.posedetectionkt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.R
import com.example.posedetectionkt.utils.Loading
import com.example.posedetectionkt.utils.WindowManager
import com.example.posedetectionkt.utils.preference.UserDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var registerButton: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loading: Loading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        WindowManager.statusBarManager(this, R.color.white, true)

        loading = Loading(this)

        loginButton = findViewById(R.id.login)
        registerButton = findViewById(R.id.register)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            loading.show()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // get the user  from the users collection
                            val user = auth.currentUser
                            val uid = user!!.uid
                            // get the user name and email
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        val name = document.getString("name")
                                        // store the user in the phone memory
                                        UserDetails(this@LoginActivity).setUserName(name!!)
                                        UserDetails(this@LoginActivity).setUserEmail(email)
                                        UserDetails(this@LoginActivity).setIsUserLoggedIn(true)

                                        loading.dismiss()
                                        Toast.makeText(
                                            baseContext, "Welcome $name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(
                                                this@LoginActivity,
                                                DashboardActivity::class.java
                                            )
                                        startActivity(intent)
                                    } else {
                                        Log.d("TAG", "No such document")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    loading.dismiss()
                                    Log.d("TAG", "get failed with ", exception)
                                    Toast.makeText(
                                        baseContext, exception.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            loading.dismiss()
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, task.exception!!.message,
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
            } else {
                loading.dismiss()
                Toast.makeText(
                    baseContext, "Please fill all the fields",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}