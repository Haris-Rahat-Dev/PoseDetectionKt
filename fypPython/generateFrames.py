import os
import cv2


def video_to_frames(video_path: str, destination: str):
    global count
    if not os.path.exists(os.path.join(os.getcwd(), 'output2')):
        os.mkdir(os.path.join(os.getcwd(), 'output2'))
    if not os.path.exists(destination):
        os.mkdir(destination)
    vid = cv2.VideoCapture(video_path)  # read the video
    success, image = vid.read()  # read the first image
    # flip the image horizontally

    # count = 0
    while success:  # in case there are more images - proceed
        image = cv2.flip(image, 1)
        cv2.imwrite(os.path.join(destination, f'image_{count}.jpg'),
                    image)  # write the image to the destination directory
        success, image = vid.read()  # read the next image
        count += 1


if __name__ == '__main__':
    count = 1
    for video in os.listdir(os.path.join(os.getcwd(), 'BodyWeightSquats')):
        video_to_frames(os.path.join(os.getcwd(), 'BodyWeightSquats', video), 'output2')
