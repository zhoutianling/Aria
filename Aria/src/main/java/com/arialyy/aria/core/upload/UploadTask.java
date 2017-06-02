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
 * Created by lyy on 2017/2/23.
 * 上传任务
 */
public class UploadTask implements ITask {
  private static final String TAG = "UploadTask";
  private Handler mOutHandler;
  private UploadEntity mUploadEntity;
  private String mTargetName;

  private UploadUtil mUtil;
  private UListener mListener;
  private boolean isHeighestTask = false;

  private UploadTask(UploadTaskEntity taskEntity, Handler outHandler) {
    mOutHandler = outHandler;
    mUploadEntity = taskEntity.uploadEntity;
    mListener = new UListener(mOutHandler, this);
    mUtil = new UploadUtil(taskEntity, mListener);
  }

  @Override public void setTargetName(String targetName) {
    mTargetName = targetName;
  }

  @Override public void removeRecord() {
    mUploadEntity.deleteData();
  }

  @Override public void setHighestPriority(boolean isHighestPriority) {
    isHeighestTask = isHighestPriority;
  }

  @Override public boolean isHighestPriorityTask() {
    return isHeighestTask;
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
      intent.putExtra(Aria.UPLOAD_ENTITY, mUploadEntity);
      AriaManager.APP.sendBroadcast(intent);
    }
  }

  public String getTargetName() {
    return mTargetName;
  }

  /**
   * @return 返回原始byte速度，需要你在配置文件中配置
   * <pre>
   *  {@code
   *   <xml>
   *    <upload>
   *      ...
   *      <convertSpeed value="false"/>
   *    </upload>
   *
   *    或在代码中设置
   *    Aria.get(this).getUploadConfig().setConvertSpeed(false);
   *   </xml>
   *  }
   * </pre>
   * 才能生效
   */
  @Override public long getSpeed() {
    return mUploadEntity.getSpeed();
  }

  /**
   * @return 返回转换单位后的速度，需要你在配置文件中配置，转换完成后为：1b/s、1k/s、1m/s、1g/s、1t/s
   * <pre>
   *   {@code
   *   <xml>
   *    <upload>
   *      ...
   *      <convertSpeed value="true"/>
   *    </upload>
   *
   *    或在代码中设置
   *    Aria.get(this).getUploadConfig().setConvertSpeed(true);
   *   </xml>
   *   }
   * </pre>
   *
   * 才能生效
   */
  @Override public String getConvertSpeed() {
    return mUploadEntity.getConvertSpeed();
  }

  /**
   * 获取百分比进度
   *
   * @return 返回百分比进度，如果文件长度为0，返回0
   */
  @Override public int getPercent() {
    if (mUploadEntity.getFileSize() == 0) {
      return 0;
    }
    return (int) (mUploadEntity.getCurrentProgress() * 100 / mUploadEntity.getFileSize());
  }

  /**
   * 转换单位后的文件长度
   *
   * @return 如果文件长度为0，则返回0m，否则返回转换后的长度1b、1k、1m、1g、1t
   */
  @Override public String getConvertFileSize() {
    if (mUploadEntity.getFileSize() == 0) {
      return "0m";
    }
    return CommonUtil.formatFileSize(mUploadEntity.getFileSize());
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
    UploadEntity uploadEntity;
    Intent sendIntent;
    boolean isOpenBroadCast = false;
    boolean isConvertSpeed = false;
    Context context;

    UListener(Handler outHandle, UploadTask task) {
      this.outHandler = new WeakReference<>(outHandle);
      this.task = new WeakReference<>(task);
      uploadEntity = this.task.get().getUploadEntity();
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
      uploadEntity.setState(DownloadEntity.STATE_RUNNING);
      sendInState2Target(DownloadSchedulers.RESUME);
      sendIntent(Aria.ACTION_RESUME, resumeLocation);
    }

    @Override public void onStop(long stopLocation) {
      uploadEntity.setState(DownloadEntity.STATE_STOP);
      handleSpeed(0);
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
          speed = 0;
          isFirst = false;
        }
        handleSpeed(speed);
        uploadEntity.setCurrentProgress(currentLocation);
        lastLen = currentLocation;
        sendInState2Target(DownloadSchedulers.RUNNING);
        AriaManager.APP.sendBroadcast(sendIntent);
      }
    }

    @Override public void onCancel() {
      uploadEntity.setState(DownloadEntity.STATE_CANCEL);
      handleSpeed(0);
      sendInState2Target(DownloadSchedulers.CANCEL);
      sendIntent(Aria.ACTION_CANCEL, -1);
      uploadEntity.deleteData();
    }

    @Override public void onComplete() {
      uploadEntity.setState(DownloadEntity.STATE_COMPLETE);
      uploadEntity.setComplete(true);
      handleSpeed(0);
      sendInState2Target(DownloadSchedulers.COMPLETE);
      sendIntent(Aria.ACTION_COMPLETE, uploadEntity.getFileSize());
    }

    @Override public void onFail() {
      uploadEntity.setFailNum(uploadEntity.getFailNum() + 1);
      uploadEntity.setState(DownloadEntity.STATE_FAIL);
      handleSpeed(0);
      sendInState2Target(DownloadSchedulers.FAIL);
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
     * @param state {@link DownloadSchedulers#START}
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
