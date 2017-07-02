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

import android.util.SparseArray;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AriaL on 2017/6/30.
 * 任务组下载工具
 */
public class DownloadGroupUtil implements IDownloadUtil {
  private static final String TAG = "DownloadGroupUtil";
  /**
   * 任务组所有任务总大小
   */
  private long mTotalSize = 0;
  private long mCurrentProgress = 0;
  private ExecutorService mInfoPool;
  private ExecutorService mExePool;
  private IDownloadListener mListener;
  private DownloadGroupTaskEntity mTaskEntity;
  private boolean isRunning = true;
  private Timer mTimer;
  /**
   * 初始化完成的任务书数
   */
  private int mInitNum = 0;
  /**
   * 初始化失败的任务数
   */
  private int mFailNum = 0;
  /**
   * 保存所有没有下载完成的任务，key为下载地址
   */
  private Map<String, DownloadTaskEntity> mExeMap = new HashMap<>();

  /**
   * 下载失败的映射表，key为下载地址
   */
  private Map<String, DownloadTaskEntity> mFailMap = new HashMap<>();

  /**
   * 下载器映射表，key为下载地址
   */
  private Map<String, Downloader> mDownloaderMap = new HashMap<>();

  /**
   * 文件信息回调组
   */
  private SparseArray<FileInfoThread.OnFileInfoCallback> mFileInfoCallbacks = new SparseArray<>();

  public DownloadGroupUtil(IDownloadListener listener, DownloadGroupTaskEntity taskEntity) {
    mListener = listener;
    mTaskEntity = taskEntity;
    mInfoPool = Executors.newCachedThreadPool();
    mExePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    for (DownloadEntity entity : mTaskEntity.entity.getChild()) {
      File file = new File(entity.getDownloadPath());
      if (entity.isDownloadComplete() && file.exists()) {
        mTotalSize += entity.getFileSize();
      } else {
        mExeMap.put(entity.getDownloadUrl(), createDownloadTask(entity));
      }
      mCurrentProgress += entity.getCurrentProgress();
    }
  }

  @Override public long getFileSize() {
    return mTotalSize;
  }

  @Override public long getCurrentLocation() {
    return mCurrentProgress;
  }

  @Override public boolean isDownloading() {
    return isRunning;
  }

  @Override public void cancelDownload() {
    isRunning = false;
    closeTimer();
    if (!mInfoPool.isShutdown()) {
      mInfoPool.shutdown();
    }
    if (!mExePool.isShutdown()) {
      mExePool.shutdown();
    }

    Set<String> keys = mDownloaderMap.keySet();
    for (String key : keys) {
      Downloader dt = mDownloaderMap.get(key);
      if (dt != null) {
        dt.cancelDownload();
      }
    }
  }

  @Override public void stopDownload() {
    isRunning = false;
    closeTimer();
    if (!mInfoPool.isShutdown()) {
      mInfoPool.shutdown();
    }
    if (!mExePool.isShutdown()) {
      mExePool.shutdown();
    }

    Set<String> keys = mDownloaderMap.keySet();
    for (String key : keys) {
      Downloader dt = mDownloaderMap.get(key);
      if (dt != null) {
        dt.stopDownload();
      }
    }
  }

