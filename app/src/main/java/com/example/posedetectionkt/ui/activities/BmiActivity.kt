package com.example.posedetectionkt.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.R
import com.example.posedetectionkt.databinding.ActivityBmiBinding
import com.example.posedetectionkt.utils.WindowManager
import kotlin.math.pow

class BmiActivity : AppCompatActivity() {

    private lateinit var heightValue: TextView
    private lateinit var heightIncrement: SeekBar

    private lateinit var weightValue: TextView
    private lateinit var weightIncrement: ImageView
    private lateinit var weightDecrement: ImageView

    private lateinit var ageValue: TextView
    private lateinit var ageIncrement: ImageView
    private lateinit var ageDecrement: ImageView

    private lateinit var calculateButton: Button

    private lateinit var bmiText: TextView
    private lateinit var resultText: TextView
    private lateinit var interpretationText: TextView

    private var height: Int = 120
    private var weight: Int = 40
    private var age: Int = 20

    private lateinit var binding: ActivityBmiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowManager.statusBarManager(
            this,
            R.color.my_light_primary,
            false
        )


        heightValue = findViewById(R.id.heightValue)
        heightIncrement = findViewById(R.id.heightIncrement)

        weightValue = findViewById(R.id.weightValue)
        weightIncrement = findViewById(R.id.weightIncrement)
        weightDecrement = findViewById(R.id.weightDecrement)

        ageValue = findViewById(R.id.ageValue)
        ageIncrement = findViewById(R.id.ageIncrement)
        ageDecrement = findViewById(R.id.ageDecrement)

        calculateButton = findViewById(R.id.calculate)

        bmiText = findViewById(R.id.bmi)
        resultText = findViewById(R.id.result)
        interpretationText = findViewById(R.id.interpretation)

        heightValue.text = height.toString()
        weightValue.text = weight.toString()
        ageValue.text = age.toString()

        heightIncrement.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                heightValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

        })

        weightIncrement.setOnClickListener {
            // increment the weight value
            weight++
            weightValue.text = weight.toString()
        }

        weightDecrement.setOnClickListener {
            // decrement the weight value
            weight--
            weightValue.text = weight.toString()
        }

        ageIncrement.setOnClickListener {
            // increment the age value
            age++
            ageValue.text = age.toString()
        }

        ageDecrement.setOnClickListener {
            // decrement the age value
            age--
            ageValue.text = age.toString()
        }

        calculateButton.setOnClickListener {
            // calculate the BMI
            val bmi = calculateBMI(height, weight)
            // display the BMI
            displayBMI(bmi)
        }

        binding.appToolbar.tbToolbar.title = "Calculate BMI"

        binding.appToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun calculateBMI(height: Int, weight: Int): Int {
        return (weight / (height / 100.0).pow(2)).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun displayBMI(bmi: Int) {
        // display the BMI and the result/interpretation
        // round the BMI to 2 decimal places
        bmiText.text = bmi.toString()
        when (bmi) {
            in 0..18 -> {
                resultText.text = "Underweight"
                interpretationText.text =
                    "You have a lower than normal body weight. You should eat a bit more."
                return
            }

            in 18..25 -> {
                resultText.text = "Normal"
                interpretationText.text = "You have a normal body weight. Good job!"
                return
            }

            else -> {
                resultText.text = "Overweight"
                interpretationText.text =
                    "You have a higher than normal body weight. Try to exercise more."
            }
        }
    }

}