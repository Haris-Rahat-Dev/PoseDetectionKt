package com.example.posedetectionkt.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.ActivityStepsBinding
import com.example.posedetectionkt.utils.WindowManager
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class StepsActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    private lateinit var stepsTaken: TextView
    private lateinit var stepProgressBar: CircularProgressBar
    private lateinit var resetButton: Button
    private lateinit var binding: ActivityStepsBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowManager.statusBarManager(
            this,
            R.color.my_light_primary,
            false
        )
        loadData()

        resetButton = findViewById(R.id.resetButton)
        stepsTaken = findViewById(R.id.stepsTaken)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        resetButton.setOnClickListener {
            resetSteps()
        }

        binding.appToolbar.tbToolbar.title = "Step Counter"

        binding.appToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            // display an error message
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_LONG).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            stepsTaken.text = ("$currentSteps")

            stepProgressBar.apply {
                setProgressWithAnimation(currentSteps.toFloat())
            }
        }
    }

    private fun resetSteps() {
        stepsTaken.setOnClickListener {
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_LONG).show()
        }

        stepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            stepsTaken.text = 0.toString()
            saveData()
            true
        }

    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("stepsCounter", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("previousSteps", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("stepsCounter", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("previousSteps", 0f)
        previousTotalSteps = savedNumber
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}