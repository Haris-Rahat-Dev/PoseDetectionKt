package com.example.posedetectionkt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.posedetectionkt.R
import com.example.posedetectionkt.utils.WindowManager
import com.example.posedetectionkt.utils.preference.UserDetails

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_splash)

        WindowManager.statusBarManager(
            this,
            R.color.white, true
        )

        Handler(Looper.getMainLooper()).postDelayed({
            if (UserDetails(this@SplashActivity).getIsUserLoggedIn()) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000)
    }
}