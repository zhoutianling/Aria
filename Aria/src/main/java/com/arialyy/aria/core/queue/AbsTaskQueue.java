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

import android.util.Log;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.queue.pool.CachePool;
import com.arialyy.aria.core.queue.pool.ExecutePool;
import java.util.Set;

/**
 * Created by lyy on 2017/2/23.
 * 任务队列
 */
abstract class AbsTaskQueue<TASK extends ITask, TASK_ENTITY extends AbsTaskEntity, ENTITY extends AbsEntity>
    implements ITaskQueue<TASK, TASK_ENTITY, ENTITY> {
  private final String TAG = "AbsTaskQueue";
  CachePool<TASK> mCachePool = new CachePool<>();
  ExecutePool<TASK> mExecutePool;

  /**
   * 停止所有任务
   */
  @Override public void stopAllTask() {
    Set<String> exeKeys = mExecutePool.getAllTask().keySet();
    for (String key : exeKeys) {
      TASK task = mExecutePool.getAllTask().get(key);
      if (task != null && task.isRunning()) task.stop();
    }
    Set<String> cacheKeys = mCachePool.getAllTask().keySet();
    for (String key : cacheKeys) {
      mExecutePool.removeTask(key);
    }
  }

  @Override public int getMaxTaskNum() {
    return AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMaxTaskNum();
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
   * 获取缓存任务数
   *
   * @return 获取缓存的任务数
   */
  @Override public int cachePoolSize() {
    return mCachePool.size();
  }

  /**
   * 获取当前运行的任务数
   *
   * @return 当前正在执行的任务数
   */
  @Override public int getExeTaskNum() {
    return mExecutePool.size();
  }

  @Override public void setTaskHighestPriority(TASK task) {

  }

  @Override public TASK getTask(String url) {
    TASK task = mExecutePool.getTask(url);
    if (task == null) {
      task = mCachePool.getTask(url);
    }
    return task;
  }

  @Override public void startTask(TASK task) {
    if (mExecutePool.putTask(task)) {
      mCachePool.removeTask(task);
      task.getEntity().setFailNum(0);
      task.start();
    }
  }

  @Override public void stopTask(TASK task) {
    if (!task.isRunning()) Log.w(TAG, "停止任务失败，【任务已经停止】");
    task.setHighestPriority(false);
    if (mExecutePool.removeTask(task)) {
      task.stop();
    } else {
      task.stop();
      Log.w(TAG, "停止任务失败，【任务已经停止】");
    }
  }

  @Override public void reTryStart(TASK task) {
    if (task == null) {
      Log.w(TAG, "重试下载失败，task 为null");
      return;
    }
    if (!task.isRunning()) {
      task.start();
    } else {
      Log.w(TAG, "任务没有完全停止，重试下载失败");
    }
  }

  @Override public void cancelTask(TASK task) {
    task.cancel();
  }

  @Override public TASK getNextTask() {
    return mCachePool.pollTask();
  }
}
