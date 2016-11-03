package com.arialyy.downloadutil.core.queue;

import android.content.Context;
import android.util.Log;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.scheduler.DownloadSchedulers;
import com.arialyy.downloadutil.core.task.Task;
import com.arialyy.downloadutil.core.task.TaskFactory;
import com.arialyy.downloadutil.core.scheduler.IDownloadSchedulers;
import com.arialyy.downloadutil.core.queue.pool.CachePool;
import com.arialyy.downloadutil.core.queue.pool.ExecutePool;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务队列
 */
public class DownloadTaskQueue implements ITaskQueue {
  private static final String      TAG          = "DownloadTaskQueue";
  private              CachePool   mCachePool   = CachePool.getInstance();
  private              ExecutePool mExecutePool = ExecutePool.getInstance();
  private Context             mContext;
  private IDownloadSchedulers mSchedulers;

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
    Task task = TaskFactory.getInstance().createTask(mContext, entity, mSchedulers);
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

  @Override public void setScheduler(IDownloadSchedulers schedulers) {
    mSchedulers = schedulers;
  }

  public static class Builder {
    Context             context;
    IDownloadSchedulers schedulers;

    public Builder(Context context) {
      this.context = context.getApplicationContext();
    }

    public Builder setDownloadSchedulers(IDownloadSchedulers schedulers) {
      this.schedulers = schedulers;
      return this;
    }

    public DownloadTaskQueue build() {
      DownloadTaskQueue queue = new DownloadTaskQueue(context);
      if (schedulers == null) {
        schedulers = DownloadSchedulers.getInstance(queue);
      }
      queue.setScheduler(schedulers);
      return queue;
    }
  }
}
