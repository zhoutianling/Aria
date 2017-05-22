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
package com.arialyy.aria.core.upload;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.util.CommonUtil;
import java.lang.ref.WeakReference;

/**
 * Created by Aria.Lao on 2017/2/23.
 * 上传任务
 */
public class UploadTask implements ITask {
  private static final String TAG = "UploadTask";
  private Handler mOutHandler;
  private UploadTaskEntity mTaskEntity;
  private UploadEntity mUploadEntity;
  private String mTargetName;

  private UploadUtil mUtil;
  private UListener mListener;

  UploadTask(UploadTaskEntity taskEntity, Handler outHandler) {
    mTaskEntity = taskEntity;
    mOutHandler = outHandler;
    mUploadEntity = mTaskEntity.uploadEntity;
    mListener = new UListener(mOutHandler, this);
    mUtil = new UploadUtil(mTaskEntity, mListener);
  }

  @Override public void setTargetName(String targetName) {
    mTargetName = targetName;
  }

  @Override public void removeRecord() {
    mUploadEntity.deleteData();
  }

  @Override public String getKey() {
    return mUploadEntity.getFilePath();
  }

  @Override public boolean isRunning() {
    return mUtil.isRunning();
  }

  public UploadEntity getUploadEntity() {
    return mUploadEntity;
  }

  @Override public IEntity getEntity() {
    return mUploadEntity;
  }

  @Override public void start() {
    if (mUtil.isRunning()) {
      Log.d(TAG, "任务正在下载");
    } else {
      if (mListener == null) {
        mListener = new UploadTask.UListener(mOutHandler, this);
      }
      mUtil.start();
    }
  }

  @Override public void stop() {

  }

  @Override public void cancel() {

    if (!mUploadEntity.isComplete()) {
      // 如果任务不是下载状态
      mUtil.cancel();
      mUploadEntity.deleteData();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(DownloadSchedulers.CANCEL, this).sendToTarget();
      }
      //发送取消下载的广播
      Intent intent = CommonUtil.createIntent(AriaManager.APP.getPackageName(), Aria.ACTION_CANCEL);
      intent.putExtra(Aria.ENTITY, mUploadEntity);
      AriaManager.APP.sendBroadcast(intent);
    }
  }

  public String getTargetName() {
    return mTargetName;
  }

  @Override public long getSpeed() {
    return mUploadEntity.getSpeed();
  }

  @Override public long getFileSize() {
    return mUploadEntity.getFileSize();
  }

  @Override public long getCurrentProgress() {
    return mUploadEntity.getCurrentProgress();
  }

  private static class UListener extends UploadListener {
    WeakReference<Handler> outHandler;
    WeakReference<UploadTask> task;
    long lastLen = 0;   //上一次发送长度
    long lastTime = 0;
    long INTERVAL_TIME = 1000;   //1m更新周期
    boolean isFirst = true;
    UploadEntity entity;
    Intent sendIntent;
    boolean isOpenBroadCast = false;
    Context context;

    UListener(Handler outHandle, UploadTask task) {
      this.outHandler = new WeakReference<>(outHandle);
      this.task = new WeakReference<>(task);
      entity = this.task.get().getUploadEntity();
      sendIntent = CommonUtil.createIntent(AriaManager.APP.getPackageName(), Aria.ACTION_RUNNING);
      sendIntent.putExtra(Aria.ENTITY, entity);
      context = AriaManager.APP;
      isOpenBroadCast = AriaManager.getInstance(context).getUploadConfig().isOpenBreadCast();
    }

    @Override public void onPre() {
      entity.setState(IEntity.STATE_PRE);
      sendIntent(Aria.ACTION_PRE, -1);
      sendInState2Target(ISchedulers.PRE);
    }

    @Override public void onStart(long fileSize) {
      entity.setFileSize(fileSize);
      entity.setState(IEntity.STATE_RUNNING);
      sendIntent(Aria.ACTION_PRE, -1);
      sendInState2Target(ISchedulers.START);
    }

    @Override public void onResume(long resumeLocation) {
      entity.setState(DownloadEntity.STATE_RUNNING);
      sendInState2Target(DownloadSchedulers.RESUME);
      sendIntent(Aria.ACTION_RESUME, resumeLocation);
    }

    @Override public void onStop(long stopLocation) {
      entity.setState(DownloadEntity.STATE_STOP);
      entity.setSpeed(0);
      sendInState2Target(DownloadSchedulers.STOP);
      sendIntent(Aria.ACTION_STOP, stopLocation);
    }

    @Override public void onProgress(long currentLocation) {
      if (System.currentTimeMillis() - lastTime > INTERVAL_TIME) {
        long speed = currentLocation - lastLen;
        sendIntent.putExtra(Aria.CURRENT_LOCATION, currentLocation);
        sendIntent.putExtra(Aria.CURRENT_SPEED, speed);
        lastTime = System.currentTimeMillis();
        if (isFirst) {
          entity.setSpeed(0);
          isFirst = false;
        } else {
          entity.setSpeed(speed);
        }
        entity.setCurrentProgress(currentLocation);
        lastLen = currentLocation;
        sendInState2Target(DownloadSchedulers.RUNNING);
        AriaManager.APP.sendBroadcast(sendIntent);
      }
    }

    @Override public void onCancel() {
      entity.setState(DownloadEntity.STATE_CANCEL);
      sendInState2Target(DownloadSchedulers.CANCEL);
      sendIntent(Aria.ACTION_CANCEL, -1);
      entity.deleteData();
    }

    @Override public void onComplete() {
      entity.setState(DownloadEntity.STATE_COMPLETE);
      entity.setComplete(true);
      entity.setSpeed(0);
      sendInState2Target(DownloadSchedulers.COMPLETE);
      sendIntent(Aria.ACTION_COMPLETE, entity.getFileSize());
    }

    @Override public void onFail() {
      entity.setFailNum(entity.getFailNum() + 1);
      entity.setState(DownloadEntity.STATE_FAIL);
      entity.setSpeed(0);
      sendInState2Target(DownloadSchedulers.FAIL);
      sendIntent(Aria.ACTION_FAIL, -1);
    }

    /**
     * 将任务状态发送给下载器
     *
     * @param state {@link DownloadSchedulers#START}
     */
    private void sendInState2Target(int state) {
      if (outHandler.get() != null) {
        outHandler.get().obtainMessage(state, task.get()).sendToTarget();
      }
    }

    private void sendIntent(String action, long location) {
      entity.setComplete(action.equals(Aria.ACTION_COMPLETE));
      entity.setCurrentProgress(location);
      entity.update();
      if (!isOpenBroadCast) return;
      Intent intent = CommonUtil.createIntent(context.getPackageName(), action);
      intent.putExtra(Aria.ENTITY, entity);
      if (location != -1) {
        intent.putExtra(Aria.CURRENT_LOCATION, location);
      }
      context.sendBroadcast(intent);
    }
  }

  public static class Builder {
    private Handler mOutHandler;
    private UploadTaskEntity mTaskEntity;
    private String mTargetName;

    public void setOutHandler(ISchedulers outHandler) {
      mOutHandler = new Handler(outHandler);
    }

    public void setUploadTaskEntity(UploadTaskEntity taskEntity) {
      mTaskEntity = taskEntity;
    }

    public void setTargetName(String targetName) {
      mTargetName = targetName;
    }

    public Builder() {

    }

    public UploadTask build() {
      UploadTask task = new UploadTask(mTaskEntity, mOutHandler);
      task.setTargetName(mTargetName);
      return task;
    }
  }
}
