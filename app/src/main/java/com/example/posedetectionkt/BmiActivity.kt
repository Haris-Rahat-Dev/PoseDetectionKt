package com.example.posedetectionkt

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import java.lang.Math.pow
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


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi)

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

        heightIncrement.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
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
            val bmi = calculateBMI(height, weight).toInt()
            // display the BMI
            displayBMI(bmi)
        }
    }

    private fun calculateBMI(height: Int, weight: Int): Int {
        return (weight.toDouble() / (height.toDouble() / 100).pow(2.toDouble())).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun displayBMI(bmi: Int) {
        // display the BMI and the result/interpretation
        bmiText.text = bmi.toString()
        if(bmi >= 25){
            resultText.text = "Overweight"
            interpretationText.text = "You have a higher than normal body weight. Try to exercise more."
        }else if(bmi >= 18.5){
            resultText.text = "Normal"
            interpretationText.text = "You have a normal body weight. Good job!"
        }else{
            resultText.text = "Underweight"
            interpretationText.text = "You have a lower than normal body weight. You should eat a bit more."
        }

    }

}