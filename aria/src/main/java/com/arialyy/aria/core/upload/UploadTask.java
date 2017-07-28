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
import android.os.Handler;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.AbsNormalTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.core.upload.uploader.SimpleHttpUploadUtil;
import com.arialyy.aria.util.CommonUtil;
import java.lang.ref.WeakReference;

/**
 * Created by lyy on 2017/2/23.
 * 上传任务
 */
public class UploadTask extends AbsNormalTask<UploadEntity> {
  private static final String TAG = "UploadTask";

  private SimpleHttpUploadUtil mUtil;
  private UListener mListener;

  private UploadTask(UploadTaskEntity taskEntity, Handler outHandler) {
    mOutHandler = outHandler;
    mEntity = taskEntity.getEntity();
    mListener = new UListener(mOutHandler, this);
    mUtil = new SimpleHttpUploadUtil(taskEntity, mListener);
  }

  @Override public String getKey() {
    return mEntity.getFilePath();
  }

  @Override public boolean isRunning() {
    return mUtil.isRunning();
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
    if (mUtil.isRunning()) {
      mUtil.stop();
    } else {
      mEntity.setState(IEntity.STATE_STOP);
      mEntity.update();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.STOP, this).sendToTarget();
      }
    }
  }

  @Override public void cancel() {

    if (!mEntity.isComplete()) {
      // 如果任务不是下载状态
      mUtil.cancel();
      mEntity.deleteData();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.CANCEL, this).sendToTarget();
      }
    }
  }

  private static class UListener extends UploadListener {
    WeakReference<Handler> outHandler;
    WeakReference<UploadTask> task;
    long lastLen = 0;   //上一次发送长度
    long lastTime = 0;
    long INTERVAL_TIME = 1000;   //1m更新周期
    boolean isFirst = true;
    UploadEntity entity;
    boolean isConvertSpeed = false;
    Context context;

    UListener(Handler outHandle, UploadTask task) {
      this.outHandler = new WeakReference<>(outHandle);
      this.task = new WeakReference<>(task);
      entity = this.task.get().getEntity();
      context = AriaManager.APP;
      final AriaManager manager = AriaManager.getInstance(context);
      isConvertSpeed = manager.getUploadConfig().isConvertSpeed();
    }

    @Override public void onPre() {
      sendInState2Target(ISchedulers.PRE);
      saveData(IEntity.STATE_PRE, -1);
    }

    @Override public void onStart(long startLocation) {
      sendInState2Target(ISchedulers.START);
      saveData(IEntity.STATE_RUNNING, 0);
    }

    @Override public void onResume(long resumeLocation) {
      sendInState2Target(ISchedulers.RESUME);
      saveData(IEntity.STATE_RUNNING, resumeLocation);
    }

    @Override public void onStop(long stopLocation) {
      handleSpeed(0);
      sendInState2Target(ISchedulers.STOP);
      saveData(IEntity.STATE_STOP, stopLocation);
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

    @Override public void onCancel() {
      handleSpeed(0);
      sendInState2Target(ISchedulers.CANCEL);
      saveData(IEntity.STATE_CANCEL, -1);
      entity.deleteData();
    }

    @Override public void onComplete() {
      entity.setComplete(true);
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
        outHandler.get().obtainMessage(state, task.get()).sendToTarget();
      }
    }

    private void saveData(int state, long location) {
      entity.setState(state);
      entity.setComplete(state == IEntity.STATE_COMPLETE);
      entity.setCurrentProgress(location);
      entity.update();
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
