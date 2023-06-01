import cv2
import mediapipe as mp
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import os
import imutils
import tensorflow as tf
from tensorflow import keras
from keras import layers

mp_pose = mp.solutions.pose
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

train_points = []
train_labels = []
test_points = []
test_labels = []
class_map = {}

# Prepare the gallery folder and image file list
train_image_folder_name = 'pointnetData'
train_image_file_list = []
train_image_class_list = sorted(os.listdir(train_image_folder_name))

for i, subfolder in enumerate(train_image_class_list):
    file_list = sorted(os.listdir(train_image_folder_name + '/' + subfolder))
    for file in file_list:
        train_image_file_list.append(train_image_folder_name + '/' + subfolder + '/' + file)
        train_labels.append(i)
        class_map[i] = subfolder

print('Train Gallery: %d classes, %d images' % (len(train_image_class_list), len(train_image_file_list)))

test_image_folder_name = 'pointnetData'
test_image_file_list = []
test_image_class_list = sorted(os.listdir(test_image_folder_name))

for i, subfolder in enumerate(test_image_class_list):
    file_list = sorted(os.listdir(test_image_folder_name + '/' + subfolder))
    for file in file_list:
        test_image_file_list.append(test_image_folder_name + '/' + subfolder + '/' + file)
        test_labels.append(i)
        class_map[i] = subfolder

print('Test Gallery: %d classes, %d images' % (len(test_image_class_list), len(test_image_file_list)))
