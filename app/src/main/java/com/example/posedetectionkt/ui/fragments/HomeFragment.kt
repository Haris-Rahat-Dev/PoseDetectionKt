package com.example.posedetectionkt.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.posedetectionkt.R
import com.example.posedetectionkt.ui.activities.BmiActivity
import com.example.posedetectionkt.ui.activities.PoseDetectorActivity
import com.example.posedetectionkt.ui.activities.StepsActivity
import com.example.posedetectionkt.ui.activities.WorkoutListActivity

class HomeFragment : Fragment() {

    private lateinit var bmiCard: CardView
    private lateinit var stepsCard: CardView
    private lateinit var workoutCard: CardView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        bmiCard = view.findViewById(R.id.bmiCardView)
        stepsCard = view.findViewById(R.id.stepsCardView)
        workoutCard = view.findViewById(R.id.workoutCardView)

        bmiCard.setOnClickListener {
            val intent = Intent(activity, BmiActivity::class.java)
            startActivity(intent)
        }

        stepsCard.setOnClickListener {
            val intent = Intent(activity, StepsActivity::class.java)
            startActivity(intent)
        }

        workoutCard.setOnClickListener {
            val intent = Intent(activity, WorkoutListActivity::class.java)
            intent.putExtra("pose", "pushup")
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return view
    }

}