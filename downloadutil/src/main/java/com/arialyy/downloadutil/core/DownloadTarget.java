package com.arialyy.downloadutil.core;

import android.content.Context;
import android.util.Log;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务调度类
 */
public class DownloadTarget extends IDownloadTarget {
  private static final    String         TAG      = "DownloadTarget";
  private static final    Object         LOCK     = new Object();
  private static volatile DownloadTarget INSTANCE = null;
  private Context mContext;

  public static DownloadTarget getInstance() {
    if (INSTANCE == null) {
      throw new NullPointerException("请在Application中调用init进行注册");
    }
    return INSTANCE;
  }

  static DownloadTarget init(Context context) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadTarget(context.getApplicationContext());
      }
    }
    return INSTANCE;
  }

  private DownloadTarget() {
    super();
  }

  private DownloadTarget(Context context) {
    super();
    mContext = context;
  }

  @Override public void startTask(Task task) {
    if (mExecutePool.putTask(task)) {
      task.start();
    }
  }

  @Override public void stopTask(Task task) {
    if (task.isDownloading()) {
      if (mExecutePool.removeTask(task)) {
        task.stop();
      }
    } else {
      task.stop();
      Log.w(TAG, "停止任务失败，【任务已经停止】");
    }
  }

  @Override public void cancelTask(Task task) {
    if (mExecutePool.removeTask(task) || mCachePool.removeTask(task)) {
      task.cancel();
    }
  }

  @Override public void reTryStart(Task task) {
    if (!task.isDownloading()) {
      task.start();
    } else {
      Log.w(TAG, "任务没有完全停止，重试下载失败");
    }
  }

  @Override public Task createTask(DownloadEntity entity) {
    Task task = TaskFactory.getInstance().createTask(mContext, entity, mTaskHandler);
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

  @Override public int getTaskState(DownloadEntity entity) {
    Task task = getTask(entity);
    if (task == null) {
      Log.e(TAG, "没有找到下载链接为【" + entity.getDownloadUrl() + "】的下载任务");
      return -1;
    }
    return task.getDownloadEntity().getState();
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
}
