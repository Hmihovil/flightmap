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

package com.google.flightmap.android.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import android.os.AsyncTask;
import com.google.flightmap.common.ProgressListener;
import com.google.flightmap.common.net.FileUpdater;

public class FileUpdaterTask extends AsyncTask<FileUpdaterTask.Params, Integer, Boolean> implements
    ProgressListener {
  final ProgressListener listener;

  public FileUpdaterTask(final ProgressListener listener) {
    this.listener = listener;
  }

  @Override
  protected Boolean doInBackground(Params... params) {
    try {
      final Params param = params[0];
      final FileUpdater updater = new FileUpdater(param.file, param.url, param.workingDir);
      if (!updater.isUpdateNeeded()) {
        return Boolean.TRUE;
      }
      updater.addProgressListener(this);
      updater.update();
      return Boolean.TRUE;
    } catch (IOException ex) {
      ex.printStackTrace();
      return Boolean.FALSE;
    }
  }

  @Override
  public void hasProgressed(int percent) {
    publishProgress(percent);
  }

  @Override
  public void hasCompleted(boolean success) {
    publishProgress(100);
  }

  @Override
  protected void onProgressUpdate(Integer... percent) {
    if (listener != null) {
      listener.hasProgressed(percent[0]);
    }
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if (listener != null) {
      listener.hasCompleted(result);
    }
  }

  public static class Params {
    final File file;
    final URL url;
    final File workingDir;

    /**
     * Initializes {@link FileUpdaterTask} parameters.
     *
     * @param file  Local destination file
     * @param url  URL to download
     * @param workingDir Temporary local directory.  Will be created if needed.
     */
    Params(final File file, final URL url, final File workingDir) {
      this.file = file;
      this.url = url;
      this.workingDir = workingDir;
    }
  }
}
