package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class PoseDetectorProcessor(
    private val context: Context,
    options: PoseDetectorOptionsBase,
    pose: String?
) : VisionProcessorBase(context) {

    private val detector: PoseDetector
    private val pose: String?
    private val paintColor: Paint
    private var reps: Int = 0
    private var stage: String = "none"


    init {
        detector = PoseDetection.getClient(options)
        this.pose = pose
        paintColor = Paint()
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
    }

    override fun stop() {
        super.stop()
        detector.close()
    }

    override fun detectInImage(image: InputImage): Task<Pose> {
        return detector
            .process(image).continueWith {
                it.result
            }
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

    // write a function for calculating distance between two points
    private fun getDistance(
        firstPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Double {
        val x = firstPoint.position3D.x - lastPoint.position3D.x
        val y = firstPoint.position3D.y - lastPoint.position3D.y
        return sqrt((x * x + y * y).toDouble())
    }


    override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) {
        paintColor.color = Color.GREEN
        graphicOverlay.add(
            PoseGraphic(
                graphicOverlay,
                results,
                paintColor,
                "52"
            )
        )
        if (results.allPoseLandmarks.size > 0) {
            // TODO: classify poses using angle heuristics for pushups, squats and plank
            if (this.pose == "pushup") {

                val shoulder = results.allPoseLandmarks[PoseLandmark.LEFT_SHOULDER]
                val hip = results.allPoseLandmarks[PoseLandmark.LEFT_HIP]
                val ankle = results.allPoseLandmarks[PoseLandmark.LEFT_ANKLE]

                val angle = getAngle(shoulder, hip, ankle)

                Log.d("PushUpAngle", angle.toString())

                if (angle in 160.0..190.0) {
                    val elbow = results.allPoseLandmarks[PoseLandmark.LEFT_ELBOW]
                    val wrist = results.allPoseLandmarks[PoseLandmark.LEFT_WRIST]

                    val angle2 = getAngle(shoulder, elbow, wrist)

                    if (angle2 in 160.0..190.0) {
                        Log.d("POSE", "Push_up")
                        stage = "up"
                        paintColor.color = Color.GREEN
                        graphicOverlay.add(
                            PoseGraphic(
                                graphicOverlay,
                                results,
                                paintColor,
                                "Push Up"
                            )
                        )
                    }

                    if (angle2 in 30.0..90.0 && stage == "up") {
                        Log.d("POSE", "Push_down")
                        stage = "down"
                        reps += 1
                        paintColor.color = Color.GREEN
                        graphicOverlay.add(
                            PoseGraphic(
                                graphicOverlay,
                                results,
                                paintColor,
                                "Push Down"
                            )
                        )
                    }

                } else {
                    paintColor.color = Color.RED
                    graphicOverlay.add(
                        PoseGraphic(
                            graphicOverlay,
                            results,
                            paintColor,
                            "Not a push up"
                        )
                    )
                }
            } else if (this.pose == "squats") {

                val shoulder = results.allPoseLandmarks[PoseLandmark.LEFT_SHOULDER]

                val hip = results.allPoseLandmarks[PoseLandmark.LEFT_HIP]

                val knee = results.allPoseLandmarks[PoseLandmark.LEFT_KNEE]

                val angle = getAngle(shoulder, hip, knee)


                val shoulder_distance =
                    getDistance(shoulder, results.allPoseLandmarks[PoseLandmark.RIGHT_SHOULDER])
                val knee_distance =
                    getDistance(knee, results.allPoseLandmarks[PoseLandmark.RIGHT_KNEE])
                val min_shoulder_distance = shoulder_distance - shoulder_distance * 0.4
                val max_shoulder_distance = shoulder_distance + shoulder_distance * 0.4


                if (knee_distance in min_shoulder_distance..max_shoulder_distance) {
                    if (angle in 160.0..190.0) {
                        Log.d("POSE", "Squatup")
                        stage = "up"
                        paintColor.color = Color.WHITE
                        graphicOverlay.add(
                            PoseGraphic(
                                graphicOverlay,
                                results,
                                paintColor,
                                "Squat Up"
                            )
                        )
                    }
                    if (angle in 30.0..90.0 && stage == "up") {
                        Log.d("POSE", "Squatdown")
                        reps += 1
                        paintColor.color = Color.WHITE
                        graphicOverlay.add(
                            PoseGraphic(
                                graphicOverlay,
                                results,
                                paintColor,
                                "Squat Down"
                            )
                        )
                    }
                } else {
                    paintColor.color = Color.RED
                    graphicOverlay.add(
                        PoseGraphic(
                            graphicOverlay,
                            results,
                            paintColor,
                            "Not a squat"
                        )
                    )
                }
            } else {
                val shoulder = results.allPoseLandmarks[PoseLandmark.LEFT_SHOULDER]

                val elbow = results.allPoseLandmarks[PoseLandmark.LEFT_ELBOW]

                val wrist = results.allPoseLandmarks[PoseLandmark.LEFT_WRIST]

                val angle = getAngle(shoulder, elbow, wrist)

                Log.d("Left arm angle", angle.toString())

                paintColor.color = Color.GREEN
                graphicOverlay.add(
                    PoseGraphic(
                        graphicOverlay,
                        results,
                        paintColor,
                        angle.toString()
                    )
                )
            }
        } else {
            paintColor.color = Color.RED
            graphicOverlay.add(
                PoseGraphic(
                    graphicOverlay,
                    results,
                    paintColor,
                    "Not a pose"
                )
            )
        }
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
        private val STROKE_WIDTH = 10.0f
    }
}
