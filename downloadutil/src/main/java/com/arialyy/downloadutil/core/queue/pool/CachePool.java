package com.arialyy.downloadutil.core.queue.pool;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.downloadutil.core.task.Task;
import com.arialyy.downloadutil.core.queue.IPool;
import com.arialyy.downloadutil.util.CommonUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by lyy on 2016/8/14.
 * 任务缓存池，所有下载任务最先缓存在这个池中
 */
public class CachePool implements IPool {
  private static final    String    TAG      = "CachePool";
  private static final    Object    LOCK     = new Object();
  private static final    int       MAX_NUM  = Integer.MAX_VALUE;  //最大下载任务数
  private static volatile CachePool INSTANCE = null;
  private Map<String, Task>         mCacheArray;
  private LinkedBlockingQueue<Task> mCacheQueue;

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

  @Override public boolean putTask(Task task) {
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

  @Override public Task pollTask() {
    synchronized (LOCK) {
      Task task = mCacheQueue.poll();
      if (task != null) {
        String url = task.getDownloadEntity().getDownloadUrl();
        mCacheArray.remove(CommonUtil.keyToHashKey(url));
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
      return mCacheArray.get(key);
    }
  }

  @Override public boolean removeTask(Task task) {
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
      Task   task = mCacheArray.get(key);
      mCacheArray.remove(key);
      return mCacheQueue.remove(task);
    }
  }

  @Override public int size() {
    return mCacheQueue.size();
  }
}
