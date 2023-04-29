package com.example.posedetectionkt

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth;
    private lateinit var loginButton: Button
    private lateinit var poseButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()

        loginButton = findViewById(R.id.loginButton)
        poseButton = findViewById(R.id.poseButton)

        loginButton.setOnClickListener{
            auth.signOut()
            // go back to the login activity without intent
            finish()
        }

        poseButton.setOnClickListener{
            // go to the pose activity
            val intent = Intent(this, PoseDetectorActivity::class.java)

            intent.putExtra("pose", "pushup")

            startActivity(intent)
        }

    }
}