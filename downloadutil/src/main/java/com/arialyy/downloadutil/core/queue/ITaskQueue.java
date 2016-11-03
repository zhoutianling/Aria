package com.arialyy.downloadutil.core.queue;

import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.task.Task;
import com.arialyy.downloadutil.core.scheduler.IDownloadSchedulers;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITaskQueue extends IDownloader {

  /**
   * 创建一个新的下载任务，创建时只是将新任务存储到缓存池
   *
   * @param entity 下载实体{@link DownloadEntity}
   * @return {@link Task}
   */
  public Task createTask(DownloadEntity entity);

  /**
   * 通过下载链接从缓存池或任务池搜索下载任务，如果缓存池或任务池都没有任务，则创建新任务
   *
   * @param entity 下载实体{@link DownloadEntity}
   * @return {@link Task}
   */
  public Task getTask(DownloadEntity entity);

  /**
   * 通过下载链接删除任务
   *
   * @param entity 下载实体{@link DownloadEntity}
   */
  public void removeTask(DownloadEntity entity);

  /**
   * 获取缓存池的下一个任务
   *
   * @return 下载任务 or null
   */
  public Task getNextTask();

  /**
   * 设置下载调度器
   *
   * @param schedulers 下载调度器{@link IDownloadSchedulers}
   */
  public void setScheduler(IDownloadSchedulers schedulers);
}
