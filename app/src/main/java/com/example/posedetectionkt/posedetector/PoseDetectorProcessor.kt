package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.posedetectionkt.utils.preference.UserDetails
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.roundToLong
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
    val toneGen: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100);


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
                                reps.toString(),
                                stage
                            )
                        )
                    }

                    if (angle2 in 30.0..90.0 && stage == "up") {
                        Log.d("POSE", "Push_down")
                        stage = "down"
                        reps += 1
                        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                        paintColor.color = Color.GREEN
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

                } else {
                    paintColor.color = Color.RED
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
            } else if (this.pose == "squat") {

                val shoulder = results.allPoseLandmarks[PoseLandmark.LEFT_SHOULDER]

                val hip = results.allPoseLandmarks[PoseLandmark.LEFT_HIP]

                val ankle = results.allPoseLandmarks[PoseLandmark.LEFT_ANKLE]

                val angle = getAngle(shoulder, hip, ankle)

                val shoulder_distance =
                    getDistance(shoulder, results.allPoseLandmarks[PoseLandmark.RIGHT_SHOULDER])
                val ankle_distance =
                    getDistance(
                        ankle,
                        results.allPoseLandmarks[PoseLandmark.RIGHT_ANKLE]
                    ).roundToInt()
                val min_shoulder_distance =
                    (shoulder_distance - shoulder_distance * 0.4).roundToInt()
                val max_shoulder_distance =
                    (shoulder_distance + shoulder_distance * 0.4).roundToInt()


                if (ankle_distance in min_shoulder_distance..max_shoulder_distance) {
                    if (angle in 160.0..190.0) {
                        Log.d("POSE", "Squatup")
                        stage = "up"
                        paintColor.color = Color.GREEN
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

                    if (angle in 30.0..90.0 && stage == "up") {
                        Log.d("POSE", "Squatdown")
                        reps += 1
                        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                        paintColor.color = Color.GREEN
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
                } else {
                    paintColor.color = Color.RED
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
            }
        } else {
            paintColor.color = Color.RED
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
