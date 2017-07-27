/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.upload.uploader;

import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Aria.Lao on 2017/7/27.
 * 文件上传器
 */
public class Uploader implements Runnable, IUploadUtil {

  private IUploadListener mListener;
  private UploadTaskEntity mTaskEntity;
  private UploadEntity mEntity;
  private long mCurrentLocation = 0;

  private Timer mTimer;
  private boolean isRunning;

  Uploader(IUploadListener listener, UploadTaskEntity taskEntity) {
    mListener = listener;
    mTaskEntity = taskEntity;
    mEntity = mTaskEntity.getEntity();
  }

  @Override public long getFileSize() {
    return mEntity.getFileSize();
  }

  @Override public long getCurrentLocation() {
    return mCurrentLocation;
  }

  @Override public boolean isUploading() {
    return isRunning;
  }

  @Override public void cancelUpload() {
    isRunning = false;
  }

  @Override public void stopUpload() {
    isRunning = false;
  }

  @Override public void startUpload() {
    new Thread(this).start();
  }

  @Override public void resumeUpload() {
    startUpload();
  }

  @Override public void setMaxSpeed(double maxSpeed) {

  }

  @Override public void run() {
    isRunning = true;
  }

  /**
   * 启动进度获取定时器
   */
  private void startTimer() {
    mTimer = new Timer(true);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        //if (mConstance.isComplete() || !mConstance.isDownloading) {
        //  closeTimer();
        //} else if (mConstance.CURRENT_LOCATION >= 0) {
        //  mListener.onProgress(mConstance.CURRENT_LOCATION);
        //}
      }
    }, 0, 1000);
  }

  private void closeTimer() {
    if (mTimer != null) {
      mTimer.purge();
      mTimer.cancel();
    }
  }
}
