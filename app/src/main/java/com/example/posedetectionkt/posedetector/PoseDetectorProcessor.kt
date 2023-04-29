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
import kotlin.math.PI
import kotlin.math.atan2

/** A processor to run pose detector. */
class PoseDetectorProcessor(
    private val context: Context,
    options: PoseDetectorOptionsBase,
    pose : String?
) : VisionProcessorBase(context) {

    private val detector: PoseDetector
    private val pose:  String?
    private val paintColor: Paint
    private var reps:  Int = 0


    /*private val classificationExecutor: Executor*/

    /*private var poseClassifierProcessor: PoseClassifierProcessor? = null*/

    /** Internal class to hold Pose and classification results. */
//    class PoseWithClassification(val pose: Pose, val classificationResult: List<String>)

    init {
        detector = PoseDetection.getClient(options)
        /*classificationExecutor = Executors.newSingleThreadExecutor()*/
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

    private fun calculateAngle(a: Array<Float>, b: Array<Float>, c: Array<Float>): Float {
        val pointA = arrayOf(a[0], a[1])
        val pointB = arrayOf(b[0], b[1])
        val pointC = arrayOf(c[0], c[1])

        val radians = atan2((pointC[1] - pointB[1]).toDouble(), (pointC[0] - pointB[0]).toDouble()) - atan2(
            (pointA[1] - pointB[1]).toDouble(), (pointA[0] - pointB[0]).toDouble()
        )
        var angle = kotlin.math.abs(radians * 180.0f / PI.toFloat()).toFloat()

        if (angle > 180.0f) {
            angle = 360.0f - angle
        }

        return angle
    }


    override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) {
        // TODO: classify poses using angle heuristics for pushups, squats and plank
        val shoulder = arrayOf(
            results.allPoseLandmarks[PoseLandmark.LEFT_SHOULDER].position.x,
            results.allPoseLandmarks[PoseLandmark.LEFT_SHOULDER].position.y
        )
        val hip = arrayOf(
            results.allPoseLandmarks[PoseLandmark.LEFT_HIP].position.x,
            results.allPoseLandmarks[PoseLandmark.LEFT_HIP].position.y
        )
        val knee = arrayOf(
            results.allPoseLandmarks[PoseLandmark.LEFT_KNEE].position.x,
            results.allPoseLandmarks[PoseLandmark.LEFT_KNEE].position.y
        )
        val ankle = arrayOf(
            results.allPoseLandmarks[PoseLandmark.LEFT_ANKLE].position.x,
            results.allPoseLandmarks[PoseLandmark.LEFT_ANKLE].position.y
        )
        val angle = calculateAngle(shoulder, hip, knee)
        val angle2 = calculateAngle(hip, knee, ankle)
        if (angle == 180.0f && angle2 == 180.0f) {
            // check if the nose and the elbow are in the same line
            val elbow = arrayOf(
                results.allPoseLandmarks[PoseLandmark.LEFT_ELBOW].position.x,
                results.allPoseLandmarks[PoseLandmark.LEFT_ELBOW].position.y
            )
            val wrist = arrayOf(
                results.allPoseLandmarks[PoseLandmark.LEFT_WRIST].position.x,
                results.allPoseLandmarks[PoseLandmark.LEFT_WRIST].position.y
            )
            val angle3 = calculateAngle(wrist, elbow, shoulder)
            if (angle3 == 180.0f) {
                Log.d("POSE", "Pushup")
            } else {
                reps += 1
                Log.d("POSE", "Squat")
            }


            paintColor.color = Color.WHITE
            graphicOverlay.add(
                PoseGraphic(
                    graphicOverlay,
                    results,
                    paintColor
                )
            )
        } else {
            paintColor.color = Color.RED
            graphicOverlay.add(
                PoseGraphic(
                    graphicOverlay,
                    results,
                    paintColor
                )
            )
        }
    }

    /*override fun detectInImage(image: MlImage): Task<PoseWithClassification> {
        return detector
            .process(image)
            .continueWith(
                classificationExecutor
            ) { task ->
                val pose = task.getResult()
                var classificationResult: List<String> = ArrayList()
                if (runClassification) {
                    if (poseClassifierProcessor == null) {
                        poseClassifierProcessor = PoseClassifierProcessor(context, isStreamMode)
                    }
                    classificationResult = poseClassifierProcessor!!.getPoseResult(pose)
                }
                PoseWithClassification(pose, classificationResult)
            }
    }*/


    override fun onFailure(e: Exception) {
        Log.e(TAG, "Pose detection failed!", e)
    }

    override fun isMlImageEnabled(context: Context?): Boolean {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return false
    }


    companion object {
        private val TAG = "PoseDetectorProcessor"
        private val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f
        private val STROKE_WIDTH = 10.0f
    }
}
