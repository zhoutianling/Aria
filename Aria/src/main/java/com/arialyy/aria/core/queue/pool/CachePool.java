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
import com.arialyy.aria.core.queue.IPool;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyy on 2016/8/14.
 * 任务缓存池，所有下载任务最先缓存在这个池中
 */
public class CachePool implements IPool {
  private static final    String    TAG      = "CachePool";
  private static final    Object    LOCK     = new Object();
  private static final    int       MAX_NUM  = Integer.MAX_VALUE;  //最大下载任务数
  private static volatile CachePool INSTANCE = null;
  private static final    long      TIME_OUT = 1000;
  private Map<String, DownloadTask>         mCacheArray;
  private LinkedBlockingQueue<DownloadTask> mCacheQueue;

  private CachePool() {
    mCacheQueue = new LinkedBlockingQueue<>(MAX_NUM);
    mCacheArray = new HashMap<>();
  }

  public static CachePool getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new CachePool();
      }
    }
    return INSTANCE;
  }

  @Override public boolean putTask(DownloadTask task) {
    synchronized (LOCK) {
      if (task == null) {
        Log.e(TAG, "下载任务不能为空！！");
        return false;
      }
      String url = task.getDownloadEntity().getDownloadUrl();
      if (mCacheQueue.contains(task)) {
        Log.w(TAG, "队列中已经包含了该任务，任务下载链接【" + url + "】");
        return false;
      } else {
        boolean s = mCacheQueue.offer(task);
        Log.d(TAG, "任务添加" + (s ? "成功" : "失败，【" + url + "】"));
        if (s) {
          mCacheArray.put(CommonUtil.keyToHashKey(url), task);
        }
        return s;
      }
    }
  }

  @Override public DownloadTask pollTask() {
    synchronized (LOCK) {
      try {
        DownloadTask task = null;
        task = mCacheQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
        if (task != null) {
          String url = task.getDownloadEntity().getDownloadUrl();
          mCacheArray.remove(CommonUtil.keyToHashKey(url));
        }
        return task;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override public DownloadTask getTask(String downloadUrl) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(downloadUrl)) {
        Log.e(TAG, "请传入有效的下载链接");
        return null;
      }
      String key = CommonUtil.keyToHashKey(downloadUrl);
      return mCacheArray.get(key);
    }
  }

  @Override public boolean removeTask(DownloadTask task) {
    synchronized (LOCK) {
      if (task == null) {
        Log.e(TAG, "任务不能为空");
        return false;
      } else {
        String key = CommonUtil.keyToHashKey(task.getDownloadEntity().getDownloadUrl());
        mCacheArray.remove(key);
        return mCacheQueue.remove(task);
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
      DownloadTask task = mCacheArray.get(key);
      mCacheArray.remove(key);
      return mCacheQueue.remove(task);
    }
  }

  @Override public int size() {
    return mCacheQueue.size();
  }
}