# Introduction #

Here is how to make a new release of Flight Map and publish it.

# Detailed steps #
## 1: Make a release branch ##
See BranchesAndTags for details on how to do this. **Verify that you are on the release branch before going any further.**
## 2: Set version number ##
Check the android/AndroidManifest.xml and ensure both the versionName and versionCode are correct. Typically the versionName from trunk will be appropriate for the next release, except the last dot will be followed by "trunk" (e.g. 0.9.trunk).

Check that the versionCode is one higher than the last release by listing the existing tags:
```
$ svn ls https://flightmap.googlecode.com/svn/tags
0005-release-0.5/
0006-release-0.5.1/
0007-release-0.7.0/
```

The first part of the tag name are the already released version codes. This new release should have the next version code (8 for the example above).

**Commit** the android/AndroidManifest.xml file on the **release branch**

## 3: Make any other branch-specific changes ##
Make changes specific to this release (e.g. remove functionality that isn't ready yet). There may be nothing release-specific to do; this step is optional.

If there are changes specific to this release (i.e. are not merged from trunk before Step 6), add them to ReleaseVsTrunkDiff.

## 4: Build release apk ##
```
cd android
ant release
```
You'll be prompted for 2 passwords to sign the release.

## 5: Test ##
Test on an actual phone in addition to on the emulator. If the current version installed was not signed with the release key (e.g. a developer build), you'll need to uninstall that first.

You can use the `adb install` command to install the apk to your phone or emulator.

If tests pass, go to step 6.  Else return to step 3.

## 6: Tag the release ##
**Make sure you are in the top-level directory (not in android), and still on the release branch.**

Create a tag for this release. See BranchesAndTags for details.

## 7: Release Notes ##
  * Update the ReleaseVsTrunkDiff, even if there are no diffs from trunk.
  * Update the DogfoodReleaseNotes for this release.
  * Update the "what's new in version x.x.x" section in the market.

## 8: Publish ##
Put the release on the market. Announce to appropriate mailing list.

## 9: Update trunk version ##
Get trunk ready for the next release. Switch back to the trunk (see BranchesAndTags for details), and modify the android/AndroidManifest.xml by setting the versionName and versionCode for the next release. This helps distinguish developer builds from release builds with a quick glance at the version number.