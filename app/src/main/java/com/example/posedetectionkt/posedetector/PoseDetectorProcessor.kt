package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class PoseDetectorProcessor(
    private val context: Context,
    options: AccuratePoseDetectorOptions,
    private val pose: String?,
) : VisionProcessorBase(context) {

    private val detector: PoseDetector = PoseDetection.getClient(options)
    private val paintColor: Paint = Paint()
    private var reps: Int = 0
    private var stage: String = "none"

    init {
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
    }

    override fun stop() {
        super.stop()
        detector.close()
    }

    override fun detectInImage(image: InputImage): Task<Pose> {
        return detector.process(image)
    }

    private fun getAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Double {
        var result = Math.toDegrees(
            (atan2(
                lastPoint.position3D.y - midPoint.position3D.y,
                lastPoint.position3D.x - midPoint.position3D.x
            )
                    - atan2(
                firstPoint.position3D.y - midPoint.position3D.y,
                firstPoint.position3D.x - midPoint.position3D.x
            )).toDouble()
        )
        result = abs(result) // Angle should never be negative
        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }

    private fun getDistance(
        firstPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Double {
        val x = firstPoint.position3D.x - lastPoint.position3D.x
        val y = firstPoint.position3D.y - lastPoint.position3D.y
        return sqrt((x * x + y * y).toDouble())
    }


    override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) {
        if (results.allPoseLandmarks.isNotEmpty()) {
            if (pose == "pushup") {
                val shoulder = results.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val hip = results.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val ankle = results.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                val angle = getAngle(shoulder!!, hip!!, ankle!!)

                if (angle in 160.0..190.0) {
                    val elbow = results.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                    val wrist = results.getPoseLandmark(PoseLandmark.LEFT_WRIST)

                    val angle2 = getAngle(shoulder, elbow!!, wrist!!)

                    if (angle2 in 160.0..190.0) {
                        stage = "up"
                        paintColor.color = Color.GREEN
                    }

                    if (angle2 in 30.0..90.0 && stage == "up") {
                        stage = "down"
                        reps += 1
                        paintColor.color = Color.GREEN
                    }
                } else {
                    paintColor.color = Color.RED
                }
            } else if (pose == "squat") {
                val shoulder = results.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val hip = results.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val knee = results.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                val ankle = results.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                val angle = getAngle(shoulder!!, hip!!, knee!!).toInt()

                val shoulderDistance =
                    getDistance(shoulder, results.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)!!)

                val ankleDistance =
                    getDistance(ankle!!, results.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)!!)

                val minShoulderDistance = shoulderDistance - shoulderDistance * 0.5
                val maxShoulderDistance = shoulderDistance + shoulderDistance * 0.5

                Log.d("Distance", ankleDistance.toString())
                if (ankleDistance in minShoulderDistance..maxShoulderDistance) {
                    if (angle in 160..190) {
                        stage = "up"
                    }

                    if (angle in 40..150 && stage == "up") {
                        stage = "down"
                        reps += 1
                    }
                    paintColor.color = Color.GREEN

                } else {
                    paintColor.color = Color.RED
                }
            }
        } else {
            Toast.makeText(context, "No pose detected", Toast.LENGTH_LONG).show()
            paintColor.color = Color.RED

        }
        graphicOverlay.add(
            PoseGraphic(
                graphicOverlay,
                results,
                paintColor,
                reps.toString(),
                stage
            )
        )
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Pose detection failed!", e)
    }

    override fun isMlImageEnabled(context: Context?): Boolean {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return false
    }

    companion object {
        private val TAG = "PoseDetectorProcessor"
        private val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 60.0f
        private val STROKE_WIDTH = 8.0f
    }
}

