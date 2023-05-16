package com.example.posedetectionkt.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.ActivityWorkoutListBinding
import com.example.posedetectionkt.utils.WindowManager

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowManager.statusBarManager(
            this,
            R.color.my_light_primary,
            false
        )

        val intent = Intent(this@WorkoutListActivity, PoseDetectorActivity::class.java)

        binding.appToolbar.tbToolbar.title = "Workout"
        binding.appToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }

        binding.cvOne.setOnClickListener {
            intent.putExtra("pose", "pushup")
            startActivity(intent)
        }
        binding.cvTwo.setOnClickListener {
            intent.putExtra("pose", "squat")
            startActivity(intent)
        }
        binding.cvThree.setOnClickListener {
            intent.putExtra("pose", "plank")
            startActivity(intent)
        }
    }
}