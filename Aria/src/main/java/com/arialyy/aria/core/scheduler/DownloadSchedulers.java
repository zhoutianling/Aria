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

package com.arialyy.aria.core.scheduler;

import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lyy on 2016/8/16.
 * 任务下载器，提供抽象的方法供具体的实现类操作
 */
public class DownloadSchedulers implements ISchedulers<DownloadTask> {

  private static final String TAG = "DownloadSchedulers";
  private static final Object LOCK = new Object();
  private static volatile DownloadSchedulers INSTANCE = null;

  /**
   * 下载器任务监听
   */
  private Map<String, IDownloadSchedulerListener<DownloadTask>> mSchedulerListeners =
      new ConcurrentHashMap<>();
  private DownloadTaskQueue mQueue;

  private DownloadSchedulers() {
    mQueue = DownloadTaskQueue.getInstance();
  }

  public static DownloadSchedulers getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadSchedulers();
      }
    }
    return INSTANCE;
  }

  @Override public void addSchedulerListener(String targetName,
      ISchedulerListener<DownloadTask> schedulerListener) {
    mSchedulerListeners.put(targetName,
        (IDownloadSchedulerListener<DownloadTask>) schedulerListener);
  }

  @Override public void removeSchedulerListener(String targetName,
      ISchedulerListener<DownloadTask> schedulerListener) {
    //该内存溢出解决方案：http://stackoverflow.com/questions/14585829/how-safe-is-to-delete-already-removed-concurrenthashmap-element
    for (Iterator<Map.Entry<String, IDownloadSchedulerListener<DownloadTask>>> iter =
        mSchedulerListeners.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<String, IDownloadSchedulerListener<DownloadTask>> entry = iter.next();
      if (entry.getKey().equals(targetName)) iter.remove();
    }
  }

  @Override public boolean handleMessage(Message msg) {
    DownloadTask task = (DownloadTask) msg.obj;
    if (task == null) {
      Log.e(TAG, "请传入下载任务");
      return true;
    }
    callback(msg.what, task);
    DownloadEntity entity = task.getDownloadEntity();
    switch (msg.what) {
      case STOP:
      case CANCEL:
        mQueue.removeTask(entity);
        if (mQueue.size() < AriaManager.getInstance(AriaManager.APP)
            .getUploadConfig()
            .getMaxTaskNum()) {
          startNextTask();
        }
        break;
      case COMPLETE:
        mQueue.removeTask(entity);
        startNextTask();
        break;
      case FAIL:
        handleFailTask(task);
        break;
    }
    return true;
  }

  /**
   * 回调
   *
   * @param state 状态
   */
  private void callback(int state, DownloadTask task) {
    if (mSchedulerListeners.size() > 0) {
      //if (!TextUtils.isEmpty(task.getTargetName())) {
      //  callback(state, task, mSchedulerListeners.get(task.getTargetName()));
      //}
      Set<String> keys = mSchedulerListeners.keySet();
      for (String key : keys) {
        callback(state, task, mSchedulerListeners.get(key));
      }
    }
  }

  private void callback(int state, DownloadTask task,
      IDownloadSchedulerListener<DownloadTask> listener) {
    if (listener != null) {
      if (task == null) {
        Log.e(TAG, "TASK 为null，回调失败");
        return;
      }
      switch (state) {
        case RUNNING:
          listener.onTaskRunning(task);
          break;
        case START:
          listener.onTaskStart(task);
          break;
        case STOP:
          listener.onTaskStop(task);
          break;
        case RESUME:
          listener.onTaskResume(task);
          break;
        case PRE:
          listener.onTaskPre(task);
          break;
        case CANCEL:
          listener.onTaskCancel(task);
          break;
        case COMPLETE:
          listener.onTaskComplete(task);
          break;
        case FAIL:
          listener.onTaskFail(task);
          break;
        case SUPPORT_BREAK_POINT:
          listener.onNoSupportBreakPoint(task);
          break;
      }
    }
  }

  /**
   * 处理下载任务下载失败的情形
   *
   * @param task 下载任务
   */
  private void handleFailTask(final DownloadTask task) {
    final long interval =
        AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryInterval();
    final int reTryNum = AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryNum();

    CountDownTimer timer = new CountDownTimer(interval, 1000) {
      @Override public void onTick(long millisUntilFinished) {

      }

      @Override public void onFinish() {
        DownloadEntity entity = task.getDownloadEntity();
        if (entity.getFailNum() < reTryNum) {
          DownloadTask task = mQueue.getTask(entity);
          mQueue.reTryStart(task);
          try {
            Thread.sleep(interval);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } else {
          mQueue.removeTask(entity);
          startNextTask();
        }
      }
    };
    timer.start();
  }

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   */
  private void startNextTask() {
    DownloadTask newTask = mQueue.getNextTask();
    if (newTask == null) {
      Log.w(TAG, "没有下一任务");
      return;
    }
    if (newTask.getDownloadEntity().getState() == DownloadEntity.STATE_WAIT) {
      mQueue.startTask(newTask);
    }
  }
}
