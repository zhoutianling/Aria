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
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.downloader.DownloadGroupUtil;
import com.arialyy.aria.core.download.downloader.DownloadListener;
import com.arialyy.aria.core.download.downloader.IDownloadUtil;
import com.arialyy.aria.core.inf.AbsGroupTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.lang.ref.WeakReference;

/**
 * Created by AriaL on 2017/6/27.
 * 任务组任务
 */
public class DownloadGroupTask extends AbsGroupTask<DownloadGroupTaskEntity, DownloadGroupEntity> {
  private final String TAG = "DownloadGroupTask";
  private DListener mListener;
  private IDownloadUtil mUtil;

  private DownloadGroupTask(DownloadGroupTaskEntity taskEntity, Handler outHandler) {
    mTaskEntity = taskEntity;
    mEntity = taskEntity.getEntity();
    mOutHandler = outHandler;
    mContext = AriaManager.APP;
    mListener = new DListener(mContext, this, mOutHandler);
    mUtil = new DownloadGroupUtil(mListener, mTaskEntity);
  }

  @Override public boolean isRunning() {
    return mUtil.isDownloading();
  }

  @Override public void start() {
    mUtil.startDownload();
  }

  @Override public void stop() {
    if (!mUtil.isDownloading()) {
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.STOP, this).sendToTarget();
      }
    }
    mUtil.stopDownload();
  }

  @Override public void cancel() {
    if (!mUtil.isDownloading()) {
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.CANCEL, this).sendToTarget();
      }
    }
    mUtil.cancelDownload();
  }

  /**
   * 下载监听类
   */
  private static class DListener extends DownloadListener {
    private final String TAG = "DListener";
    WeakReference<Handler> outHandler;
    WeakReference<DownloadGroupTask> wTask;
    Context context;
    long lastLen = 0;   //上一次发送长度
    boolean isFirst = true;
    DownloadGroupEntity entity;
    DownloadGroupTask task;
    boolean isConvertSpeed = false;

    DListener(Context context, DownloadGroupTask task, Handler outHandler) {
      this.context = context;
      this.outHandler = new WeakReference<>(outHandler);
      this.wTask = new WeakReference<>(task);
      this.task = wTask.get();
      this.entity = this.task.getEntity();
      final AriaManager manager = AriaManager.getInstance(context);
      isConvertSpeed = manager.getDownloadConfig().isConvertSpeed();
    }

    @Override public void onPre() {
      saveData(IEntity.STATE_PRE, -1);
      sendInState2Target(ISchedulers.PRE);
    }

    @Override public void onPostPre(long fileSize) {
      entity.setFileSize(fileSize);
      entity.setConvertFileSize(CommonUtil.formatFileSize(fileSize));
      saveData(IEntity.STATE_POST_PRE, -1);
      sendInState2Target(ISchedulers.POST_PRE);
    }

    @Override public void onStart(long startLocation) {
      saveData(IEntity.STATE_RUNNING, startLocation);
      sendInState2Target(ISchedulers.START);
    }

    @Override public void onResume(long resumeLocation) {
      saveData(IEntity.STATE_RUNNING, resumeLocation);
      sendInState2Target(ISchedulers.RESUME);
    }

    @Override public void onProgress(long currentLocation) {
      entity.setCurrentProgress(currentLocation);
      long speed = currentLocation - lastLen;
      if (isFirst) {
        speed = 0;
        isFirst = false;
      }
      handleSpeed(speed);
      sendInState2Target(ISchedulers.RUNNING);
      lastLen = currentLocation;
    }

    @Override public void onStop(long stopLocation) {
      saveData(IEntity.STATE_STOP, stopLocation);
      handleSpeed(0);
      sendInState2Target(ISchedulers.STOP);
    }

    @Override public void onCancel() {
      saveData(IEntity.STATE_CANCEL, -1);
      handleSpeed(0);
      sendInState2Target(ISchedulers.CANCEL);
    }

    @Override public void onComplete() {
      saveData(IEntity.STATE_COMPLETE, entity.getFileSize());
      handleSpeed(0);
      sendInState2Target(ISchedulers.COMPLETE);
    }

    @Override public void onFail() {
      entity.setFailNum(entity.getFailNum() + 1);
      saveData(IEntity.STATE_FAIL, -1);
      handleSpeed(0);
      sendInState2Target(ISchedulers.FAIL);
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
     * @param state {@link DownloadSchedulers#START}
     */
    private void sendInState2Target(int state) {
      if (outHandler.get() != null) {
        outHandler.get().obtainMessage(state, task).sendToTarget();
      }
    }

    private void saveData(int state, long location) {
      if (state == IEntity.STATE_CANCEL) {
        entity.deleteData();
      } else {
        entity.setState(state);
        if (location != -1) {
          entity.setCurrentProgress(location);
        }
        entity.update();
      }
    }
  }

  public static class Builder {
    DownloadGroupTaskEntity taskEntity;
    Handler outHandler;
    String targetName;

    public Builder(String targetName, DownloadGroupTaskEntity taskEntity) {
      CheckUtil.checkTaskEntity(taskEntity);
      this.targetName = targetName;
      this.taskEntity = taskEntity;
    }

    /**
     * 设置自定义Handler处理下载状态时间
     *
     * @param schedulers {@link ISchedulers}
     */
    public DownloadGroupTask.Builder setOutHandler(ISchedulers schedulers) {
      this.outHandler = new Handler(schedulers);
      return this;
    }

    public DownloadGroupTask build() {
      DownloadGroupTask task = new DownloadGroupTask(taskEntity, outHandler);
      task.setTargetName(targetName);
      taskEntity.save();
      return task;
    }
  }
}
