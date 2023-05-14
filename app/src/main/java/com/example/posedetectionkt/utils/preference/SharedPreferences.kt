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

    fun setUserEmail(userEmail: String) {
        userDetailEditor.putString("email", userEmail)
        userDetailEditor.apply()
    }

    fun getUserEmail(): String {
        return userDetail.getString("email", "").toString()
    }

    fun setIsUserLoggedIn(status: Boolean) {
        userDetailEditor.putBoolean("isLoggedIn", status)
        userDetailEditor.apply()
    }

    fun getIsUserLoggedIn(): Boolean {
        return userDetail.getBoolean("isLoggedIn", false)

    }
    fun clearData(){
        userDetailEditor.clear()
    }
}