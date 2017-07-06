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

package com.arialyy.aria.core.download;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.downloader.DownloadListener;
import com.arialyy.aria.core.download.downloader.DownloadUtil;
import com.arialyy.aria.core.download.downloader.IDownloadListener;
import com.arialyy.aria.core.inf.AbsNormalTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class DownloadTask extends AbsNormalTask<DownloadEntity> {
  public static final String TAG = "DownloadTask";

  private IDownloadListener mListener;
  private DownloadUtil mUtil;
  private boolean isWait = false;

  private DownloadTask(DownloadTaskEntity taskEntity, Handler outHandler) {
    mEntity = taskEntity.getEntity();
    mOutHandler = outHandler;
    mContext = AriaManager.APP;
    mListener = new DListener(mContext, this, mOutHandler);
    mUtil = new DownloadUtil(taskEntity, mListener);
  }

  /**
   * 获取文件保存路径
   *
   * @return 如果路径不存在，返回null
   */
  public String getDownloadPath() {
    File file = new File(mEntity.getDownloadPath());
    if (!file.exists()) {
      return null;
    }
    return mEntity.getDownloadPath();
  }

  /**
   * 获取当前下载任务的下载地址
   *
   * @see DownloadTask#getKey()
   */
  @Deprecated public String getDownloadUrl() {
    return mEntity.getDownloadUrl();
  }

  @Override public String getKey() {
    return getDownloadUrl();
  }

  /**
   * 任务下载状态
   *
   * @see DownloadTask#isRunning()
   */
  @Deprecated public boolean isDownloading() {
    return mUtil.isDownloading();
  }

  @Override public boolean isRunning() {
    return isDownloading();
  }

  public DownloadEntity getDownloadEntity() {
    return mEntity;
  }

  /**
   * 暂停任务，并让任务处于等待状态
   */
  @Override public void stopAndWait() {
    stop(true);
  }

  /**
   * 设置最大下载速度，单位：kb
   *
   * @param maxSpeed 为0表示不限速
   */
  public void setMaxSpeed(double maxSpeed) {
    mUtil.setMaxSpeed(maxSpeed);
  }

  /**
   * 开始下载
   */
  @Override public void start() {
    isWait = false;
    if (mUtil.isDownloading()) {
      Log.d(TAG, "任务正在下载");
    } else {
      if (mListener == null || isWait) {
        mListener = new DListener(mContext, this, mOutHandler);
      }
      mUtil.startDownload();
    }
  }

  /**
   * 停止下载
   */
  @Override public void stop() {
    stop(false);
  }

  private void stop(boolean isWait) {
    this.isWait = isWait;
    if (mUtil.isDownloading()) {
      mUtil.stopDownload();
    } else {
      mEntity.setState(isWait ? IEntity.STATE_WAIT : IEntity.STATE_STOP);
      mEntity.update();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.STOP, this).sendToTarget();
      }
    }
  }

  /**
   * 取消下载
   */
  @Override public void cancel() {
    if (!mEntity.isDownloadComplete()) {
      if (!mUtil.isDownloading()) {
        if (mOutHandler != null) {
          mOutHandler.obtainMessage(ISchedulers.CANCEL, this).sendToTarget();
        }
      }
      mUtil.cancelDownload();
    }
  }

  public static class Builder {
    DownloadTaskEntity taskEntity;
    Handler outHandler;
    String targetName;

    public Builder(String targetName, DownloadTaskEntity taskEntity) {
      CheckUtil.checkTaskEntity(taskEntity);
      this.targetName = targetName;
      this.taskEntity = taskEntity;
    }

    /**
     * 设置自定义Handler处理下载状态时间
     *
     * @param schedulers {@link ISchedulers}
     */
    public Builder setOutHandler(ISchedulers schedulers) {
      this.outHandler = new Handler(schedulers);
      return this;
    }

    public DownloadTask build() {
      DownloadTask task = new DownloadTask(taskEntity, outHandler);
      task.setTargetName(targetName);
      taskEntity.getEntity().save();
      taskEntity.save();
      return task;
    }
  }

  /**
   * 下载监听类
   */
  private static class DListener extends DownloadListener {
    WeakReference<Handler> outHandler;
    WeakReference<DownloadTask> wTask;
    Context context;
    long lastLen = 0;   //上一次发送长度
    long lastTime = 0;
    long INTERVAL_TIME = 1000;   //1m更新周期
    boolean isFirst = true;
    DownloadEntity entity;
    DownloadTask task;
    boolean isConvertSpeed = false;

    DListener(Context context, DownloadTask task, Handler outHandler) {
      this.context = context;
      this.outHandler = new WeakReference<>(outHandler);
      this.wTask = new WeakReference<>(task);
      this.task = wTask.get();
      this.entity = this.task.getDownloadEntity();
      final AriaManager manager = AriaManager.getInstance(context);
      isConvertSpeed = manager.getDownloadConfig().isConvertSpeed();
    }

    @Override public void supportBreakpoint(boolean support) {
      if (!support) {
        sendInState2Target(ISchedulers.SUPPORT_BREAK_POINT);
      }
    }

    @Override public void onPre() {
      sendInState2Target(ISchedulers.PRE);
      saveData(IEntity.STATE_PRE, -1);
    }

    @Override public void onPostPre(long fileSize) {
      entity.setFileSize(fileSize);
      sendInState2Target(ISchedulers.POST_PRE);
      saveData(IEntity.STATE_POST_PRE, -1);
    }

    @Override public void onResume(long resumeLocation) {
      sendInState2Target(ISchedulers.RESUME);
      saveData(IEntity.STATE_RUNNING, resumeLocation);
    }

    @Override public void onStart(long startLocation) {
      sendInState2Target(ISchedulers.START);
      saveData(IEntity.STATE_RUNNING, startLocation);
    }

    @Override public void onProgress(long currentLocation) {
      if (System.currentTimeMillis() - lastTime > INTERVAL_TIME) {
        long speed = currentLocation - lastLen;
        lastTime = System.currentTimeMillis();
        if (isFirst) {
          speed = 0;
          isFirst = false;
        }
        handleSpeed(speed);
        entity.setCurrentProgress(currentLocation);
        lastLen = currentLocation;
        sendInState2Target(ISchedulers.RUNNING);
      }
    }

    @Override public void onStop(long stopLocation) {
      handleSpeed(0);
      sendInState2Target(ISchedulers.STOP);
      saveData(task.isWait ? IEntity.STATE_WAIT : IEntity.STATE_STOP, stopLocation);
    }

    @Override public void onCancel() {
      handleSpeed(0);
      sendInState2Target(ISchedulers.CANCEL);
      saveData(IEntity.STATE_CANCEL, -1);
      entity.deleteData();
    }

    @Override public void onComplete() {
      handleSpeed(0);
      sendInState2Target(ISchedulers.COMPLETE);
      saveData(IEntity.STATE_COMPLETE, entity.getFileSize());
    }

    @Override public void onFail() {
      entity.setFailNum(entity.getFailNum() + 1);
      handleSpeed(0);
      sendInState2Target(ISchedulers.FAIL);
      saveData(IEntity.STATE_FAIL, -1);
    }

    private void handleSpeed(long speed) {
      if (isConvertSpeed) {
        entity.setConvertSpeed(CommonUtil.formatFileSize(speed) + "/s");
      } else {
        entity.setSpeed(speed);
      }
    }

    /**
     * 将任务状态发送给下载器
     *
     * @param state {@link ISchedulers#START}
     */
    private void sendInState2Target(int state) {
      if (outHandler.get() != null) {
        outHandler.get().obtainMessage(state, task).sendToTarget();
      }
    }

    private void saveData(int state, long location) {
      entity.setState(state);
      entity.setDownloadComplete(state == IEntity.STATE_COMPLETE);
      entity.setCurrentProgress(location);
      entity.update();

    }
  }
}