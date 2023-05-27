package com.example.posedetectionkt.posedetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.posedetectionkt.posedetector.GraphicOverlay.Graphic
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/** Draw the detected pose in preview. */
class PoseGraphic
internal constructor(
    overlay: GraphicOverlay,
    private val pose: Pose,
    private val paintColor: Paint,
    private val reps: String,
    private val stage: String,
) : Graphic(overlay) {

    private val infoText: Paint
    private val POSE_CLASSIFICATION_TEXT_SIZE = 60.0f


    init {
        infoText = Paint()
        infoText.color = Color.WHITE
        infoText.textSize = POSE_CLASSIFICATION_TEXT_SIZE
        infoText.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
    }

    override fun draw(canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        // show the reps counter on the screen
        canvas.drawText("Reps: $reps", 20.0F, 50.0F, infoText)
        canvas.drawText("Stage: $stage", 20.0F, 120.0F, infoText)


        // Draw all the points
        for (landmark in landmarks) {
            drawPoint(canvas, landmark, paintColor)
        }

        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
        val leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
        val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
        val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
        val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
        val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
        val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
        val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
        val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
        val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
        val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
        val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
        val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
        val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
        val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
        val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

        // Face
        drawLine(canvas, nose, leftEyeInner, paintColor)
        drawLine(canvas, leftEyeInner, leftEye, paintColor)
        drawLine(canvas, leftEye, leftEyeOuter, paintColor)
        drawLine(canvas, leftEyeOuter, leftEar, paintColor)
        drawLine(canvas, nose, rightEyeInner, paintColor)
        drawLine(canvas, rightEyeInner, rightEye, paintColor)
        drawLine(canvas, rightEye, rightEyeOuter, paintColor)
        drawLine(canvas, rightEyeOuter, rightEar, paintColor)
        drawLine(canvas, leftMouth, rightMouth, paintColor)

        drawLine(canvas, leftShoulder, rightShoulder, paintColor)
        drawLine(canvas, leftHip, rightHip, paintColor)

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, paintColor)
        drawLine(canvas, leftElbow, leftWrist, paintColor)
        drawLine(canvas, leftShoulder, leftHip, paintColor)
        drawLine(canvas, leftHip, leftKnee, paintColor)
        drawLine(canvas, leftKnee, leftAnkle, paintColor)
        drawLine(canvas, leftWrist, leftThumb, paintColor)
        drawLine(canvas, leftWrist, leftPinky, paintColor)
        drawLine(canvas, leftWrist, leftIndex, paintColor)
        drawLine(canvas, leftIndex, leftPinky, paintColor)
        drawLine(canvas, leftAnkle, leftHeel, paintColor)
        drawLine(canvas, leftHeel, leftFootIndex, paintColor)
        drawLine(canvas, leftFootIndex, leftAnkle, paintColor)

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, paintColor)
        drawLine(canvas, rightElbow, rightWrist, paintColor)
        drawLine(canvas, rightShoulder, rightHip, paintColor)
        drawLine(canvas, rightHip, rightKnee, paintColor)
        drawLine(canvas, rightKnee, rightAnkle, paintColor)
        drawLine(canvas, rightWrist, rightThumb, paintColor)
        drawLine(canvas, rightWrist, rightPinky, paintColor)
        drawLine(canvas, rightWrist, rightIndex, paintColor)
        drawLine(canvas, rightIndex, rightPinky, paintColor)
        drawLine(canvas, rightAnkle, rightHeel, paintColor)
        drawLine(canvas, rightHeel, rightFootIndex, paintColor)
        drawLine(canvas, rightFootIndex, rightAnkle, paintColor)
    }

    private fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
        val point = landmark.position3D
        canvas.drawCircle(translateX(point.x), translateY(point.y), DOT_RADIUS, paint)
    }

    private fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Paint
    ) {
        val start = startLandmark!!.position3D
        val end = endLandmark!!.position3D

        canvas.drawLine(
            translateX(start.x),
            translateY(start.y),
            translateX(end.x),
            translateY(end.y),
            paint
        )
    }

    companion object {
        private val DOT_RADIUS = 10.0f
    }
}
