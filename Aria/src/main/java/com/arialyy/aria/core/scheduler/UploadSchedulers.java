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
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.queue.UploadTaskQueue;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTask;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lyy on 2017/2/27.
 * 上传任务调度器
 */
public class UploadSchedulers implements ISchedulers<UploadTask> {
  private static final String TAG = "UploadSchedulers";
  private static final Object LOCK = new Object();
  private static volatile UploadSchedulers INSTANCE = null;
  private Map<String, ISchedulerListener<UploadTask>> mSchedulerListeners =
      new ConcurrentHashMap<>();
  private UploadTaskQueue mQueue;

  private UploadSchedulers() {
    mQueue = UploadTaskQueue.getInstance();
  }

  public static UploadSchedulers getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new UploadSchedulers();
      }
    }

    return INSTANCE;
  }

  @Override public void addSchedulerListener(String targetName,
      ISchedulerListener<UploadTask> schedulerListener) {
    mSchedulerListeners.put(targetName, schedulerListener);
  }

  @Override public void removeSchedulerListener(String targetName,
      ISchedulerListener<UploadTask> schedulerListener) {
    for (Iterator<Map.Entry<String, ISchedulerListener<UploadTask>>> iter =
        mSchedulerListeners.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<String, ISchedulerListener<UploadTask>> entry = iter.next();
      if (entry.getKey().equals(targetName)) iter.remove();
    }
  }

  private void handleFailTask(final UploadTask task) {
    final long interval =
        AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryInterval();
    final int reTryNum = AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryNum();
    CountDownTimer timer = new CountDownTimer(interval, 1000) {
      @Override public void onTick(long millisUntilFinished) {

      }

      @Override public void onFinish() {
        UploadEntity entity = task.getUploadEntity();
        if (entity.getFailNum() <= reTryNum) {
          UploadTask task = mQueue.getTask(entity);
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

  private void startNextTask() {
    UploadTask newTask = mQueue.getNextTask();
    if (newTask == null) {
      Log.w(TAG, "没有下一任务");
      return;
    }
    if (newTask.getUploadEntity().getState() == IEntity.STATE_WAIT) {
      mQueue.startTask(newTask);
    }
  }

  /**
   * 回调
   *
   * @param state 状态
   */
  private void callback(int state, UploadTask task) {
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

  private void callback(int state, UploadTask task, ISchedulerListener<UploadTask> listener) {
    if (listener != null) {
      if (task == null) {
        Log.e(TAG, "TASK 为null，回调失败");
        return;
      }
      switch (state) {
        case PRE:
          listener.onPre(task);
          break;
        case POST_PRE:
          listener.onTaskPre(task);
          break;
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
        case CANCEL:
          listener.onTaskCancel(task);
          break;
        case COMPLETE:
          listener.onTaskComplete(task);
          break;
        case FAIL:
          listener.onTaskFail(task);
          break;
      }
    }
  }

  @Override public boolean handleMessage(Message msg) {
    UploadTask task = (UploadTask) msg.obj;
    if (task == null) {
      Log.e(TAG, "请传入上传任务");
      return true;
    }
    callback(msg.what, task);
    UploadEntity entity = task.getUploadEntity();
    switch (msg.what) {
      case STOP:
      case CANCEL:
        mQueue.removeTask(entity);
        if (mQueue.executePoolSize() < AriaManager.getInstance(AriaManager.APP)
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
}
