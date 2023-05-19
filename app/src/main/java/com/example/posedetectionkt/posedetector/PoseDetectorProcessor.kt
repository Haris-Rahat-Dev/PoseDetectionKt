package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
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

/*class PoseDetectorProcessor(
    private val context: Context,
    options: AccuratePoseDetectorOptions,
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
                                "push$stage"
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
                                "push$stage"
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
                    )
                val min_shoulder_distance =
                    (shoulder_distance - shoulder_distance * 0.5)
                val max_shoulder_distance =
                    (shoulder_distance + shoulder_distance * 0.5)

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
                                "push$stage"
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
                                "push$stage"
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
}*/
class PoseDetectorProcessor(
    private val context: Context,
    options: AccuratePoseDetectorOptions,
    private val pose: String?
) : VisionProcessorBase(context) {

    private val detector: PoseDetector = PoseDetection.getClient(options)
    private val paintColor: Paint = Paint()
    private var reps: Int = 0
    private var stage: String = "none"
    private val toneGen: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

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

                Log.d("PushUpAngle", angle.toString())

                if (angle in 160.0..190.0) {
                    val elbow = results.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                    val wrist = results.getPoseLandmark(PoseLandmark.LEFT_WRIST)

                    val angle2 = getAngle(shoulder, elbow!!, wrist!!)

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
                                "push$stage"
                            )
                        )
                    }

                    if (angle2 in 30.0..90.0 && stage == "up") {
                        Log.d("POSE", "Push_down")
                        stage = "down"
                        reps += 1
                        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        paintColor.color = Color.GREEN
                        graphicOverlay.add(
                            PoseGraphic(
                                graphicOverlay,
                                results,
                                paintColor,
                                reps.toString(),
                                "push$stage"
                            )
                        )
                        stage = "none" // Reset the stage after detecting the down state
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
            } else if (pose == "squat") {
                val shoulder = results.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val hip = results.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val ankle = results.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                val angle = getAngle(shoulder!!, hip!!, ankle!!)

                val shoulderDistance =
                    getDistance(shoulder!!, results.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)!!)
                val ankleDistance =
                    getDistance(ankle!!, results.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)!!)
                val minShoulderDistance =
                    shoulderDistance - shoulderDistance * 0.5
                val maxShoulderDistance =
                    shoulderDistance + shoulderDistance * 0.5

                if (ankleDistance in minShoulderDistance..maxShoulderDistance) {
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
                                "push$stage"
                            )
                        )
                    }

                    if (angle in 30.0..90.0 && stage == "up") {
                        Log.d("POSE", "Squatdown")
                        reps += 1
                        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        paintColor.color = Color.GREEN
                        graphicOverlay.add(
                            PoseGraphic(
                                graphicOverlay,
                                results,
                                paintColor,
                                reps.toString(),
                                "push$stage"
                            )
                        )
                        stage = "none" // Reset the stage after detecting the down state
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

