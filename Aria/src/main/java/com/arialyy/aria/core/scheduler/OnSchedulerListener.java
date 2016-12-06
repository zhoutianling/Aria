package com.arialyy.aria.core.scheduler;

import com.arialyy.aria.core.task.Task;

/**
 * Target处理任务监听
 */
public interface OnSchedulerListener {
  /**
   * 任务预加载
   */
  public void onTaskPre(Task task);

  /**
   * 任务恢复下载
   */
  public void onTaskResume(Task task);

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

  /**
   * 任务执行中
   */
  public void onTaskRunning(Task task);
}