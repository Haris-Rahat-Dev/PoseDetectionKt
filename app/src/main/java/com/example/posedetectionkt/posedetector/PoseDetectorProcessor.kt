/*package com.example.posedetectionkt.posedetector

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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
    private lateinit var poseAlertDialog: AlertDialog
    val NUM_LANDMARKS = 33
    val NUM_COORDINATES = 3
    val bufferCapacity = NUM_LANDMARKS * NUM_COORDINATES * Float.SIZE_BYTES

    init {
        detector = PoseDetection.getClient(options)
        this.pose = pose
        paintColor = Paint()
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
        poseAlertDialog = AlertDialog(context, "No  pose detected")
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
        poseAlertDialog.dismiss()
        if (results.allPoseLandmarks.size > 0) {
            val byteBuffer = ByteBuffer.allocate(bufferCapacity).order(ByteOrder.nativeOrder())
            val floatBuffer = byteBuffer.asFloatBuffer()

            val poseLandmarks = results.allPoseLandmarks
            val numLandmarks = minOf(poseLandmarks.size, NUM_LANDMARKS)

            for (i in 0 until numLandmarks) {
                val landmark = poseLandmarks[i]
                val landmarkX = landmark.position3D.x
                val landmarkY = landmark.position3D.y
                val landmarkZ = landmark.position3D.z

                floatBuffer.put(landmarkX)
                floatBuffer.put(landmarkY)
                floatBuffer.put(landmarkZ)
            }

            floatBuffer.flip() // Prepare the buffer for reading

            inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(1, NUM_LANDMARKS, NUM_COORDINATES),
                DataType.FLOAT32
            )
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
            } else {
                paintColor.color = Color.RED
            }
        } else {
            // If no pose landmarks are detected, display the message
            poseAlertDialog.show()
            paintColor.color = Color.RED
        }
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
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.posedetectionkt.ml.Pointnet
import com.example.posedetectionkt.posedetector.GraphicOverlay
import com.example.posedetectionkt.posedetector.PoseGraphic
import com.example.posedetectionkt.posedetector.VisionProcessorBase
import com.example.posedetectionkt.ui.activities.AlertDialog
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
import java.nio.ByteOrder

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
    private val toneGen: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private lateinit var model: Pointnet
    private lateinit var inputFeature0: TensorBuffer
    private val classMap = mapOf(
        0 to "pushup_down",
        1 to "pushup_up",
        2 to "squat_down",
        3 to "squat_up"
    )
    private lateinit var poseAlertDialog: AlertDialog
    private val NUM_LANDMARKS = 33
    private val NUM_COORDINATES = 3
    private val bufferCapacity = NUM_LANDMARKS * NUM_COORDINATES * Float.SIZE_BYTES

    init {
        detector = PoseDetection.getClient(options)
        this.pose = pose
        paintColor = Paint()
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
        poseAlertDialog = AlertDialog(context, "No pose detected")
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
        poseAlertDialog.dismiss()
        if (results.allPoseLandmarks.isNotEmpty()) {
            val byteBuffer = ByteBuffer.allocate(bufferCapacity).order(ByteOrder.nativeOrder())
            val floatBuffer = byteBuffer.asFloatBuffer()

            val poseLandmarks = results.allPoseLandmarks
            val numLandmarks = minOf(poseLandmarks.size, NUM_LANDMARKS)

            for (i in 0 until numLandmarks) {
                val landmark = poseLandmarks[i]
                val landmarkX = landmark.position3D.x // normalized x-coordinate
                val landmarkY = landmark.position3D.y // normalized y-coordinate
                val landmarkZ = landmark.position3D.z // normalized z-coordinate

                // Perform normalization based on the provided x, y, and z normalization values
                val normalizedX = landmarkX / graphicOverlay.imageWidth
                val normalizedY = landmarkY / graphicOverlay.imageHeight
                val normalizedZ =
                    (landmarkZ - poseLandmarks[11].position3D.z) / (poseLandmarks[23].position3D.z - poseLandmarks[11].position3D.z)

                Log.d("Points", "[$normalizedX, $normalizedY, $normalizedZ]")

                floatBuffer.put(normalizedX)
                floatBuffer.put(normalizedY)
                floatBuffer.put(normalizedZ)
            }

            floatBuffer.flip() // Prepare the buffer for reading

            inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(1, NUM_LANDMARKS, NUM_COORDINATES),
                DataType.FLOAT32
            )
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
            } else {
                paintColor.color = Color.RED
            }
        } else {
            // If no pose landmarks are detected, display the message
            poseAlertDialog.show()
            paintColor.color = Color.RED
        }
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

