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
import java.nio.FloatBuffer

class PoseDetectorProcessor(
    private val context: Context,
    options: AccuratePoseDetectorOptions,
    pose: String?
) : VisionProcessorBase(context) {

    private val detector: PoseDetector
    private val paintColor: Paint
    private val pose: String?
    private var reps: Int = 0
    private var stage: String = pose + "_up"
    private var lastStage: String = pose + "_down"
    private var prevStage: String = "none"
    private var repCounted = false
    private lateinit var predictedClass: String
    private lateinit var model: Pointnet
    private val classMap = mapOf(
        0 to "pushup_down",
        1 to "pushup_up",
        2 to "squat_down",
        3 to "squat_up"
    )
    private lateinit var inputFeature0: TensorBuffer
    private var poseAlertDialog: AlertDialog

    init {
        detector = PoseDetection.getClient(options)
        paintColor = Paint()
        paintColor.strokeWidth = STROKE_WIDTH
        paintColor.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
        this.pose = pose
        poseAlertDialog = AlertDialog(context, "No pose detected")
        try {
            model = Pointnet.newInstance(context)
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
            .process(image)
    }

    override fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay) {
        poseAlertDialog.dismiss()
        if (results.allPoseLandmarks.size > 0) {
            val poseLandmarks = results.allPoseLandmarks
            val landmarkCoordinates = FloatArray(poseLandmarks.size * 3)
            for ((index, landmark) in poseLandmarks.withIndex()) {
                // Assuming the model expects normalized landmark coordinates.
                landmarkCoordinates[index * 3] = landmark.position3D.x / graphicOverlay.imageWidth
                landmarkCoordinates[index * 3 + 1] =
                    landmark.position3D.y / graphicOverlay.imageHeight
                landmarkCoordinates[index * 3 + 2] =
                    landmark.position3D.z / graphicOverlay.imageWidth
            }
            Log.d("PoseArry", "Pose array size: ${landmarkCoordinates.contentToString()}")

            inputFeature0.loadArray(landmarkCoordinates)
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val predictions = outputFeature0.floatArray
            val maxProbabilityIndex = predictions.indices.maxByOrNull { predictions[it] }

            val currentStage = classMap[maxProbabilityIndex ?: 0].toString()
            if (currentStage != stage && currentStage != lastStage) {
                prevStage = stage
                stage = currentStage
                if (prevStage == pose + "_up" && stage == pose + "_down" && !repCounted) {
                    reps++
                    repCounted = true
                } else if (prevStage == pose + "_down" && stage == pose + "_up") {
                    repCounted = false
                }
            }
            lastStage = currentStage
            paintColor.color = Color.GREEN
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
                lastStage
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


/*for (landmark in results.allPoseLandmarks) {
                val landmarkList = floatArrayOf(
                    landmark.position3D.x / graphicOverlay.imageWidth,
                    landmark.position3D.y / graphicOverlay.imageHeight,
                    (landmark.position3D.z / graphicOverlay.imageWidth) * 2 - 1
                )
                inputFeature0.loadArray(landmarkList)
                Log.d("Array", "Array: ${landmarkList.contentToString()}")
            }
*/