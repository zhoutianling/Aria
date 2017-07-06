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
import com.arialyy.aria.core.inf.AbsTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.util.CommonUtil;
import java.lang.ref.WeakReference;

/**
 * Created by lyy on 2017/2/23.
 * 上传任务
 */
public class UploadTask extends AbsTask<UploadTaskEntity, UploadEntity> {
  private static final String TAG = "UploadTask";
  private Handler mOutHandler;

  private UploadUtil mUtil;
  private UListener mListener;

  private UploadTask(UploadTaskEntity taskEntity, Handler outHandler) {
    mOutHandler = outHandler;
    mEntity = taskEntity.uploadEntity;
    mListener = new UListener(mOutHandler, this);
    mUtil = new UploadUtil(taskEntity, mListener);
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

  }

  @Override public void cancel() {

    if (!mEntity.isComplete()) {
      // 如果任务不是下载状态
      mUtil.cancel();
      mEntity.deleteData();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(ISchedulers.CANCEL, this).sendToTarget();
      }
      //发送取消下载的广播
      Intent intent = CommonUtil.createIntent(AriaManager.APP.getPackageName(), Aria.ACTION_CANCEL);
      intent.putExtra(Aria.UPLOAD_ENTITY, mEntity);
      AriaManager.APP.sendBroadcast(intent);
    }
  }

  private static class UListener extends UploadListener {
    WeakReference<Handler> outHandler;
    WeakReference<UploadTask> task;
    long lastLen = 0;   //上一次发送长度
    long lastTime = 0;
    long INTERVAL_TIME = 1000;   //1m更新周期
    boolean isFirst = true;
    UploadEntity uploadEntity;
    Intent sendIntent;
    boolean isOpenBroadCast = false;
    boolean isConvertSpeed = false;
    Context context;

    UListener(Handler outHandle, UploadTask task) {
      this.outHandler = new WeakReference<>(outHandle);
      this.task = new WeakReference<>(task);
      uploadEntity = this.task.get().getEntity();
      sendIntent = CommonUtil.createIntent(AriaManager.APP.getPackageName(), Aria.ACTION_RUNNING);
      sendIntent.putExtra(Aria.UPLOAD_ENTITY, uploadEntity);
      context = AriaManager.APP;
      final AriaManager manager = AriaManager.getInstance(context);
      isOpenBroadCast = manager.getUploadConfig().isOpenBreadCast();
      isConvertSpeed = manager.getUploadConfig().isConvertSpeed();
    }

    @Override public void onPre() {
      uploadEntity.setState(IEntity.STATE_PRE);
      sendIntent(Aria.ACTION_PRE, -1);
      sendInState2Target(ISchedulers.PRE);
    }

    @Override public void onPostPre(long fileSize) {
      super.onPostPre(fileSize);
      uploadEntity.setFileSize(fileSize);
      uploadEntity.setState(IEntity.STATE_POST_PRE);
      sendIntent(Aria.ACTION_POST_PRE, 0);
      sendInState2Target(ISchedulers.POST_PRE);
    }

    @Override public void onStart() {
      uploadEntity.setState(IEntity.STATE_RUNNING);
      sendIntent(Aria.ACTION_START, 0);
      sendInState2Target(ISchedulers.START);
    }

    @Override public void onResume(long resumeLocation) {
      uploadEntity.setState(IEntity.STATE_RUNNING);
      sendInState2Target(ISchedulers.RESUME);
      sendIntent(Aria.ACTION_RESUME, resumeLocation);
    }

    @Override public void onStop(long stopLocation) {
      uploadEntity.setState(IEntity.STATE_STOP);
      handleSpeed(0);
      sendInState2Target(ISchedulers.STOP);
      sendIntent(Aria.ACTION_STOP, stopLocation);
    }

    @Override public void onProgress(long currentLocation) {
      if (System.currentTimeMillis() - lastTime > INTERVAL_TIME) {
        long speed = currentLocation - lastLen;
        sendIntent.putExtra(Aria.CURRENT_LOCATION, currentLocation);
        sendIntent.putExtra(Aria.CURRENT_SPEED, speed);
        lastTime = System.currentTimeMillis();
        if (isFirst) {
          speed = 0;
          isFirst = false;
        }
        handleSpeed(speed);
        uploadEntity.setCurrentProgress(currentLocation);
        lastLen = currentLocation;
        sendInState2Target(ISchedulers.RUNNING);
        AriaManager.APP.sendBroadcast(sendIntent);
      }
    }

    @Override public void onCancel() {
      uploadEntity.setState(IEntity.STATE_CANCEL);
      handleSpeed(0);
      sendInState2Target(ISchedulers.CANCEL);
      sendIntent(Aria.ACTION_CANCEL, -1);
      uploadEntity.deleteData();
    }

    @Override public void onComplete() {
      uploadEntity.setState(IEntity.STATE_COMPLETE);
      uploadEntity.setComplete(true);
      handleSpeed(0);
      sendInState2Target(ISchedulers.COMPLETE);
      sendIntent(Aria.ACTION_COMPLETE, uploadEntity.getFileSize());
    }

    @Override public void onFail() {
      uploadEntity.setFailNum(uploadEntity.getFailNum() + 1);
      uploadEntity.setState(IEntity.STATE_FAIL);
      handleSpeed(0);
      sendInState2Target(ISchedulers.FAIL);
      sendIntent(Aria.ACTION_FAIL, -1);
    }

    private void handleSpeed(long speed) {
      if (isConvertSpeed) {
        uploadEntity.setConvertSpeed(CommonUtil.formatFileSize(speed) + "/s");
      } else {
        uploadEntity.setSpeed(speed);
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

    private void sendIntent(String action, long location) {
      uploadEntity.setComplete(action.equals(Aria.ACTION_COMPLETE));
      uploadEntity.setCurrentProgress(location);
      uploadEntity.update();
      if (!isOpenBroadCast) return;
      Intent intent = CommonUtil.createIntent(context.getPackageName(), action);
      intent.putExtra(Aria.UPLOAD_ENTITY, uploadEntity);
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
