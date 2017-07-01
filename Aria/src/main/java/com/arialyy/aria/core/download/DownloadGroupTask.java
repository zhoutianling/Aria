///*
// * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.arialyy.aria.core.download;
//
//import android.content.Context;
//import android.os.Handler;
//import android.util.SparseArray;
//import com.arialyy.aria.core.Aria;
//import com.arialyy.aria.core.AriaManager;
//import com.arialyy.aria.core.download.downloader.DownloadListener;
//import com.arialyy.aria.core.download.downloader.DownloadUtil;
//import com.arialyy.aria.core.inf.AbsGroupTask;
//import com.arialyy.aria.core.inf.IEntity;
//import com.arialyy.aria.core.scheduler.DownloadSchedulers;
//import com.arialyy.aria.core.scheduler.ISchedulers;
//import com.arialyy.aria.util.CheckUtil;
//import com.arialyy.aria.util.CommonUtil;
//import java.lang.ref.WeakReference;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Created by AriaL on 2017/6/27.
// * 任务组任务
// */
//public class DownloadGroupTask extends AbsGroupTask<DownloadTaskEntity, DownloadGroupEntity> {
//  DListener mListener;
//  SparseArray<DownloadUtil> mUtils = new SparseArray<>();
//  boolean isStop = false;
//
//  private ExecutorService mFixedThreadPool;
//
//  private DownloadGroupTask(DownloadTaskEntity taskEntity, Handler outHandler) {
//    mTaskEntity = taskEntity;
//    mEntity = taskEntity.groupEntity;
//    mOutHandler = outHandler;
//    mContext = AriaManager.APP;
//    mListener = new DListener(mContext, this, mOutHandler);
//    mFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//  }
//
//  @Override public void start() {
//    List<DownloadEntity> childs = mEntity.getChild();
//    int i = 1;
//    mListener.sendInState2Target(ISchedulers.PRE);
//    for (DownloadEntity entity : childs) {
//      DownloadUtil util = createChildDownload(entity);
//      if (isStop) break;
//      mUtils.append(i, util);
//      mFixedThreadPool.execute(util);
//      i++;
//    }
//  }
//
//  /**
//   * 创建任务组单个任务下载工具
//   */
//  private DownloadUtil createChildDownload(DownloadEntity entity) {
//    DownloadTaskEntity taskEntity = new DownloadTaskEntity(entity);
//    taskEntity.headers = mTaskEntity.headers;
//    taskEntity.requestEnum = mTaskEntity.requestEnum;
//    taskEntity.redirectUrlKey = mTaskEntity.redirectUrlKey;
//    taskEntity.removeFile = mTaskEntity.removeFile;
//    return new DownloadUtil(mContext, taskEntity, mListener);
//  }
//
//  @Override public void stop() {
//    isStop = true;
//    if (mFixedThreadPool != null) {
//      mFixedThreadPool.shutdown();
//    }
//    for (int i = 1, len = mUtils.size(); i <= len; i++) {
//      DownloadUtil util = mUtils.get(i);
//      if (util != null && util.isDownloading()) {
//        util.stopDownload();
//      }
//    }
//  }
//
//  @Override public void cancel() {
//    isStop = true;
//    if (mFixedThreadPool != null) {
//      mFixedThreadPool.shutdown();
//    }
//    for (int i = 1, len = mUtils.size(); i <= len; i++) {
//      DownloadUtil util = mUtils.get(i);
//      if (util != null) {
//        util.cancelDownload();
//      }
//    }
//  }
//
//  /**
//   * 下载监听类
//   */
//  private static class DListener extends DownloadListener {
//    WeakReference<Handler> outHandler;
//    WeakReference<DownloadGroupTask> wTask;
//    Context context;
//    long lastLen = 0;   //上一次发送长度
//    long lastTime = 0;
//    long INTERVAL_TIME = 1000;   //1m更新周期
//    boolean isFirst = true;
//    DownloadGroupEntity groupEntity;
//    DownloadGroupTask task;
//    boolean isConvertSpeed = false;
//    Map<String, DownloadEntity> mEntityMap = new HashMap<>();
//
//    DListener(Context context, DownloadGroupTask task, Handler outHandler) {
//      this.context = context;
//      this.outHandler = new WeakReference<>(outHandler);
//      this.wTask = new WeakReference<>(task);
//      this.task = wTask.get();
//      this.groupEntity = this.task.getEntity();
//      final AriaManager manager = AriaManager.getInstance(context);
//      isConvertSpeed = manager.getDownloadConfig().isConvertSpeed();
//      for (DownloadEntity entity : groupEntity.getChild()) {
//        mEntityMap.put(entity.getDownloadUrl(), entity);
//      }
//    }
//
//    @Override public void onPre(String url) {
//      saveData(url, IEntity.STATE_PRE, -1);
//    }
//
//    @Override public void onPostPre(String url, long fileSize) {
//      DownloadEntity entity = mEntityMap.get(url);
//      if (entity != null){
//        entity.setFileSize(fileSize);
//      }
//      saveData(url, IEntity.STATE_POST_PRE, -1);
//    }
//
//    @Override public void onStart(String url, long startLocation) {
//      downloadEntity.setState(IEntity.STATE_RUNNING);
//      sendInState2Target(ISchedulers.START);
//      sendIntent(Aria.ACTION_START, startLocation);
//    }
//
//    @Override public void onResume(String url, long resumeLocation) {
//      downloadEntity.setState(IEntity.STATE_RUNNING);
//      sendInState2Target(ISchedulers.RESUME);
//      sendIntent(Aria.ACTION_RESUME, resumeLocation);
//    }
//
//    @Override public void onProgress(String url, long currentLocation) {
//      if (System.currentTimeMillis() - lastTime > INTERVAL_TIME) {
//        long speed = currentLocation - lastLen;
//        sendIntent.putExtra(Aria.CURRENT_LOCATION, currentLocation);
//        sendIntent.putExtra(Aria.CURRENT_SPEED, speed);
//        lastTime = System.currentTimeMillis();
//        if (isFirst) {
//          speed = 0;
//          isFirst = false;
//        }
//        handleSpeed(speed);
//        downloadEntity.setCurrentProgress(currentLocation);
//        lastLen = currentLocation;
//        sendInState2Target(ISchedulers.RUNNING);
//        context.sendBroadcast(sendIntent);
//      }
//    }
//
//    @Override public void onStop(String url, long stopLocation) {
//      downloadEntity.setState(task.isWait ? IEntity.STATE_WAIT : IEntity.STATE_STOP);
//      handleSpeed(0);
//      sendInState2Target(ISchedulers.STOP);
//      sendIntent(Aria.ACTION_STOP, stopLocation);
//    }
//
//    @Override public void onCancel(String url) {
//      downloadEntity.setState(IEntity.STATE_CANCEL);
//      handleSpeed(0);
//      sendInState2Target(ISchedulers.CANCEL);
//      sendIntent(Aria.ACTION_CANCEL, -1);
//      downloadEntity.deleteData();
//    }
//
//    @Override public void onComplete(String url) {
//      downloadEntity.setState(IEntity.STATE_COMPLETE);
//      downloadEntity.setDownloadComplete(true);
//      handleSpeed(0);
//      sendInState2Target(ISchedulers.COMPLETE);
//      sendIntent(Aria.ACTION_COMPLETE, downloadEntity.getFileSize());
//    }
//
//    @Override public void onFail(String url) {
//      downloadEntity.setFailNum(downloadEntity.getFailNum() + 1);
//      downloadEntity.setState(IEntity.STATE_FAIL);
//      handleSpeed(0);
//      sendInState2Target(ISchedulers.FAIL);
//      sendIntent(Aria.ACTION_FAIL, -1);
//    }
//
//    private void handleSpeed(long speed) {
//      if (isConvertSpeed) {
//        downloadEntity.setConvertSpeed(CommonUtil.formatFileSize(speed) + "/s");
//      } else {
//        downloadEntity.setSpeed(speed);
//      }
//    }
//
//    /**
//     * 将任务状态发送给下载器
//     *
//     * @param state {@link DownloadSchedulers#START}
//     */
//    private void sendInState2Target(int state) {
//      if (outHandler.get() != null) {
//        outHandler.get().obtainMessage(state, task).sendToTarget();
//      }
//    }
//
//    private void saveData(String url, int state, long location) {
//      DownloadEntity child = mEntityMap.get(url);
//      if (child != null) {
//        child.setState(state);
//        child.setDownloadComplete(state == IEntity.STATE_COMPLETE);
//        child.setCurrentProgress(location);
//        child.update();
//      }
//    }
//  }
//
//  public static class Builder {
//    DownloadTaskEntity taskEntity;
//    Handler outHandler;
//    String targetName;
//
//    public Builder(String targetName, DownloadTaskEntity taskEntity) {
//      CheckUtil.checkTaskEntity(taskEntity);
//      this.targetName = targetName;
//      this.taskEntity = taskEntity;
//    }
//
//    /**
//     * 设置自定义Handler处理下载状态时间
//     *
//     * @param schedulers {@link ISchedulers}
//     */
//    public DownloadGroupTask.Builder setOutHandler(ISchedulers schedulers) {
//      this.outHandler = new Handler(schedulers);
//      return this;
//    }
//
//    public DownloadGroupTask build() {
//      DownloadGroupTask task = new DownloadGroupTask(taskEntity, outHandler);
//      task.setTargetName(targetName);
//      taskEntity.downloadEntity.save();
//      return task;
//    }
//  }
//}
