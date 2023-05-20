package com.example.posedetectionkt.ui.activities

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.posedetectionkt.R
import java.time.Duration

class AlertDialog(context: Context, message: String) : Dialog(context) {
    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER_HORIZONTAL
        params.y = 60
        window!!.attributes = params
        setTitle(null)
//        window!!.attributes.windowAnimations = R.style.dialog_animation
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(false)
        setOnCancelListener(null)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val view: View = LayoutInflater.from(context).inflate(R.layout.alert_dialog_layout, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_alert_message)

        tvMessage.text = message
        setContentView(view)
    }

    fun showSimpleAlertMessage() {
        show()
        Handler().postDelayed({ dismiss() }, Duration.ofSeconds(5).toMillis())
    }
}
