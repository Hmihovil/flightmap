# Force Close on start #
If you get an application error and a force close message on start, chances are the database you're accessing doesn't match the current code version. Sync to the latest build, connect your device or emulator to your computer with the Android SDK installed, and run this command:

```
adb push data/aviation.db /sdcard/com.google.flightmap/aviation.db
```