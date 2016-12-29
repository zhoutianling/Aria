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

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.core.task.Task;
import com.arialyy.aria.core.queue.pool.ExecutePool;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.util.Configuration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lyy on 2016/8/16.
 * 任务下载器，提供抽象的方法供具体的实现类操作
 */
public class DownloadSchedulers implements IDownloadSchedulers {
  /**
   * 任务预加载
   */
  public static final int PRE = 0;
  /**
   * 任务开始
   */
  public static final int START = 1;
  /**
   * 任务停止
   */
  public static final int STOP = 2;
  /**
   * 任务失败
   */
  public static final int FAIL = 3;
  /**
   * 任务取消
   */
  public static final int CANCEL = 4;
  /**
   * 任务完成
   */
  public static final int COMPLETE = 5;
  /**
   * 下载中
   */
  public static final int RUNNING = 6;
  /**
   * 恢复下载
   */
  public static final int RESUME = 7;
  private static final String TAG = "DownloadSchedulers";
  private static final Object LOCK = new Object();
  private static volatile DownloadSchedulers INSTANCE = null;

  /**
   * 超时时间
   */
  long mTimeOut = 10000;

  /**
   * 下载器任务监听
   */
  Map<String, OnSchedulerListener> mSchedulerListeners = new ConcurrentHashMap<>();
  ITaskQueue mQueue;

  public DownloadSchedulers(ITaskQueue downloadTaskQueue) {
    mQueue = downloadTaskQueue;
  }

  public static DownloadSchedulers getInstance(DownloadTaskQueue queue) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadSchedulers(queue);
      }
    }
    return INSTANCE;
  }

  @Override public boolean handleMessage(Message msg) {
    Task task = (Task) msg.obj;
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
        if (mQueue.size() != Configuration.getInstance().getDownloadNum()) {
          startNextTask(entity);
        }
        break;
      case COMPLETE:
        startNextTask(entity);
        break;
      case FAIL:
        handleFailTask(entity);
        break;
    }
    return true;
  }

  /**
   * 回调
   *
   * @param state 状态
   */
  private void callback(int state, Task task) {
    if (mSchedulerListeners.size() > 0) {
      if (!TextUtils.isEmpty(task.getTargetName())) {
        callback(state, task, mSchedulerListeners.get(task.getTargetName()));
      }
    }
  }

  private void callback(int state, Task task, OnSchedulerListener listener) {
    if (listener != null) {
      //Task task = mQueue.getTask(entity);
      if (task == null) {
        //Log.e(TAG, "队列中没有下载链接【" + entity.getDownloadUrl() + "】的任务");
        Log.e(TAG, "传递的下载任务");
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
      }
    }
  }

  /**
   * 处理下载任务下载失败的情形
   *
   * @param entity 失败实体
   */
  @Override public void handleFailTask(DownloadEntity entity) {
    final Configuration config = Configuration.getInstance();
    if (entity.getFailNum() <= config.getReTryNum()) {
      Task task = mQueue.getTask(entity);
      mQueue.reTryStart(task);
      try {
        Thread.currentThread().sleep(config.getReTryInterval());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      startNextTask(entity);
    }
  }

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   *
   * @param entity 通过Handler传递的下载实体
   */
  @Override public void startNextTask(DownloadEntity entity) {
    mQueue.removeTask(entity);
    Task newTask = mQueue.getNextTask();
    if (newTask == null) {
      Log.w(TAG, "没有下一任务");
      return;
    }
    if (newTask.getDownloadEntity().getState() == DownloadEntity.STATE_WAIT) {
      mQueue.startTask(newTask);
    }
  }

  @Override public void addSchedulerListener(Object target, OnSchedulerListener schedulerListener) {
    if (target == null) {
      throw new IllegalArgumentException("target 不能为null");
    }
    String name = target.getClass().getName();
    if (mSchedulerListeners.get(name) != null) {
      Log.w(TAG, "监听器已存在");
      return;
    }
    mSchedulerListeners.put(name, schedulerListener);
  }

  @Override
  public void removeSchedulerListener(Object target, OnSchedulerListener schedulerListener) {
    if (target == null) {
      throw new IllegalArgumentException("target 不能为null");
    }
    OnSchedulerListener listener = mSchedulerListeners.get(target.getClass().getName());
    mSchedulerListeners.remove(listener);
    listener = null;
  }
}