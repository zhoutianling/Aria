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

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.queue.pool.ExecutePool;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务队列
 */
public class DownloadTaskQueue
    extends AbsTaskQueue<DownloadTask, DownloadTaskEntity, DownloadEntity> {
  private static final String TAG = "DownloadTaskQueue";
  private static volatile DownloadTaskQueue INSTANCE = null;
  private static final Object LOCK = new Object();

  public static DownloadTaskQueue getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadTaskQueue();
      }
    }
    return INSTANCE;
  }

  private DownloadTaskQueue() {
    mExecutePool = new ExecutePool<>(true);
  }

  @Override public void setTaskHighestPriority(DownloadTask task) {
    task.setHighestPriority(true);
    Map<String, DownloadTask> exeTasks = mExecutePool.getAllTask();
    if (exeTasks != null && !exeTasks.isEmpty()) {
      Set<String> keys = exeTasks.keySet();
      for (String key : keys) {
        DownloadTask temp = exeTasks.get(key);
        if (temp != null && temp.isRunning() && temp.isHighestPriorityTask() && !temp.getKey()
            .equals(task.getKey())) {
          Log.e(TAG, "设置最高优先级任务失败，失败原因【任务中已经有最高优先级任务，请等待上一个最高优先级任务完成，或手动暂停该任务】");
          task.setHighestPriority(false);
          return;
        }
      }
    }
    int maxSize = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMaxTaskNum();
    int currentSize = mExecutePool.size();
    if (currentSize == 0 || currentSize < maxSize) {
      startTask(task);
    } else {
      Set<DownloadTask> tempTasks = new LinkedHashSet<>();
      for (int i = 0; i < maxSize; i++) {
        DownloadTask oldTsk = mExecutePool.pollTask();
        if (oldTsk != null && oldTsk.isRunning()) {
          if (i == maxSize - 1) {
            oldTsk.stop();
            break;
          }
          tempTasks.add(oldTsk);
        }
      }
      startTask(task);

      for (DownloadTask temp : tempTasks){
        mExecutePool.putTask(temp);
      }

      //int i = 0, len = tempTasks.size() - 1;
      //for (DownloadTask oldTask : tempTasks) {
      //  if (i < len) {
      //    startTask(oldTask);
      //  }
      //  i++;
      //}
    }
  }

  @Override public void setMaxTaskNum(int downloadNum) {
    int oldMaxSize = AriaManager.getInstance(AriaManager.APP).getDownloadConfig().oldMaxTaskNum;
    int diff = downloadNum - oldMaxSize;
    if (oldMaxSize == downloadNum) {
      Log.d(TAG, "设置的下载任务数和配置文件的下载任务数一直，跳过");
      return;
    }
    //设置的任务数小于配置任务数
    if (diff <= -1 && mExecutePool.size() >= oldMaxSize) {
      for (int i = 0, len = Math.abs(diff); i < len; i++) {
        DownloadTask eTask = mExecutePool.pollTask();
        if (eTask != null) {
          stopTask(eTask);
        }
      }
    }
    mExecutePool.setDownloadNum(downloadNum);
    if (diff >= 1) {
      for (int i = 0; i < diff; i++) {
        DownloadTask nextTask = getNextTask();
        if (nextTask != null && nextTask.getDownloadEntity().getState() == IEntity.STATE_WAIT) {
          startTask(nextTask);
        }
      }
    }
  }

  @Override public DownloadTask createTask(String target, DownloadTaskEntity entity) {
    DownloadTask task = null;
    if (!TextUtils.isEmpty(target)) {
      task = (DownloadTask) TaskFactory.getInstance()
          .createTask(target, entity, DownloadSchedulers.getInstance());
      mCachePool.putTask(task);
    } else {
      Log.e(TAG, "target name 为 null是！！");
    }
    return task;
  }

  @Override public DownloadTask getTask(DownloadEntity entity) {
    return getTask(entity.getDownloadUrl());
  }

  @Override public void removeTask(DownloadEntity entity) {
    DownloadTask task = mExecutePool.getTask(entity.getDownloadUrl());
    if (task != null) {
      Log.d(TAG, "从执行池删除任务，删除" + (mExecutePool.removeTask(task) ? "成功" : "失败"));
    }
    task = mCachePool.getTask(entity.getDownloadUrl());
    if (task != null) {
      Log.d(TAG, "从缓存池删除任务，删除" + (mCachePool.removeTask(task) ? "成功" : "失败"));
    }
  }
}