  @Override public void startDownload() {
    isRunning = true;
    Set<String> keys = mExeMap.keySet();
    mListener.onPre();
    for (String key : keys) {
      DownloadTaskEntity taskEntity = mExeMap.get(key);
      if (taskEntity != null) {
        mInfoPool.execute(createFileInfoThread(taskEntity));
      }
    }
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  /**
   * 创建文件信息获取线程
   */
  private FileInfoThread createFileInfoThread(DownloadTaskEntity taskEntity) {
    FileInfoThread.OnFileInfoCallback callback = mFileInfoCallbacks.get(taskEntity.hashCode());

    if (callback == null) {
      callback = new FileInfoThread.OnFileInfoCallback() {
        int failNum = 0;

        @Override public void onComplete(String url, int code) {
          DownloadTaskEntity te = mExeMap.get(url);
          if (te != null) {
            mTotalSize += te.getEntity().getFileSize();
            startChildDownload(te);
          }
          mInitNum++;
          if (mInitNum + mFailNum == mTaskEntity.getEntity().getChild().size()) {
            startRunningFlow();
          }
        }

        @Override public void onFail(String url, String errorMsg) {
          DownloadTaskEntity te = mExeMap.get(url);
          if (te != null) {
            mFailMap.put(url, te);
            mFileInfoCallbacks.put(te.hashCode(), this);
          }
          mFailNum++;
          failNum++;
          if (failNum < 10) {
            mInfoPool.execute(createFileInfoThread(te));
          }
          if (mInitNum + mFailNum == mTaskEntity.getEntity().getChild().size()) {
            startRunningFlow();
          }
        }
      };
    }

    return new FileInfoThread(taskEntity, callback);
  }

  private void closeTimer() {
    if (mTimer != null) {
      mTimer.purge();
      mTimer.cancel();
    }
  }

  /**
   * 开始进度流程
   */
  private void startRunningFlow() {
    mListener.onPostPre(mTotalSize);
    mListener.onStart(mCurrentProgress);
    closeTimer();
    mTimer = new Timer(true);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        mListener.onProgress(mCurrentProgress);
      }
    }, 1000);
  }

  /**
   * 启动子任务下载器
   */
  private void startChildDownload(DownloadTaskEntity taskEntity) {
    ChildDownloadListener listener = new ChildDownloadListener(taskEntity);
    Downloader dt = new Downloader(listener, taskEntity);
    mDownloaderMap.put(taskEntity.getEntity().getDownloadUrl(), dt);
    mExePool.execute(dt);
  }

  /**
   * 创建子任务下载信息
   */
  private DownloadTaskEntity createDownloadTask(DownloadEntity entity) {
    DownloadTaskEntity taskEntity =
        DbEntity.findData(DownloadTaskEntity.class, "key=?", entity.getDownloadUrl());
    if (taskEntity != null) {
      return taskEntity;
    }
    taskEntity = new DownloadTaskEntity(entity);
    taskEntity.headers = mTaskEntity.headers;
    taskEntity.requestEnum = mTaskEntity.requestEnum;
    taskEntity.redirectUrlKey = mTaskEntity.redirectUrlKey;
    taskEntity.removeFile = mTaskEntity.removeFile;
    return taskEntity;
  }

  /**
   * 子任务事件监听
   */
  private class ChildDownloadListener extends DownloadListener {

    DownloadTaskEntity taskEntity;
    DownloadEntity entity;

    ChildDownloadListener(DownloadTaskEntity entity) {
      this.taskEntity = entity;
      this.entity = taskEntity.getEntity();
    }

    @Override public void onPre() {
      saveData(IEntity.STATE_PRE, -1);
    }

    @Override public void onPostPre(long fileSize) {
      entity.setFileSize(fileSize);
      saveData(IEntity.STATE_POST_PRE, -1);
    }

    @Override public void onResume(long resumeLocation) {
      saveData(IEntity.STATE_POST_PRE, IEntity.STATE_RUNNING);
    }

    @Override public void onStart(long startLocation) {
      saveData(IEntity.STATE_POST_PRE, IEntity.STATE_RUNNING);
    }

    @Override public void onProgress(long currentLocation) {
      mCurrentProgress += currentLocation;
    }

    @Override public void onStop(long stopLocation) {
      saveData(IEntity.STATE_STOP, stopLocation);
    }

    @Override public void onCancel() {
      saveData(IEntity.STATE_CANCEL, -1);
    }

    @Override public void onComplete() {
      saveData(IEntity.STATE_COMPLETE, entity.getFileSize());
    }

    @Override public void onFail() {
      entity.setFailNum(entity.getFailNum() + 1);
      saveData(IEntity.STATE_FAIL, -1);
    }

    private void saveData(int state, long location) {
      entity.setState(state);
      entity.setDownloadComplete(state == IEntity.STATE_COMPLETE);
      entity.setCurrentProgress(location);
      entity.update();
    }
  }
}
