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
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.core.upload.UploadTask;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by AriaL on 2017/6/4.
 */
public abstract class AbsSchedulers<TASK_ENTITY extends AbsTaskEntity, ENTITY extends AbsEntity, TASK extends ITask<ENTITY>, QUEUE extends ITaskQueue<TASK, TASK_ENTITY, ENTITY>>
    implements ISchedulers<TASK> {
  private static final String TAG = "AbsSchedulers";

  protected QUEUE mQueue;

  private Map<String, ISchedulerListener<TASK>> mSchedulerListeners = new ConcurrentHashMap<>();

  @Override
  public void addSchedulerListener(String targetName, ISchedulerListener<TASK> schedulerListener) {
    mSchedulerListeners.put(targetName, schedulerListener);
  }

  @Override public void removeSchedulerListener(String targetName,
      ISchedulerListener<TASK> schedulerListener) {
    //该内存泄露解决方案：http://stackoverflow.com/questions/14585829/how-safe-is-to-delete-already-removed-concurrenthashmap-element
    for (Iterator<Map.Entry<String, ISchedulerListener<TASK>>> iter =
        mSchedulerListeners.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<String, ISchedulerListener<TASK>> entry = iter.next();
      if (entry.getKey().equals(targetName)) iter.remove();
    }
  }

  @Override public boolean handleMessage(Message msg) {
    TASK task = (TASK) msg.obj;
    if (task == null) {
      Log.e(TAG, "请传入下载任务");
      return true;
    }
    callback(msg.what, task);
    ENTITY entity = task.getEntity();
    switch (msg.what) {
      case STOP:
        if (task.getEntity().getState() == IEntity.STATE_WAIT) {
          break;
        }
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

  /**
   * 回调
   *
   * @param state 状态
   */
  private void callback(int state, TASK task) {
    if (mSchedulerListeners.size() > 0) {
      Set<String> keys = mSchedulerListeners.keySet();
      for (String key : keys) {
        callback(state, task, mSchedulerListeners.get(key));
      }
    }
  }

  private void callback(int state, TASK task, ISchedulerListener<TASK> listener) {
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
        case SUPPORT_BREAK_POINT:
          if (listener instanceof IDownloadSchedulerListener) {
            ((IDownloadSchedulerListener<TASK>) listener).onNoSupportBreakPoint(task);
          }
          break;
      }
    }
  }

  /**
   * 处理下载任务下载失败的情形
   *
   * @param task 下载任务
   */
  private void handleFailTask(final TASK task) {
    long interval = 2000;
    int num = 10;
    if (task instanceof DownloadTask) {
      interval = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getReTryInterval();
      num = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getReTryNum();
    } else if (task instanceof UploadTask) {
      interval = AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryInterval();
      num = AriaManager.getInstance(AriaManager.APP).getUploadConfig().getReTryNum();
    }

    final int reTryNum = num;
    CountDownTimer timer = new CountDownTimer(interval, 1000) {
      @Override public void onTick(long millisUntilFinished) {

      }

      @Override public void onFinish() {
        ENTITY entity = task.getEntity();
        if (entity.getFailNum() < reTryNum) {
          TASK task = mQueue.getTask(entity);
          mQueue.reTryStart(task);
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
    TASK newTask = mQueue.getNextTask();
    if (newTask == null) {
      Log.w(TAG, "没有下一任务");
      return;
    }
    if (newTask.getEntity().getState() == IEntity.STATE_WAIT) {
      mQueue.startTask(newTask);
    }
  }
}
