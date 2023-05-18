package com.example.posedetectionkt.posedetector

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.GuardedBy
import com.example.posedetectionkt.utils.preference.PreferenceUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(VisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase(context: Context) :
    VisionImageProcessor {

    companion object {
        private const val TAG = "VisionProcessorBase"
    }

    private val fpsTimer = Timer()
    private val executor =
        ScopedExecutor(TaskExecutors.MAIN_THREAD)

    // Whether this processor is already shut down
    private var isShutdown = false

    // Used to calculate latency, running in the same thread, no sync needed.
    private var numRuns = 0
    private var totalFrameMs = 0L
    private var maxFrameMs = 0L
    private var minFrameMs = Long.MAX_VALUE
    private var totalDetectorMs = 0L
    private var maxDetectorMs = 0L
    private var minDetectorMs = Long.MAX_VALUE

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    init {
        fpsTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    // -----------------Code for processing live preview frame from Camera1 API-----------------------
    @Synchronized
    override fun processByteBuffer(
        data: ByteBuffer?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay)
        }
    }

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay) {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null && !isShutdown) {
            processImage(processingImage!!, processingMetaData!!, graphicOverlay)
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        val frameStartMs = SystemClock.elapsedRealtime()
        // If live viewport is on (that is the underneath surface view takes care of the camera preview
        // drawing), skip the unnecessary bitmap creation that used for the manual preview drawing.
        val bitmap =
            if (PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.context)) null
            else BitmapUtils.getBitmap(data, frameMetadata)


        requestDetectInImage(
            InputImage.fromByteBuffer(
                data,
                frameMetadata.width,
                frameMetadata.height,
                frameMetadata.rotation,
                InputImage.IMAGE_FORMAT_NV21
            ),
            graphicOverlay,
            bitmap,
            shouldShowFps = true,
            frameStartMs
        )
            .addOnSuccessListener(executor) { processLatestImage(graphicOverlay) }
    }

    // -----------------Common processing logic-------------------------------------------------------
    private fun requestDetectInImage(
        image: InputImage,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<Pose> {
        return setUpListener(
            detectInImage(image),
            graphicOverlay,
            originalCameraImage,
            shouldShowFps,
            frameStartMs
        )
    }


    private fun setUpListener(
        task: Task<Pose>,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<Pose> {
        val detectorStartMs = SystemClock.elapsedRealtime()
        return task
            .addOnSuccessListener(
                executor
            ) { results: Pose ->
                val endMs = SystemClock.elapsedRealtime()
                val currentFrameLatencyMs = endMs - frameStartMs
                val currentDetectorLatencyMs = endMs - detectorStartMs
                if (numRuns >= 500) {
                    resetLatencyStats()
                }
                numRuns++
                frameProcessedInOneSecondInterval++
                totalFrameMs += currentFrameLatencyMs
                maxFrameMs = currentFrameLatencyMs.coerceAtLeast(maxFrameMs)
                minFrameMs = currentFrameLatencyMs.coerceAtMost(minFrameMs)
                totalDetectorMs += currentDetectorLatencyMs
                maxDetectorMs = currentDetectorLatencyMs.coerceAtLeast(maxDetectorMs)
                minDetectorMs = currentDetectorLatencyMs.coerceAtMost(minDetectorMs)


                graphicOverlay.clear()
                if (originalCameraImage != null) {
                    graphicOverlay.add(
                        CameraImageGraphic(
                            graphicOverlay,
                            originalCameraImage
                        )
                    )
                }
                this@VisionProcessorBase.onSuccess(results, graphicOverlay)
                graphicOverlay.add(
                    InferenceInfoGraphic(
                        graphicOverlay,
                        currentFrameLatencyMs,
                        currentDetectorLatencyMs,
                        if (shouldShowFps) framesPerSecond else null
                    )
                )
                graphicOverlay.postInvalidate()
            }
            .addOnFailureListener(
                executor
            ) { e: Exception ->
                graphicOverlay.clear()
                graphicOverlay.postInvalidate()
                val error = "Failed to process. Error: " + e.localizedMessage
                Toast.makeText(
                    graphicOverlay.context,
                    """
          $error
          Cause: ${e.cause}
          """.trimIndent(),
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d(TAG, error)
                e.printStackTrace()
                this@VisionProcessorBase.onFailure(e)
            }
    }

    override fun stop() {
        executor.shutdown()
        isShutdown = true
        resetLatencyStats()
        fpsTimer.cancel()
    }

    private fun resetLatencyStats() {
        numRuns = 0
        totalFrameMs = 0
        maxFrameMs = 0
        minFrameMs = Long.MAX_VALUE
        totalDetectorMs = 0
        maxDetectorMs = 0
        minDetectorMs = Long.MAX_VALUE
    }

    protected abstract fun detectInImage(image: InputImage): Task<Pose>

    protected abstract fun onSuccess(results: Pose, graphicOverlay: GraphicOverlay)

    protected abstract fun onFailure(e: Exception)

    protected open fun isMlImageEnabled(context: Context?): Boolean {
        return false
    }
}
