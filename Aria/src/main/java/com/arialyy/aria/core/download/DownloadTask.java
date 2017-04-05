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
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.ISchedulers;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.Configuration;
import java.lang.ref.WeakReference;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class DownloadTask implements ITask {
  public static final String TAG = "DownloadTask";
  /**
   * 产生该任务对象的hash码
   */
  private String mTargetName;
  private DownloadEntity mEntity;
  private DownloadTaskEntity mTaskEntity;
  private IDownloadListener mListener;
  private Handler mOutHandler;
  private IDownloadUtil mUtil;
  private Context mContext;

  private DownloadTask(DownloadTaskEntity taskEntity, Handler outHandler) {
    mTaskEntity = taskEntity;
    mEntity = taskEntity.downloadEntity;
    mOutHandler = outHandler;
    mContext = AriaManager.APP;
    init();
  }

  private void init() {
    mListener = new DListener(mContext, this, mOutHandler);
    mUtil = new DownloadUtil(mContext, mTaskEntity, mListener);
  }

  /**
   * 获取下载速度
   */
  @Override public long getSpeed() {
    return mEntity.getSpeed();
  }

  /**
   * 获取文件大小
   */
  @Override public long getFileSize() {
    return mEntity.getFileSize();
  }

  /**
   * 获取当前下载进度
   */
  @Override public long getCurrentProgress() {
    return mEntity.getCurrentProgress();
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

  @Override public DownloadEntity getEntity() {
    return mEntity;
  }

  /**
   * 开始下载
   */
  @Override public void start() {
    if (mUtil.isDownloading()) {
      Log.d(TAG, "任务正在下载");
    } else {
      if (mListener == null) {
        mListener = new DListener(mContext, this, mOutHandler);
      }
      mUtil.startDownload();
    }
  }

  public DownloadEntity getDownloadEntity() {
    return mEntity;
  }

  public String getTargetName() {
    return mTargetName;
  }

  @Override public void setTargetName(String targetName) {
    this.mTargetName = targetName;
  }

  /**
   * 停止下载
   */
  @Override public void stop() {
    if (mUtil.isDownloading()) {
      mUtil.stopDownload();
    } else {
      mEntity.setState(DownloadEntity.STATE_STOP);
      mEntity.save();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(DownloadSchedulers.STOP, this).sendToTarget();
      }
      // 发送停止下载的广播
      Intent intent = CommonUtil.createIntent(mContext.getPackageName(), Aria.ACTION_STOP);
      intent.putExtra(Aria.CURRENT_LOCATION, mEntity.getCurrentProgress());
      intent.putExtra(Aria.ENTITY, mEntity);
      mContext.sendBroadcast(intent);
    }
  }

  /**
   * 取消下载
   */
  @Override public void cancel() {
    if (mUtil.isDownloading()) {
      mUtil.cancelDownload();
    } else {
      // 如果任务不是下载状态
      mUtil.cancelDownload();
      mUtil.delConfigFile();
      mUtil.delTempFile();
      mEntity.deleteData();
      if (mOutHandler != null) {
        mOutHandler.obtainMessage(DownloadSchedulers.CANCEL, this).sendToTarget();
      }
      //发送取消下载的广播
      Intent intent = CommonUtil.createIntent(mContext.getPackageName(), Aria.ACTION_CANCEL);
      intent.putExtra(Aria.ENTITY, mEntity);
      mContext.sendBroadcast(intent);
    }
  }

  public static class Builder {
    DownloadTaskEntity taskEntity;
    Handler outHandler;
    int threadNum = 3;
    String targetName;

    public Builder(String targetName, DownloadTaskEntity taskEntity) {
      CheckUtil.checkDownloadTaskEntity(taskEntity.downloadEntity);
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

    /**
     * 设置线程数
     */
    public Builder setThreadNum(int threadNum) {
      this.threadNum = threadNum;
      return this;
    }

    public DownloadTask build() {
      DownloadTask task = new DownloadTask(taskEntity, outHandler);
      task.setTargetName(targetName);
      taskEntity.downloadEntity.save();
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
    Intent sendIntent;
    long INTERVAL = 1024 * 10;   //10k大小的间隔
    long lastLen = 0;   //上一次发送长度
    long lastTime = 0;
    long INTERVAL_TIME = 1000;   //1m更新周期
    boolean isFirst = true;
    DownloadEntity downloadEntity;
    DownloadTask task;

    DListener(Context context, DownloadTask task, Handler outHandler) {
      this.context = context;
      this.outHandler = new WeakReference<>(outHandler);
      this.wTask = new WeakReference<>(task);
      this.task = wTask.get();
      this.downloadEntity = this.task.getDownloadEntity();
      sendIntent = CommonUtil.createIntent(context.getPackageName(), Aria.ACTION_RUNNING);
      sendIntent.putExtra(Aria.ENTITY, downloadEntity);
    }

    @Override public void supportBreakpoint(boolean support) {
      super.supportBreakpoint(support);
      sendInState2Target(ISchedulers.SUPPORT_BREAK_POINT);
      sendIntent(Aria.ACTION_SUPPORT_BREAK_POINT, -1);
    }

    @Override public void onPre() {
      super.onPre();
      downloadEntity.setState(DownloadEntity.STATE_PRE);
      sendIntent(Aria.ACTION_PRE, -1);
    }

    @Override public void onPostPre(long fileSize) {
      super.onPostPre(fileSize);
      downloadEntity.setFileSize(fileSize);
      downloadEntity.setState(DownloadEntity.STATE_POST_PRE);
      sendInState2Target(DownloadSchedulers.PRE);
      sendIntent(Aria.ACTION_POST_PRE, -1);
    }

    @Override public void onResume(long resumeLocation) {
      super.onResume(resumeLocation);
      downloadEntity.setState(DownloadEntity.STATE_RUNNING);
      sendInState2Target(DownloadSchedulers.RESUME);
      sendIntent(Aria.ACTION_RESUME, resumeLocation);
    }

    @Override public void onStart(long startLocation) {
      super.onStart(startLocation);
      downloadEntity.setState(DownloadEntity.STATE_RUNNING);
      sendInState2Target(DownloadSchedulers.START);
      sendIntent(Aria.ACTION_START, startLocation);
    }

    @Override public void onProgress(long currentLocation) {
      super.onProgress(currentLocation);
      if (System.currentTimeMillis() - lastTime > INTERVAL_TIME) {
        long speed = currentLocation - lastLen;
        sendIntent.putExtra(Aria.CURRENT_LOCATION, currentLocation);
        sendIntent.putExtra(Aria.CURRENT_SPEED, speed);
        lastTime = System.currentTimeMillis();
        if (isFirst) {
          downloadEntity.setSpeed(0);
          isFirst = false;
        } else {
          downloadEntity.setSpeed(speed);
        }
        downloadEntity.setCurrentProgress(currentLocation);
        lastLen = currentLocation;
        sendInState2Target(DownloadSchedulers.RUNNING);
        context.sendBroadcast(sendIntent);
      }
    }

    @Override public void onStop(long stopLocation) {
      super.onStop(stopLocation);
      downloadEntity.setState(DownloadEntity.STATE_STOP);
      downloadEntity.setSpeed(0);
      sendInState2Target(DownloadSchedulers.STOP);
      sendIntent(Aria.ACTION_STOP, stopLocation);
    }

    @Override public void onCancel() {
      super.onCancel();
      downloadEntity.setState(DownloadEntity.STATE_CANCEL);
      sendInState2Target(DownloadSchedulers.CANCEL);
      sendIntent(Aria.ACTION_CANCEL, -1);
      downloadEntity.deleteData();
    }

    @Override public void onComplete() {
      super.onComplete();
      downloadEntity.setState(DownloadEntity.STATE_COMPLETE);
      downloadEntity.setDownloadComplete(true);
      downloadEntity.setSpeed(0);
      sendInState2Target(DownloadSchedulers.COMPLETE);
      sendIntent(Aria.ACTION_COMPLETE, downloadEntity.getFileSize());
    }

    @Override public void onFail() {
      super.onFail();
      downloadEntity.setFailNum(downloadEntity.getFailNum() + 1);
      downloadEntity.setState(DownloadEntity.STATE_FAIL);
      downloadEntity.setSpeed(0);
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
        outHandler.get().obtainMessage(state, task).sendToTarget();
      }
    }

    private void sendIntent(String action, long location) {
      downloadEntity.setDownloadComplete(action.equals(Aria.ACTION_COMPLETE));
      downloadEntity.setCurrentProgress(location);
      downloadEntity.update();
      if (!Configuration.isOpenBreadCast) return;
      Intent intent = CommonUtil.createIntent(context.getPackageName(), action);
      intent.putExtra(Aria.ENTITY, downloadEntity);
      if (location != -1) {
        intent.putExtra(Aria.CURRENT_LOCATION, location);
      }
      context.sendBroadcast(intent);
    }
  }
}