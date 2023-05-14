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

        binding.appToolbar.tbToolbar.title = "Workout"

        binding.cvOne.setOnClickListener {
            startActivity(Intent(this@WorkoutListActivity, PoseDetectorActivity::class.java))
        }
        binding.cvTwo.setOnClickListener {
            startActivity(Intent(this@WorkoutListActivity, PoseDetectorActivity::class.java))
        }
        binding.cvThree.setOnClickListener {
            startActivity(Intent(this@WorkoutListActivity, PoseDetectorActivity::class.java))
        }
    }
}