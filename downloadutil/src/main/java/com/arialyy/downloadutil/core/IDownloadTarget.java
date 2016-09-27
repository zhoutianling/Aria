package com.arialyy.downloadutil.core;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.arialyy.downloadutil.core.inf.IDownloader;
import com.arialyy.downloadutil.core.inf.ITask;
import com.arialyy.downloadutil.core.pool.CachePool;
import com.arialyy.downloadutil.core.pool.ExecutePool;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/8/16.
 * 任务下载器，提供抽象的方法供具体的实现类操作
 */
public abstract class IDownloadTarget implements IDownloader, ITask {
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

  protected CachePool mCachePool = CachePool.getInstance();
  protected ExecutePool mExecutePool = ExecutePool.getInstance();
  protected AutoTaskHandler mTaskHandler;
  /**
   * 下载失败次数
   */
  protected int mFailNum = 10;

  /**
   * 超时时间
   */
  protected long mTimeOut = 10000;

  /**
   * 下载器任务监听
   */
  protected OnTargetListener mTargetListener;

  /**
   * Target处理任务监听
   */
  public interface OnTargetListener {
    /**
     * 任务开始
     */
    public void onTaskStart(Task task);

    /**
     * 任务停止
     */
    public void onTaskStop(Task task);

    /**
     * 任务取消
     */
    public void onTaskCancel(Task task);

    /**
     * 任务下载失败
     */
    public void onTaskFail(Task task);

    /**
     * 任务完成
     */
    public void onTaskComplete(Task task);
  }

  protected IDownloadTarget() {
    mTaskHandler = new AutoTaskHandler(this);
  }

  /**
   * 设置下载器监听
   *
   * @param targetListener {@link OnTargetListener}
   */
  public void setOnTargetListener(OnTargetListener targetListener) {
    this.mTargetListener = targetListener;
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

  public void setFailNum(int mFailNum) {
    this.mFailNum = mFailNum;
  }

  public void setTimeOut(long timeOut) {
    this.mTimeOut = timeOut;
  }

  /**
   * 自动处理任务停止，下载失败，取消下载，自动下载下一个任务的操作
   */
  private static class AutoTaskHandler extends Handler {
    private static final String TAG = "AutoTaskHandler";
    IDownloadTarget target;

    public AutoTaskHandler(IDownloadTarget target) {
      this.target = target;
    }

    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      DownloadEntity entity = (DownloadEntity) msg.obj;
      if (entity == null) {
        Log.e(TAG, "请传入下载实体DownloadEntity");
        return;
      }
      switch (msg.what) {
        case STOP:
          startNextTask(entity);
          break;
        case CANCEL:
          startNextTask(entity);
          break;
        case COMPLETE:
          startNextTask(entity);
          break;
        case FAIL:
          handleFailTask(entity);
          break;
      }
      callback(msg.what, entity);
    }

    /**
     * 回调
     *
     * @param state 状态
     * @param entity 下载实体
     */
    private void callback(int state, DownloadEntity entity) {
      if (target.mTargetListener != null) {
        Task task = target.getTask(entity);
        switch (state) {
          case START:
            target.mTargetListener.onTaskStart(task);
            break;
          case STOP:
            target.mTargetListener.onTaskStop(task);
            break;
          case CANCEL:
            target.mTargetListener.onTaskCancel(task);
            break;
          case COMPLETE:
            target.mTargetListener.onTaskCancel(task);
            break;
          case FAIL:
            target.mTargetListener.onTaskFail(task);
            break;
        }
      }
    }

    /**
     * 处理下载任务下载失败的情形
     *
     * @param entity 失败实体
     */
    private void handleFailTask(DownloadEntity entity) {
      if (entity.getFailNum() <= target.mFailNum) {
        Task task = target.getTask(entity);
        target.reTryStart(task);
      } else {
        startNextTask(entity);
      }
    }

    /**
     * 启动下一个任务，条件：任务停止，取消下载，任务完成
     *
     * @param entity 通过Handler传递的下载实体
     */
    private void startNextTask(DownloadEntity entity) {
      target.removeTask(entity);
      Task newTask = target.getNextTask();
      if (newTask == null) {
        Log.e(TAG, "没有下一任务");
        return;
      }
      target.startTask(newTask);
    }
  }
}
