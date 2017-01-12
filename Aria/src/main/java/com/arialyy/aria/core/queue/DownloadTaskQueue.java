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

package com.arialyy.aria.core.queue;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.core.queue.pool.CachePool;
import com.arialyy.aria.core.queue.pool.ExecutePool;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.IDownloadSchedulers;
import com.arialyy.aria.core.task.Task;
import com.arialyy.aria.core.task.TaskFactory;
import com.arialyy.aria.util.Configuration;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务队列
 */
public class DownloadTaskQueue implements ITaskQueue {
  private static final String TAG = "DownloadTaskQueue";
  private CachePool mCachePool = CachePool.getInstance();
  private ExecutePool mExecutePool = ExecutePool.getInstance();
  private Context mContext;
  //private IDownloadSchedulers mSchedulers;

  private DownloadTaskQueue() {
  }

  private DownloadTaskQueue(Context context) {
    super();
    mContext = context;
  }

  /**
   * 获取任务执行池
   */
  public ExecutePool getExecutePool() {
    return mExecutePool;
  }

  /**
   * 获取缓存池
   */
  public CachePool getCachePool() {
    return mCachePool;
  }

  /**
   * 获取当前运行的任务数
   *
   * @return 当前正在执行的任务数
   */
  public int getCurrentTaskNum() {
    return mExecutePool.size();
  }

  /**
   * 获取缓存任务数
   *
   * @return 获取缓存的任务数
   */
  public int getCacheTaskNum() {
    return mCachePool.size();
  }

  @Override public void startTask(Task task) {
    if (mExecutePool.putTask(task)) {
      mCachePool.removeTask(task);
      task.getDownloadEntity().setFailNum(0);
      task.start();
    }
  }

  @Override public void stopTask(Task task) {
    if (!task.isDownloading()) Log.w(TAG, "停止任务失败，【任务已经停止】");
    if (mExecutePool.removeTask(task)) {
      task.stop();
    } else {
      task.stop();
      Log.w(TAG, "停止任务失败，【任务已经停止】");
    }
  }

  @Override public void cancelTask(Task task) {
    task.cancel();
  }

  @Override public void reTryStart(Task task) {
    if (task == null) {
      Log.w(TAG, "重试下载失败，task 为null");
      return;
    }
    if (!task.isDownloading()) {
      task.start();
    } else {
      Log.w(TAG, "任务没有完全停止，重试下载失败");
    }
  }

  @Override public int size() {
    return mExecutePool.size();
  }

  @Override public void setDownloadNum(int downloadNum) {
    //原始长度
    int size = Configuration.getInstance().getDownloadNum();
    int diff = downloadNum - size;
    if (size == downloadNum) {
      Log.d(TAG, "设置的下载任务数和配置文件的下载任务数一直，跳过");
      return;
    }
    //设置的任务数小于配置任务数
    if (diff <= -1 && mExecutePool.size() >= size) {
      for (int i = 0, len = Math.abs(diff); i < len; i++) {
        Task eTask = mExecutePool.pollTask();
        if (eTask != null) {
          stopTask(eTask);
        }
      }
    }
    mExecutePool.setDownloadNum(downloadNum);
    if (diff >= 1) {
      for (int i = 0; i < diff; i++) {
        Task nextTask = getNextTask();
        if (nextTask != null
            && nextTask.getDownloadEntity().getState() == DownloadEntity.STATE_WAIT) {
          startTask(nextTask);
        }
      }
    }
  }

  @Override public Task createTask(String target, DownloadEntity entity) {
    Task task;
    if (TextUtils.isEmpty(target)) {
      task =
          TaskFactory.getInstance().createTask(mContext, entity, DownloadSchedulers.getInstance());
    } else {
      task = TaskFactory.getInstance()
          .createTask(target, mContext, entity, DownloadSchedulers.getInstance());
    }
    mCachePool.putTask(task);
    return task;
  }

  @Override public Task getTask(DownloadEntity entity) {
    Task task = mExecutePool.getTask(entity.getDownloadUrl());
    if (task == null) {
      task = mCachePool.getTask(entity.getDownloadUrl());
    }
    return task;
  }

  @Override public void removeTask(DownloadEntity entity) {
    Task task = mExecutePool.getTask(entity.getDownloadUrl());
    if (task != null) {
      Log.d(TAG, "从执行池删除任务，删除" + (mExecutePool.removeTask(task) ? "成功" : "失败"));
    } else {
      task = mCachePool.getTask(entity.getDownloadUrl());
    }
    if (task != null) {
      Log.d(TAG, "从缓存池删除任务，删除" + (mCachePool.removeTask(task) ? "成功" : "失败"));
    } else {
      Log.w(TAG, "没有找到下载链接为【" + entity.getDownloadUrl() + "】的任务");
    }
  }

  @Override public Task getNextTask() {
    return mCachePool.pollTask();
  }

  public static class Builder {
    Context context;

    public Builder(Context context) {
      this.context = context.getApplicationContext();
    }

    public DownloadTaskQueue build() {
      DownloadTaskQueue queue = new DownloadTaskQueue(context);
      return queue;
    }
  }
}