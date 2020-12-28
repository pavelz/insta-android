/Users/pavel/Library/Android/sdk/platform-tools/adb -d shell "run-as com.example.insta_android cat /data/data/com.example.insta_android/databases/photos" > photos
/Users/pavel/Library/Android/sdk/platform-tools/adb -d shell "run-as com.example.insta_android cat /data/data/com.example.insta_android/databases/photos-shm" > photos-shm
/Users/pavel/Library/Android/sdk/platform-tools/adb -d shell "run-as com.example.insta_android cat /data/data/com.example.insta_android/databases/photos-wal" > photos-wal
