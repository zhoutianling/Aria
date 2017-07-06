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

package com.arialyy.aria.core.download.downloader;

import android.util.Log;
import com.arialyy.aria.core.download.DownloadTaskEntity;

/**
 * Created by lyy on 2015/8/25.
 * 下载工具类
 */
public class DownloadUtil implements IDownloadUtil, Runnable {
  private static final String TAG = "DownloadUtil";
  private IDownloadListener mListener;
  private Downloader mDT;
  private DownloadTaskEntity mTaskEntity;

  public DownloadUtil(DownloadTaskEntity entity, IDownloadListener downloadListener) {
    mTaskEntity = entity;
    mListener = downloadListener;
    mDT = new Downloader(downloadListener, entity);
  }

  @Override public long getFileSize() {
    return mDT.getFileSize();
  }

  /**
   * 获取当前下载位置
   */
  @Override public long getCurrentLocation() {
    return mDT.getCurrentLocation();
  }

  @Override public boolean isDownloading() {
    return mDT.isDownloading();
  }

  /**
   * 取消下载
   */
  @Override public void cancelDownload() {
    mDT.cancelDownload();
  }

  /**
   * 停止下载
   */
  @Override public void stopDownload() {
    mDT.stopDownload();
  }

  /**
   * 多线程断点续传下载文件，开始下载
   */
  @Override public void startDownload() {
    mListener.onPre();
    new Thread(this).start();
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  public void setMaxSpeed(double maxSpeed) {
    mDT.setMaxSpeed(maxSpeed);
  }

  private void failDownload(String msg) {
    Log.e(TAG, msg);
    mListener.onFail();
  }

  @Override public void run() {
    new Thread(new FileInfoThread(mTaskEntity, new FileInfoThread.OnFileInfoCallback() {
      @Override public void onComplete(String url, int code) {
        mDT.startDownload();
      }

      @Override public void onFail(String url, String errorMsg) {
        failDownload(errorMsg);
      }
    })).start();
  }
}