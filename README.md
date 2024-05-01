# ImageSpeak

Our Image Captioning app designed for people who are visually impaired.

# Running the app
1. Start the server.py file located in the Server folder. (Our model requires 10 GBs of vRAM else the server won't run successfully)
2. Set up HTTP traffic forwarding from any public url to the url and port of the server started in step 1.
3. Change the BASE_URL variable in the MainActivity.kt (app/src/main/java/com/sam/imagespeak/MainActivity.kt) to the public url created in step 2.
4. Build the app (Preferably using Android Studio).
5. Install the app on any android device through the apk file generated while building.
6. Give the required permissions (Camera & Mic) to the app and you are good to go!

