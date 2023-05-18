package com.example.posedetectionkt.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.utils.Loading
import com.example.posedetectionkt.R
import com.example.posedetectionkt.utils.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;
    private lateinit var loginButton: TextView
    private lateinit var registerButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loading: Loading

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        WindowManager.statusBarManager(
            this,
            R.color.white, true
        )
        loading = Loading(this)

        auth = FirebaseAuth.getInstance()

        loginButton = findViewById(R.id.login)

        registerButton = findViewById(R.id.register)

        nameEditText = findViewById(R.id.name)

        emailEditText = findViewById(R.id.email)

        passwordEditText = findViewById(R.id.password)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            loading.show()
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // take the user id and save it in the users collection
                            val user = auth.currentUser
                            val uid = user!!.uid

                            // save the user data in the database
                            val userMap = HashMap<String, Any>()
                            /*userMap["uid"] = uid*/
                            userMap["name"] = name
                            userMap["email"] = email

                            // save the user data in the database
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener {
                                    Log.d("TAG", "DocumentSnapshot successfully written!")
                                }
                                .addOnFailureListener { exception ->
                                    Log.w("TAG", "Error writing document", exception)
                                    Toast.makeText(
                                        baseContext, exception.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            // go to the main activity
                            loading.dismiss()
                            Toast.makeText(
                                baseContext, "Authentication successful.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            loading.dismiss()
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                loading.dismiss()
                Toast.makeText(
                    baseContext, "Please fill all the fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        loginButton.setOnClickListener {
            // go back to the login activity without intent
            finish()
        }

    }
}