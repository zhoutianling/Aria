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


package com.arialyy.downloadutil.core.queue;

import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.scheduler.IDownloadSchedulers;
import com.arialyy.downloadutil.core.task.Task;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITaskQueue extends IDownloader {

  /**
   * 设置下载任务数
   *
   * @param downloadNum 下载任务数
   */
  public void setDownloadNum(int downloadNum);

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