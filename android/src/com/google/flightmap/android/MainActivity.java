/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.flightmap.android;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.flightmap.android.db.AndroidAviationDbAdapter;
import com.google.flightmap.android.location.LocationHandler;
import com.google.flightmap.android.location.SimulatorDialog;
import com.google.flightmap.android.map.MapView;
import com.google.flightmap.android.net.DbUpdaterTask;
import com.google.flightmap.common.ProgressListener;
import com.google.flightmap.common.db.CachedAirportDirectory;
import com.google.flightmap.common.db.CachedAviationDbAdapter;
import com.google.flightmap.common.db.CustomGridAirportDirectory;
import com.google.flightmap.common.db.DbAdapter;

public class MainActivity extends Activity {
  private static final String TAG = MainActivity.class.getSimpleName();
  /**
   * Milliseconds between screen updates. Note that the fastest I've seen GPS
   * updates arrive is once per second.
   */
  private static long UPDATE_RATE = 100;

  // Dialog identifiers (must be unique).
  private static final int DOWNLOAD_FAILED_DIALOG = 1;
  private static final int SIMULATOR_DIALOG = 2;

  // Saved instance state constants
  private static final String DISCLAIMER_ACCEPTED = "disclaimer-accepted";
  private static final String DATABASE_DOWNLOADED = "database-downloaded";

  private static final String AVIATION_DATABASE_URL =
      "http://sites.google.com/site/flightmapdata/aviation-db/aviation.db";
  private static final int AVIATION_DATABASE_REQUIRED_SCHEMA_VERSION = 4;

  private boolean disclaimerAccepted;
  private boolean isRunning;
  private UpdateHandler updater = new UpdateHandler();
  private FrameLayout mapFrame;
  private MapView mapView;
  private boolean initializationDone;
  private boolean databaseDownloaded;
  private DbUpdaterTask dbUpdaterTask;
  private FlightMap flightMap;
  private CachedAviationDbAdapter aviationDbAdapter;
  private CachedAirportDirectory airportDirectory;
  private UserPrefs userPrefs;
  private SimulatorDialog simulatorDialog;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    flightMap = (FlightMap) getApplication();
    if (flightMap.getLocationHandler() == null) {
      flightMap.setLocationHandler(new LocationHandler(
          (LocationManager) getSystemService(Context.LOCATION_SERVICE)));
    } else {
      Log.i(TAG, "location handler already exists");
    }

    userPrefs = new UserPrefs(flightMap);
    setDatabaseDownloaded((null != savedInstanceState && savedInstanceState
        .getBoolean(DATABASE_DOWNLOADED)));

    // Map view initialization.
    setContentView(R.layout.mapview);
    mapFrame = (FrameLayout) findViewById(R.id.map_frame);
    mapView = new MapView(this);

    // The MapView must be the first child.
    mapFrame.addView(mapView, 0);

