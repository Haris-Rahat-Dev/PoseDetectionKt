# write code for getting the videos from the videos/PushUps folder and renaming the videos as Pushup5.avi, Pushup(n).avi
import os

folder_path = "BodyWeightSquats"
i = 5

for filename in os.listdir(folder_path):
    if filename.endswith(".avi"):
        new_name = f"Squats{str(i)}.mp4"
        os.rename(os.path.join(folder_path, filename), os.path.join(folder_path, new_name))
        i += 1
