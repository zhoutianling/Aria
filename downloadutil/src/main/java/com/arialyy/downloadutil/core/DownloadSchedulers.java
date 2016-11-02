package com.arialyy.downloadutil.core;

import android.os.Message;
import android.util.Log;
import com.arialyy.downloadutil.core.inf.IDownloadSchedulers;
import com.arialyy.downloadutil.core.pool.ExecutePool;

/**
 * Created by lyy on 2016/8/16.
 * 任务下载器，提供抽象的方法供具体的实现类操作
 */
public class DownloadSchedulers implements IDownloadSchedulers {
  /**
   * 任务开始
   */
  public static final     int                START    = 1;
  /**
   * 任务停止
   */
  public static final     int                STOP     = 2;
  /**
   * 任务失败
   */
  public static final     int                FAIL     = 3;
  /**
   * 任务取消
   */
  public static final     int                CANCEL   = 4;
  /**
   * 任务完成
   */
  public static final     int                COMPLETE = 5;
  private static final    String             TAG      = "DownloadSchedulers";
  private static final    Object             LOCK     = new Object();
  private static volatile DownloadSchedulers INSTANCE = null;
  /**
   * 下载失败次数
   */
  int mFailNum = 10;

  /**
   * 超时时间
   */
  long mTimeOut = 10000;

  /**
   * 下载器任务监听
   */
  OnTargetListener  mTargetListener;
  DownloadTaskQueue mQueue;

  public DownloadSchedulers(DownloadTaskQueue downloadTaskQueue) {
    mQueue = downloadTaskQueue;
  }

  public static DownloadSchedulers getInstance(DownloadTaskQueue queue) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadSchedulers(queue);
      }
    }
    return INSTANCE;
  }

  @Override public boolean handleMessage(Message msg) {
    DownloadEntity entity = (DownloadEntity) msg.obj;
    if (entity == null) {
      Log.e(TAG, "请传入下载实体DownloadEntity");
      return true;
    }
    switch (msg.what) {
      case STOP:
      case CANCEL:
        if (mQueue.getExecutePool().size() != ExecutePool.SIZE) {
          startNextTask(entity);
        }
        break;
      case COMPLETE:
        startNextTask(entity);
        break;
      case FAIL:
        handleFailTask(entity);
        break;
    }
    callback(msg.what, entity);
    return true;
  }

  /**
   * 回调
   *
   * @param state 状态
   * @param entity 下载实体
   */
  private void callback(int state, DownloadEntity entity) {
    if (mTargetListener != null) {
      Task task = mQueue.getTask(entity);
      switch (state) {
        case START:
          mTargetListener.onTaskStart(task);
          break;
        case STOP:
          mTargetListener.onTaskStop(task);
          break;
        case CANCEL:
          mTargetListener.onTaskCancel(task);
          break;
        case COMPLETE:
          mTargetListener.onTaskComplete(task);
          break;
        case FAIL:
          mTargetListener.onTaskFail(task);
          break;
      }
    }
  }

  /**
   * 处理下载任务下载失败的情形
   *
   * @param entity 失败实体
   */
  @Override public void handleFailTask(DownloadEntity entity) {
    if (entity.getFailNum() <= mFailNum) {
      Task task = mQueue.getTask(entity);
      mQueue.reTryStart(task);
    } else {
      startNextTask(entity);
    }
  }

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   *
   * @param entity 通过Handler传递的下载实体
   */
  @Override public void startNextTask(DownloadEntity entity) {
    mQueue.removeTask(entity);
    Task newTask = mQueue.getNextTask();
    if (newTask == null) {
      Log.w(TAG, "没有下一任务");
      return;
    }
    if (newTask.getDownloadEntity().getState() == DownloadEntity.STATE_WAIT) {
      mQueue.startTask(newTask);
    }
  }

  /**
   * 设置下载器监听
   *
   * @param targetListener {@link OnTargetListener}
   */
  public void setOnTargetListener(OnTargetListener targetListener) {
    this.mTargetListener = targetListener;
  }

  public void setFailNum(int mFailNum) {
    this.mFailNum = mFailNum;
  }

  public void setTimeOut(long timeOut) {
    this.mTimeOut = timeOut;
  }

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
}
