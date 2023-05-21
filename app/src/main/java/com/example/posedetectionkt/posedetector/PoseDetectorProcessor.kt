/*package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.posedetectionkt.ml.Pointnet
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer

class PoseDetectorProcessor(
    private val context: Context,
    options: AccuratePoseDetectorOptions,
    pose: String?
) : VisionProcessorBase(context) {

    private val detector: PoseDetector
    private val pose: String?
    private val paintColor: Paint
    private var reps: Int = 0
    private var stage: String = "none"
    private var lastStage: String = "none"
    private var prevStage: String = "none"
    private var repCounted = false
    private lateinit var predictedClass: String
    private val toneGen: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private lateinit var model: Pointnet
    private lateinit var inputFeature0: TensorBuffer
    private val classMap = mapOf(
        0 to "pushup_down",
        1 to "pushup_up",
        2 to "squat_down",
        3 to "squat_up"
    )

    init {
        detector = PoseDetection.getClient(options)
        this.pose = pose
        paintColor = Paint()
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
        // set the stage and last stage based on the pose selected
        when (pose) {
            "pushup" -> {
                stage = "pushup_up"
                lastStage = "pushup_down"
            }

            "squat" -> {
                stage = "squat_up"
                lastStage = "squat_down"
            }
        }
        try {
            model = Pointnet.newInstance(context)
            // Creates inputs for reference.
            inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 33, 3), DataType.FLOAT32)
            // Releases model resources if no longer used.
        } catch (exception: IOException) {
            Log.d("PoseDetectorProcessor", "Error: ${exception.message}")
        }

    }

    override fun stop() {
        super.stop()
        detector.close()
        model.close()
    }

    override fun detectInImage(image: InputImage): Task<Pose> {
        return detector
            .process(image).continueWith {
                it.result
            }
    }


    override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) {
        if (results.allPoseLandmarks.size > 0) {
            val bufferCapacity =
                results.allPoseLandmarks.size * 3 * Float.SIZE_BYTES // Assuming 2 floats per landmark (x, y)
            val byteBuffer = ByteBuffer.allocate(bufferCapacity)
            val poseLandmarks = results.allPoseLandmarks
            for (landmark in poseLandmarks) {
                // Extract relevant information from the pose landmarks
                val landmarkX = landmark.position3D.x
                val landmarkY = landmark.position3D.y
                val landmarkZ = landmark.position3D.z

                // Assuming `byteBuffer` is a `ByteBuffer` with the appropriate capacity
                // and that `float` values are being stored in the buffer
                byteBuffer.putFloat(landmarkX)
                byteBuffer.putFloat(landmarkY)
                byteBuffer.putFloat(landmarkZ)
            }
            inputFeature0.loadBuffer(byteBuffer)
            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val predictions = FloatArray(outputFeature0.shape[1])
            outputFeature0.floatArray.copyInto(predictions)
            // Find the index of the maximum predicted probability
            var maxProbabilityIndex = 0
            for (i in 1 until predictions.size) {
                if (predictions[i] > predictions[maxProbabilityIndex]) {
                    maxProbabilityIndex = i
                }
            }
            // Retrieve the predicted class label based on the max probability index
            predictedClass = classMap[maxProbabilityIndex].toString()
            Log.d("PoseDetectorProcessor", "Predicted class: $predictedClass")
            val poseUp = pose + "_up"
            val poseDown = pose + "_down"
            if (predictedClass == classMap.entries.find { it.value == poseUp }?.value ||
                predictedClass == classMap.entries.find { it.value == poseDown }?.value
            ) {
                // Check if stage has changed
                if (predictedClass != stage && predictedClass != lastStage) {
                    val prevStage = stage
                    stage = predictedClass
                    if (prevStage == poseUp && stage == poseDown && !repCounted) {
                        reps++
                        repCounted = true
                        println("$pose count: $reps")
                    } else if (prevStage == poseDown && stage == poseUp) {
                        repCounted = false
                    }
                    toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                }
                lastStage = predictedClass

                paintColor.color = Color.GREEN
                graphicOverlay.add(
                    PoseGraphic(
                        graphicOverlay,
                        results,
                        paintColor,
                        reps.toString(),
                        predictedClass
                    )
                )
            } else {
                paintColor.color = Color.RED
                graphicOverlay.add(
                    PoseGraphic(
                        graphicOverlay,
                        results,
                        paintColor,
                        reps.toString(),
                        predictedClass
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
package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.posedetectionkt.ml.Pointnet
import com.example.posedetectionkt.ui.activities.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

class PoseDetectorProcessor(
    private val context: Context,
    options: AccuratePoseDetectorOptions,
    pose: String?
) : VisionProcessorBase(context) {

    private val detector: PoseDetector
    private val pose: String?
    private val paintColor: Paint
    private var reps: Int = 0
    private var stage: String = "none"
    private var lastStage: String = "none"
    private var prevStage: String = "none"
    private var repCounted = false
    private lateinit var predictedClass: String
    private val toneGen: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private lateinit var model: Pointnet
    private lateinit var inputFeature: TensorBuffer
    private val classMap = mapOf(
        0 to "pushup_down",
        1 to "pushup_up",
        2 to "squat_down",
        3 to "squat_up"
    )
    private lateinit var poseAlertDialog: AlertDialog

    init {
        detector = PoseDetection.getClient(options)
        this.pose = pose
        paintColor = Paint()
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
        poseAlertDialog = AlertDialog(context, "No  pose detected")

        // Set the stage and last stage based on the pose selected
        when (pose) {
            "pushup" -> {
                stage = "pushup_up"
                lastStage = "pushup_down"
            }

            "squat" -> {
                stage = "squat_up"
                lastStage = "squat_down"
            }
        }

        try {
            model = Pointnet.newInstance(context)
            // Releases model resources if no longer used.
        } catch (exception: IOException) {
            Log.d("PoseDetectorProcessor", "Error: ${exception.message}")
        }
    }

    override fun stop() {
        super.stop()
        detector.close()
        model.close()
    }

    override fun detectInImage(image: InputImage): Task<Pose> {
        return detector.process(image).continueWith {
            it.result
        }
    }

    override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) {
        poseAlertDialog.dismiss()
        if (results.allPoseLandmarks.isNotEmpty()) {
            // Creates inputs for reference.

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val predictions = FloatArray(outputFeature0.shape[1])
            outputFeature0.floatArray.copyInto(predictions)

            // Find the index of the maximum predicted probability
            var maxProbabilityIndex = 0
            for (i in 1 until predictions.size) {
                if (predictions[i] > predictions[maxProbabilityIndex]) {
                    maxProbabilityIndex = i
                }
            }

            // Retrieve the predicted class label based on the max probability index
            predictedClass = classMap[maxProbabilityIndex].toString()
            Log.d("PoseDetectorProcessor", "Predicted class: $predictedClass")

            // Update the stage variable based on the detected pose
            val poseUp = pose + "_up"
            val poseDown = pose + "_down"
            stage = when (predictedClass) {
                poseUp, poseDown -> predictedClass
                else -> stage
            }

            // Check if stage has changed
            if (stage != lastStage) {
                if (stage == poseDown && lastStage == poseUp && !repCounted) {
                    reps++
                    repCounted = true
                    println("$pose count: $reps")
                } else if (stage == poseUp && lastStage == poseDown) {
                    repCounted = false
                }
                toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            }

            lastStage = stage

            // Create and add PoseGraphic to the graphic overlay
            paintColor.color = if (predictedClass == stage) Color.GREEN else Color.RED
        } else {
            poseAlertDialog.show()
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
        private val STROKE_WIDTH = 10.0f
    }
}

