/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
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

package com.arialyy.aria.core.queue;

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.ITaskEntity;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITaskQueue<TASK extends ITask, TASK_ENTITY extends ITaskEntity, ENTITY extends IEntity> {

  /**
   * 开始任务
   *
   * @param task {@link DownloadTask}
   */
  public void startTask(TASK task);

  /**
   * 停止任务
   *
   * @param task {@link DownloadTask}
   */
  public void stopTask(TASK task);

  /**
   * 取消任务
   *
   * @param task {@link DownloadTask}
   */
  public void cancelTask(TASK task);

  /**
   * 重试下载
   *
   * @param task {@link DownloadTask}
   */
  public void reTryStart(TASK task);

  /**
   * 任务池队列大小
   */
  public int size();

  /**
   * 设置下载任务数
   *
   * @param downloadNum 下载任务数
   */
  public void setDownloadNum(int downloadNum);

  /**
   * 创建一个新的下载任务，创建时只是将新任务存储到缓存池
   *
   * @param entity 下载实体{@link DownloadTaskEntity}
   * @param targetName 生成该任务的对象
   * @return {@link DownloadTask}
   */
  public TASK createTask(String targetName, TASK_ENTITY entity);

  /**
   * 通过下载链接从缓存池或任务池搜索下载任务，如果缓存池或任务池都没有任务，则创建新任务
   *
   * @param entity 下载实体{@link DownloadEntity}
   * @return {@link DownloadTask}
   */
  public TASK getTask(ENTITY entity);

  /**
   * 通过下载链接删除任务
   *
   * @param entity 下载实体{@link DownloadEntity}
   */
  public void removeTask(ENTITY entity);

  /**
   * 获取缓存池的下一个任务
   *
   * @return 下载任务 or null
   */
  public TASK getNextTask();
}