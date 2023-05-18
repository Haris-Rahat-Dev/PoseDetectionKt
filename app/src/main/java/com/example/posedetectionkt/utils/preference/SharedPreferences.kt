package com.example.posedetectionkt.utils.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class UserDetails(context: Context) {

    private var userDetail: SharedPreferences
    private var userDetailEditor: SharedPreferences.Editor

    init {
        userDetail = context.getSharedPreferences(
            "userDetail",
            AppCompatActivity.MODE_PRIVATE,
        )
        userDetailEditor = userDetail.edit()
    }

    fun setUserName(userName: String) {
        userDetailEditor.putString("name", userName)
        userDetailEditor.apply()
    }

    fun getUserName(): String {
        return userDetail.getString("name", "").toString()
    }

    fun setUserEmail(userEmail: String) {
        userDetailEditor.putString("email", userEmail)
        userDetailEditor.apply()
    }

    fun getUserEmail(): String {
        return userDetail.getString("email", "").toString()
    }

    /*fun setUserReps(reps: Int) {
        userDetailEditor.putInt("reps", reps)
        userDetailEditor.apply()
    }

    fun getUserReps(reps: Int) {
        userDetail.getInt("reps", 0).toString()
    }*/


    fun setIsUserLoggedIn(status: Boolean) {
        userDetailEditor.putBoolean("isLoggedIn", status)
        userDetailEditor.apply()
    }

    fun getIsUserLoggedIn(): Boolean {
        return userDetail.getBoolean("isLoggedIn", false)
    }

    fun clearData() {
        userDetailEditor.clear()
    }
}