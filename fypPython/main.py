import cv2
import mediapipe as mp
import numpy as np

mp_drawing = mp.solutions.drawing_utils
mp_pose = mp.solutions.pose


def calculate_angle(a, b, c):
    a = np.array(a)  # First
    b = np.array(b)  # Mid
    c = np.array(c)  # End

    radians = np.arctan2(c[1] - b[1], c[0] - b[0]) - np.arctan2(a[1] - b[1], a[0] - b[0])
    angle = np.abs(radians * 180.0 / np.pi)

    if angle > 180.0:
        angle = 360 - angle

    return angle


def calculate_distance(a, b):
    a = np.array(a)
    b = np.array(b)

    return np.linalg.norm(a - b)


cap = cv2.VideoCapture("videos/Squats4.mp4")

# Curl counter variables
counter = 0
stage = None

## Setup mediapipe instance
with mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
            continue
        # Recolor image to RGB
        image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        image.flags.writeable = False

        # get the frame width and height
        frame_width = image.shape[1]
        frame_height = image.shape[0]

        # Make detection
        results = pose.process(image)

        # Recolor back to BGR
        image.flags.writeable = True
        image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

        # Extract landmarks
        try:
            landmarks = results.pose_landmarks.landmark
            workout = 'squat'
            if workout == 'pushup':
                # Get coordinates
                shoulder = [landmarks[mp_pose.PoseLandmark.LEFT_SHOULDER.value].x,
                            landmarks[mp_pose.PoseLandmark.LEFT_SHOULDER.value].y]
                hip = [landmarks[mp_pose.PoseLandmark.LEFT_HIP.value].x,
                       landmarks[mp_pose.PoseLandmark.LEFT_HIP.value].y]
                ankle = [landmarks[mp_pose.PoseLandmark.LEFT_ANKLE.value].x,
                         landmarks[mp_pose.PoseLandmark.LEFT_ANKLE.value].y]

                # Calculate angle
                angle = calculate_angle(shoulder, hip, ankle)

                # Visualize angle
                cv2.putText(image, str(angle),
                            tuple(np.multiply(hip, [frame_width, frame_height]).astype(int)),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2, cv2.LINE_AA
                            )

                # Curl counter logic
                # if angle > 160:
                #     stage = "down"
                # if angle < 30 and stage == 'down':
                #     stage = "up"
                #     counter += 1
                #     print(counter)
                if 190 > angle > 160:
                    elbow = [landmarks[mp_pose.PoseLandmark.LEFT_ELBOW.value].x,
                             landmarks[mp_pose.PoseLandmark.LEFT_ELBOW.value].y]
                    wrist = [landmarks[mp_pose.PoseLandmark.LEFT_WRIST.value].x,
                             landmarks[mp_pose.PoseLandmark.LEFT_WRIST.value].y]
                    angle2 = calculate_angle(shoulder, elbow, wrist)
                    cv2.putText(image, str(angle2),
                                tuple(np.multiply(elbow, [frame_width, frame_height]).astype(int)),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2, cv2.LINE_AA
                                )
                    if 190 > angle2 > 160:
                        stage = "up"

                    if 90 >= angle2 >= 30 and stage == 'up':
                        stage = "down"
                        counter += 1

                    mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS,
                                              mp_drawing.DrawingSpec(color=(245, 117, 66), thickness=2,
                                                                     circle_radius=3),
                                              mp_drawing.DrawingSpec(color=(245, 66, 230), thickness=2, circle_radius=3)
                                              )
                else:
                    mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS,
                                              mp_drawing.DrawingSpec(color=(0, 0, 255), thickness=2,
                                                                     circle_radius=3),
                                              mp_drawing.DrawingSpec(color=(0, 0, 255), thickness=2, circle_radius=3)
                                              )

            elif workout == 'squat':
                shoulder = [landmarks[mp_pose.PoseLandmark.LEFT_SHOULDER.value].x,
                            landmarks[mp_pose.PoseLandmark.LEFT_SHOULDER.value].y]
                hip = [landmarks[mp_pose.PoseLandmark.LEFT_HIP.value].x,
                       landmarks[mp_pose.PoseLandmark.LEFT_HIP.value].y]
                knee = [landmarks[mp_pose.PoseLandmark.LEFT_KNEE.value].x,
                        landmarks[mp_pose.PoseLandmark.LEFT_KNEE.value].y]

                angle = calculate_angle(shoulder, hip, knee)

                cv2.putText(image, str(angle),
                            tuple(np.multiply(hip, [frame_width, frame_height]).astype(int)),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2, cv2.LINE_AA
                            )

                # calculate distance between both shoulders (Left and Right)
                shoulder_distance = calculate_distance(shoulder,
                                                       [landmarks[mp_pose.PoseLandmark.RIGHT_SHOULDER.value].x,
                                                        landmarks[mp_pose.PoseLandmark.RIGHT_SHOULDER.value].y])

                left_ankle = [landmarks[mp_pose.PoseLandmark.LEFT_ANKLE.value].x,
                              landmarks[mp_pose.PoseLandmark.LEFT_ANKLE.value].y]
                right_ankle = [landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].x,
                               landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].y]

                ankle_distance = round(calculate_distance(left_ankle, right_ankle), 2)
                min_shoulder_distance = round(shoulder_distance - shoulder_distance * 0.2, 2)
                max_shoulder_distance = round(shoulder_distance + shoulder_distance * 0.5, 2)

                print(angle)

                if max_shoulder_distance >= ankle_distance >= min_shoulder_distance:
                    if 190 > angle > 160:
                        stage = "up"

                    if 150 >= angle >= 40 and stage == 'up':
                        stage = "down"
                        counter += 1

                    mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS,
                                              mp_drawing.DrawingSpec(color=(245, 117, 66), thickness=2,
                                                                     circle_radius=3),
                                              mp_drawing.DrawingSpec(color=(245, 66, 230), thickness=2, circle_radius=3)
                                              )
                else:
                    mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS,
                                              mp_drawing.DrawingSpec(color=(0, 0, 255), thickness=2,
                                                                     circle_radius=3),
                                              mp_drawing.DrawingSpec(color=(0, 0, 255), thickness=2, circle_radius=3)
                                              )


        except:
            pass
        # Render counter
        # Setup status box
        cv2.rectangle(image, (0, 0), (325, 100), (245, 117, 16), -1)

        # Rep data
        cv2.putText(image, 'REPS', (15, 12),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 1, cv2.LINE_AA)
        cv2.putText(image, str(counter),
                    (10, 60),
                    cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 255, 255), 2, cv2.LINE_AA)

        # Stage data
        cv2.putText(image, 'STAGE', (85, 12),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 1, cv2.LINE_AA)
        cv2.putText(image, stage,
                    (85, 60),
                    cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 255, 255), 2, cv2.LINE_AA)

        # Render detections
        # mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_pose.POSE_CONNECTIONS,
        #                           mp_drawing.DrawingSpec(color=(245, 117, 66), thickness=2, circle_radius=3),
        #                           mp_drawing.DrawingSpec(color=(245, 66, 230), thickness=2, circle_radius=3)
        #                           )

        # cv2.namedWindow('Mediapipe Feed', cv2.WND_PROP_FULLSCREEN)
        # cv2.setWindowProperty('Mediapipe Feed', cv2.WND_PROP_FULLSCREEN, cv2.WINDOW_FULLSCREEN)
        cv2.imshow('Mediapipe Feed', image)

        if cv2.waitKey(10) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()
