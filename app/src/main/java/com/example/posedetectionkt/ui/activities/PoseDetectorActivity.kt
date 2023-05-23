package com.example.posedetectionkt.ui.activities

import PoseDetectorProcessor
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetectionkt.R
import com.example.posedetectionkt.posedetector.CameraSource
import com.example.posedetectionkt.posedetector.CameraSourcePreview
import com.example.posedetectionkt.posedetector.GraphicOverlay
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.io.IOException


class PoseDetectorActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var pose: String? = null
    private lateinit var hintText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose_detector)

        val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)

        // get data from intent
        val data = intent.extras

        hintText = findViewById(R.id.hint)
        hintText.setOnClickListener {
            // create an intent for opening a youtube link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data?.getString("hintText")))
            startActivity(intent)
        }

        Log.d(TAG, "onCreate")

        preview = findViewById(R.id.preview_view)
        if (preview == null) {
            Log.d(TAG, "Preview is null")
        }

        graphicOverlay = findViewById(R.id.graphic_overlay)
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        if (data == null) {
            Toast.makeText(
                applicationContext,
                "No data available",
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            pose = data.getString("pose")

            createCameraSource(pose)
        }
    }

    private fun createCameraSource(pose: String?) {
        if (cameraSource == null) {
            cameraSource = CameraSource(
                this,
                graphicOverlay
            )
        }

        try {

            val poseDetectorOptions = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU)
                .build()

            cameraSource!!.setMachineLearningFrameProcessor(
                PoseDetectorProcessor(
                    this,
                    poseDetectorOptions,
                    pose
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor", e)
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
            } else {
                cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
            }
        }
        preview?.stop()
        startCameraSource()
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createCameraSource(pose)
        startCameraSource()
    }

    /** Stops the camera. */
    override fun onPause() {
        super.onPause()
        preview?.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource?.release()
        }
    }

    companion object {
        /*private const val POSE_DETECTION = "Pose Detection"*/
        private const val TAG = "PoseDetectorActivity"
    }

}