package com.arialyy.downloadutil.core.inf;

import com.arialyy.downloadutil.util.Task;

/**
 * Created by lyy on 2016/8/14.
 * 任务池
 */
public interface IPool {
  /**
   * 将下载任务添加到任务池中
   */
  public boolean putTask(Task task);

  /**
   * 按照队列原则取出下载任务
   *
   * @return 返回null或者下载任务
   */
  public Task pollTask();

  /**
   * 通过下载链接获取下载任务，当任务不为空时，队列将删除该下载任务
   *
   * @param downloadUrl 下载链接
   * @return 返回null或者下载任务
   */
  public Task getTask(String downloadUrl);

  /**
   * 删除任务池中的下载任务
   *
   * @param task 下载任务
   * @return true:移除成功
   */
  public boolean removeTask(Task task);

  /**
   * 通过下载链接移除下载任务
   *
   * @param downloadUrl 下载链接
   * @return true:移除成功
   */
  public boolean removeTask(String downloadUrl);

  /**
   * 池子大小
   *
   * @return 返回缓存池或者当前任务池大小
   */
  public int size();
}
