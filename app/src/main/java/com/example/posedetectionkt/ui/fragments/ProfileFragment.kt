package com.example.posedetectionkt.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.posedetectionkt.R
import com.example.posedetectionkt.utils.Loading
import com.example.posedetectionkt.utils.preference.UserDetails
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileName: EditText
    private lateinit var email: TextView
    private lateinit var updateButton: Button
    private lateinit var loading: Loading
    val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)
        profileName = view.findViewById(R.id.profile_name)
        email = view.findViewById(R.id.email)
        email.text = UserDetails(requireActivity()).getUserEmail()
        profileName.setText(UserDetails(requireActivity()).getUserName())
        loading = Loading(requireActivity())

        updateButton = view.findViewById(R.id.update)


        updateButton.setOnClickListener {
            loading.show()
            val name = profileName.text.toString()

            val userRef =
                db.collection("users").document(UserDetails(requireActivity()).getUserId())

            userRef.update("name", name)
                .addOnSuccessListener {
                    loading.dismiss()
                    UserDetails(requireActivity()).setUserName(name)
                    Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    loading.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Profile Update Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }


        // Inflate the layout for this fragment
        return view
    }

}