# Introduction #

The Flight Map code is split into multiple top-level directories. This needed because although the main output of this project is an Android application, we also have supporting code that is not meant to go on the phone. There is also some common code that is shared between the code that goes on the phone and the supporting code.

# Directory Details #

| **Directory** | **Description** | **Deployed to phone** | **Depends on Android SDK** |
|:--------------|:----------------|:----------------------|:---------------------------|
| android       | The Android application code | Yes                   | Yes                        |
| common        | Code that is used by the `android` code as well as other code. | Yes                   | No                         |
| data          | Databases and other static files that are deployed to the phone | Yes                   | No                         |
| preprocessing | Code that is not deployed to the phone. This is what parses the raw data from the FAA and other public sources and creates the databases in `data`. | No                    | No                         |