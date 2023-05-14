package com.example.posedetectionkt.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.example.posedetectionkt.R

class Loading(context: Context) : Dialog(context) {
    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER
        window!!.attributes = params
        setTitle(null)
        setCancelable(false)
        setOnCancelListener(null)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val view: View = LayoutInflater.from(context).inflate(R.layout.loading, null)
        setContentView(view)
    }
}
