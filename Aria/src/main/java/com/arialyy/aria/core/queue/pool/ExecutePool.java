/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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

package com.arialyy.aria.core.queue.pool;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.core.queue.IPool;
import com.arialyy.aria.core.task.Task;
import com.arialyy.aria.util.Configuration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyy on 2016/8/15.
 * 任务执行池，所有当前下载任务都该任务池中，默认下载大小为2
 */
public class ExecutePool implements IPool {
  private static final    String      TAG      = "ExecutePool";
  private static final    Object      LOCK     = new Object();
  private static final    long        TIME_OUT = 1000;
  private static volatile ExecutePool INSTANCE = null;
  private ArrayBlockingQueue<Task> mExecuteQueue;
  private Map<String, Task>        mExecuteArray;
  private int                      mSize;

  private ExecutePool() {
    mSize = Configuration.getInstance().getDownloadNum();
    mExecuteQueue = new ArrayBlockingQueue<>(mSize);
    mExecuteArray = new HashMap<>();
  }

  public static ExecutePool getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new ExecutePool();
      }
    }
    return INSTANCE;
  }

  @Override public boolean putTask(Task task) {
    synchronized (LOCK) {
      if (task == null) {
        Log.e(TAG, "下载任务不能为空！！");
        return false;
      }
      String url = task.getDownloadEntity().getDownloadUrl();
      if (mExecuteQueue.contains(task)) {
        Log.e(TAG, "队列中已经包含了该任务，任务下载链接【" + url + "】");
        return false;
      } else {
        if (mExecuteQueue.size() >= mSize) {
          if (pollFirstTask()) {
            return putNewTask(task);
          }
        } else {
          return putNewTask(task);
        }
      }
    }
    return false;
  }

  /**
   * 设置执行任务数
   *
   * @param downloadNum 下载数
   */
  public void setDownloadNum(int downloadNum) {
    try {
      ArrayBlockingQueue<Task> temp = new ArrayBlockingQueue<>(downloadNum);
      Task                     task;
      while ((task = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS)) != null) {
        temp.offer(task);
      }
      mExecuteQueue = temp;
      mSize = downloadNum;
      Configuration.getInstance().setDownloadNum(mSize);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * 添加新任务
   *
   * @param newTask 新下载任务
   */
  private boolean putNewTask(Task newTask) {
    String  url = newTask.getDownloadEntity().getDownloadUrl();
    boolean s   = mExecuteQueue.offer(newTask);
    Log.w(TAG, "任务添加" + (s ? "成功" : "失败，【" + url + "】"));
    if (s) {
      newTask.start();
      mExecuteArray.put(CommonUtil.keyToHashKey(url), newTask);
    }
    return s;
  }

  /**
   * 队列满时，将移除下载队列中的第一个任务
   */
  private boolean pollFirstTask() {
    try {
      Task oldTask = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
      if (oldTask == null) {
        Log.e(TAG, "移除任务失败");
        return false;
      }
      oldTask.stop();
      //            wait(200);
      String key = CommonUtil.keyToHashKey(oldTask.getDownloadEntity().getDownloadUrl());
      mExecuteArray.remove(key);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override public Task pollTask() {
    synchronized (LOCK) {
      Task task = mExecuteQueue.poll();
      if (task != null) {
        String url = task.getDownloadEntity().getDownloadUrl();
        mExecuteArray.remove(CommonUtil.keyToHashKey(url));
      }
      return task;
    }
  }

  @Override public Task getTask(String downloadUrl) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(downloadUrl)) {
        Log.e(TAG, "请传入有效的下载链接");
        return null;
      }
      String key = CommonUtil.keyToHashKey(downloadUrl);
      return mExecuteArray.get(key);
    }
  }

  @Override public boolean removeTask(Task task) {
    synchronized (LOCK) {
      if (task == null) {
        Log.e(TAG, "任务不能为空");
        return false;
      } else {
        String key = CommonUtil.keyToHashKey(task.getDownloadEntity().getDownloadUrl());
        mExecuteArray.remove(key);
        return mExecuteQueue.remove(task);
      }
    }
  }

  @Override public boolean removeTask(String downloadUrl) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(downloadUrl)) {
        Log.e(TAG, "请传入有效的下载链接");
        return false;
      }
      String key  = CommonUtil.keyToHashKey(downloadUrl);
      Task   task = mExecuteArray.get(key);
      mExecuteArray.remove(key);
      return mExecuteQueue.remove(task);
    }
  }

  @Override public int size() {
    return mExecuteQueue.size();
  }
}