
# Introduction #

Read GettingStarted if you haven't already.

Eclipse is the easiest way to do Flight Map development, but it's not required. Here's how to set up Eclipse for this project. These instructions assume you know the basics of how to develop Java code in Eclipse, and that you've worked through one or more of the Android SDK tutorials using Eclipse. See the [Android Eclipse setup instructions](http://developer.android.com/sdk/eclipse-adt.html) for details on installing the Eclipse plugin.

# Android SDK #
After installing the Android ADT plugin for Eclipse, you must [configure it](http://developer.android.com/sdk/eclipse-adt.html#configuring).

Short version: Preferences > Android > Browse to location of Android SDK.

# Java Compiler Version #

Configure Eclipse to use a 1.6 compiler.
  * Preferences > Java > Compiler > Compiler - should be 1.6 with all boxes checked.
  * Preferences > Java > Installed JREs - check "JVM 1.6"

# Code Style #
Use the Eclipse auto-formatter to format your code in a style that's consistent with other Flight Map developers. Here's how:
  1. Download the [flightmap-eclipse-format.xml](http://flightmap.googlecode.com/files/flightmap-eclipse-format.xml) file.
  1. In Eclipse, File > Import. General > Preferences. Then select the above file.
  1. On a Mac, Eclipse > Preferences (or command-comma). Filter on "formatter" to get to Java > Code Style > Formatter. Click the "Import" button and choose the above file.

Use the Eclipse auto-formatter by hitting ctl+shift+F (ctl+cmd+F on a Mac).

# Tips #
Use the **LogCat** view to see log output from your emulator (Window > View > Other > LogCat).

Use the **DDMS** perpective to send mock locations or replay a GPS track from a GPX file to your emulator (Window > Open Perspective > Other > DDMS).

# Eclipse Projects #
You'll need to create 3 different projects to correspond to the 3 code ProjectDirectories. One will be an Android project for the phone code, one will be for the common code and a third will be for the preprocessing code.

Depending on what part of the project you are working on, you may not actually need all three projects, but it's best to just set all three up in the beginning.

## Android Project (1 of 3) ##

Create a new Android Project:
  1. File > New > Project... > **Android Project**
  1. Project Name: **FlightMap-Android**
  1. **Create project from existing source**
  1. Set Location to where you have the `android` directory checked out (e.g. `/src/flightmap/android`).
  1. Build Target
    * **Android 2.2 (API level 8)**
  1. Min SDK Version: **7**
  1. Finish

**Note** that the build target is API 8, but the Min SDK version is 7. That's because we want to allow installing to the SD card, which requires building with SDK 8. However we don't currently use any SDK 8 features, so the min SDK version is 7.

The project icon will have a little red X and lots of problems indicated. That's because the Android code has a dependency on the `common` project that you need to configure in the project properties. We'll take care of this soon in the "Add dependency on common code" section below.

## Preprocessing Project (2 of 3) ##
Do the same as for the common project above to create the FlightMap-Preprocessing project.
  1. File > New > **Java Project**
  1. Project Name: **FlightMap-Preprocessing**
  1. **Create project from existing source**
  1. Set Location to where you have the `preprocessing` directory checked out (e.g. `/src/flightmap/preprocessing`).
  1. Finish

Now add the libraries that the preprocessing project needs.
  1. Right-click on the newly-created FlightMap-Preprocessing project in the Package Explorer window on the left side.
  1. Build Path > Configure Build Path
  1. Click on the "Libraries" tab
    * Depending on your version of Eclipse, you'll either have a long list of .jar files, or just the JRE System Library.
    * **Delete all .jar files listed** by clicking the first one, shift-clicking the last one, and hitting the "Remove" button.
  1. Click **Add JARs...**
  1. Expand FlightMap-Preprocessing/lib
  1. Choose all the .jar files **EXCEPT flightmap-common.jar**.

Just like with the FlightMap-Android project, this one will have a little red X because you need to add the common project to the build path. Again, we'll take care of this soon in the "Add dependency on common code" section below.

## Common Project (3 of 3) ##
Create a project for the common code.
  1. File > New > **Java Project**
  1. Project Name: **FlightMap-Common**
  1. **Create project from existing source**
  1. Set Location to where you have the `common` directory checked out (e.g. `/src/flightmap/common`).
  1. Finish

## Add dependency on common code ##
Now it's time to resolve the dependencies that are causing the FlightMap-Android and FlightMap-Preprocessing projects to have build errors. Both these projects depend on code that's in the FlightMap-Common project.

To resolve these dependencies, we just need to let Eclipse know that the FlightMap-Android and FlightMap-Preprocessing projects depend on the FlightMap-Common project.

  1. Right-click on the newly-created FlightMap-Android project in the Package Explorer window on the left side.
  1. Build Path > Configure Build Path
  1. Click on the "Projects" tab
  1. Click "Add..."
  1. Under Select projects to add: choose FlightMap-Common
  1. Repeat the above steps for FlightMap-Preprocessing project

## Confirm dependencies are resolved ##
Now the little red X should be gone from the FlightMap-Android and FlightMap-Preprocessing projects.

# Important Note on changing Common Code #

You'll notice that the first two projects created depend on the code in `flightmap/common`.

If you make any changes to code in the **common** project, you'll need to re-generate the `common/release/flightmap-common.jar` file (which is checked in to subversion). Please use `ant` to do that as follows:
```
  cd common
  ant release
```

After doing the above, there are two more things to do:
  1. Refresh the `android` and `preprocessing` projects (right-click on the project > Refresh).
  1. Check in the updated flightmap-common.jar file.

# Eclipse Troubleshooting #

If something isn't working right with Eclipse, try re-creating the project as follows.
  1. Quit Eclipse
  1. `cd` into the appropriate subdirectory (`android`, `common`, or `preprocessing`)
  1. Remove the `.project` and `.classpath` files from the project that is giving you trouble.
  1. Start Eclipse and re-create the project as described above.