    // Show the disclaimer screen if there's no previous state, or the user
    // didn't accept the disclaimer.
    if (null == savedInstanceState || !savedInstanceState.getBoolean(DISCLAIMER_ACCEPTED)) {
      setDisclaimerAccepted(false);
      showDisclaimerView();
    } else { // disclaimer accepted.
      setDisclaimerAccepted(true);
      downloadDatabase();
    }
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    if (id == SIMULATOR_DIALOG) {
      simulatorDialog.setUnits(getUserPrefs().getDistanceUnits());
      simulatorDialog.updateDialog();
    }
    super.onPrepareDialog(id, dialog);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DOWNLOAD_FAILED_DIALOG:
        return new AlertDialog.Builder(this).setMessage(R.string.download_failed)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }).create();

      case SIMULATOR_DIALOG:
        simulatorDialog = new SimulatorDialog(this, flightMap.getLocationHandler());
        return simulatorDialog;

      default:
        return null;
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(DISCLAIMER_ACCEPTED, isDisclaimerAccepted());
    outState.putBoolean(DATABASE_DOWNLOADED, isDatabaseDownloaded());
    mapView.saveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mapView.restoreInstanceState(savedInstanceState);
  }

  private void showDisclaimerView() {
    setContentView(R.layout.disclaimer);

    // Set disclaimer "agree" button to switch to map view.
    final Button agreeButton = (Button) findViewById(R.id.agree);
    agreeButton.setEnabled(true);
    agreeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        agreeButton.setEnabled(false);
        setDisclaimerAccepted(true);
        downloadDatabase();
      }
    });
  }

  /**
   * If there is a network connection, try to update the database.
   */
  private void downloadDatabase() {
    if (isDatabaseDownloaded()) {
      downloadDatabaseDone();
      return;
    }

    final File localFile = new File(AndroidAviationDbAdapter.DATABASE_PATH);
    final File workingDir = new File(AndroidAviationDbAdapter.DATABASE_PATH + ".work");
    URL url = null;
    try {
      url = new URL(AVIATION_DATABASE_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Bad url", e);
      return;
    }

    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setMessage(this.getString(R.string.updating_database));
    dialog.setCancelable(false);

    final ProgressListener dbUpdateListener = new ProgressListener() {
      @Override
      public void hasCompleted(boolean success) {
        dialog.dismiss();
        Log.i(TAG, "Download completed.  Success: " + success);
        if (!success) {
          showDialog(DOWNLOAD_FAILED_DIALOG);
          Log.e(TAG, "Download failed");
        }
        downloadDatabaseDone();
      }

      @Override
      public void hasProgressed(int percent) {
        dialog.show();
        dialog.setProgress(percent);
      }
    };

    final DbAdapter dbAdapter = new AndroidAviationDbAdapter(getUserPrefs());
    final DbUpdaterTask.Params params =
        new DbUpdaterTask.Params(localFile, url, workingDir, dbAdapter,
            AVIATION_DATABASE_REQUIRED_SCHEMA_VERSION);
    dbUpdaterTask = new DbUpdaterTask(dbUpdateListener);
    dbUpdaterTask.execute(params);
  }

  /**
   * Called after the database is downloaded successfully.
   */
  private void downloadDatabaseDone() {
    setDatabaseDownloaded(true);
    initializeApplication();
  }

  private synchronized void initializeApplication() {
    aviationDbAdapter = new CachedAviationDbAdapter(new AndroidAviationDbAdapter(getUserPrefs()));
    airportDirectory =
        new CachedAirportDirectory(new CustomGridAirportDirectory(getAviationDbAdapter()));

    // TODO: handle the case of this throwing when there's no database.
    airportDirectory.open();

    setInitializationDone(true);
    setRunning(true);

    // Now show the map.
    setContentView(mapFrame);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // Simulator menu item may be hidden by user prefs.
    MenuItem simulatorItem = menu.findItem(R.id.simulator);
    boolean showSimulatorItem = getUserPrefs().enableSimulator();
    simulatorItem.setVisible(showSimulatorItem);
    simulatorItem.setEnabled(showSimulatorItem);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId()) {
      case R.id.preferences:
        Intent startIntent = new Intent(this, UserPrefsActivity.class);
        startActivity(startIntent);
        return true;

      case R.id.simulator:
        showDialog(SIMULATOR_DIALOG);
        return true;
    }
    return false;
  }

  @Override
  protected void onResume() {
    super.onResume();
    flightMap.getLocationHandler().startListening();
    setRunning(isInitializationDone());
    update();
  }

  @Override
  protected void onPause() {
    super.onPause();
    setRunning(false);
    flightMap.getLocationHandler().stopListening();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.destroy();
    if (airportDirectory != null) {
      airportDirectory.close();
    }
  }

  /**
   * Updates the view every {@link #UPDATE_RATE} milliseconds using
   * {@link UpdateHandler}.
   */
  private void update() {
    updater.scheduleUpdate(UPDATE_RATE);
    if (!isRunning()) {
      return;
    }
    drawUi();
  }

  private void drawUi() {
    if (!isDisclaimerAccepted()) {
      return;
    }
    Location location = flightMap.getLocationHandler().getLocation();
    mapView.drawMap(location);
  }

  /**
   * Updates the UI using a delayed message.
   */
  private class UpdateHandler extends Handler {
    private static final int UPDATE_MESSAGE = 1;

    @Override
    public void handleMessage(Message msg) {
      update();
    }

    /**
     * Call {@link #update} after {@code delay} milliseconds.
     */
    public void scheduleUpdate(long delay) {
      removeMessages(UPDATE_MESSAGE);
      sendMessageDelayed(obtainMessage(UPDATE_MESSAGE), delay);
    }
  }

  private synchronized boolean isRunning() {
    return isRunning;
  }

  private synchronized void setRunning(boolean isRunning) {
    this.isRunning = isRunning;
  }

  public synchronized void setDisclaimerAccepted(boolean disclaimerAccepted) {
    this.disclaimerAccepted = disclaimerAccepted;
  }

  public synchronized boolean isDisclaimerAccepted() {
    return disclaimerAccepted;
  }

  private synchronized boolean isInitializationDone() {
    return initializationDone;
  }

  private synchronized void setInitializationDone(final boolean initializationDone) {
    this.initializationDone = initializationDone;
  }

  private synchronized boolean isDatabaseDownloaded() {
    return databaseDownloaded;
  }

  private synchronized void setDatabaseDownloaded(final boolean databaseDownloaded) {
    this.databaseDownloaded = databaseDownloaded;
  }

  public UserPrefs getUserPrefs() {
    return userPrefs;
  }

  public FlightMap getFlightMap() {
    return flightMap;
  }

  public CachedAviationDbAdapter getAviationDbAdapter() {
    return aviationDbAdapter;
  }

  public CachedAirportDirectory getAirportDirectory() {
    return airportDirectory;
  }
}
