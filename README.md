# ModuleCameraX
CameraX is a Jetpack support library, built to help you make camera app development easier. It provides a consistent and easy-to-use API surface that works across most Android devices, with backward-compatibility to Android 5.0 (API level 21).

CameraX introduces use cases, which allow you to focus on the task you need to get done instead of spending time managing device-specific nuances. There are several basic use cases:

- Preview: get an image on the display
- Image analysis: access a buffer seamlessly for use in your algorithms, such as to pass into MLKit
- Image capture: save high-quality images

------------


Here in my project i have managed two use cases **Preview** and **Image capture**.
By adding this module in your project you can take picture and save it in your app memory.

------------


### How to use this Module 
- Create your project and add this module in it
- Open the activity where you want to use the camera
-  implement SavedImagePathInterface  to get the saved image path
- Also Create  CameraXHelperClass object and call the function openCamera
- By running the activity camera will be opened ,you can take and save the picture
