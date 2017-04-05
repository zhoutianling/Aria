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
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.aria.core.upload.UploadTaskEntity;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITaskQueue<TASK extends ITask, TASK_ENTITY extends ITaskEntity, ENTITY extends IEntity> {

  /**
   * 开始任务
   *
   * @param task {@link DownloadTask}、{@link UploadTask}
   */
  public void startTask(TASK task);

  /**
   * 停止任务
   *
   * @param task {@link DownloadTask}、{@link UploadTask}
   */
  public void stopTask(TASK task);

  /**
   * 取消任务
   *
   * @param task {@link DownloadTask}、{@link UploadTask}
   */
  public void cancelTask(TASK task);

  /**
   * 重试下载
   *
   * @param task {@link DownloadTask}、{@link UploadTask}
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
   * 创建一个新的任务，创建时只是将新任务存储到缓存池
   *
   * @param entity 任务实体{@link DownloadTaskEntity}、{@link UploadTaskEntity}
   * @param targetName 生成该任务的对象
   * @return {@link DownloadTask}、{@link UploadTask}
   */
  public TASK createTask(String targetName, TASK_ENTITY entity);

  /**
   * 通过工作实体缓存池或任务池搜索下载任务，如果缓存池或任务池都没有任务，则创建新任务
   *
   * @param entity 工作实体{@link DownloadEntity}、{@link UploadEntity}
   * @return {@link DownloadTask}、{@link UploadTask}
   */
  public TASK getTask(ENTITY entity);

  /**
   * 通过工作实体删除任务
   *
   * @param entity 工作实体{@link DownloadEntity}、{@link UploadEntity}
   */
  public void removeTask(ENTITY entity);

  /**
   * 获取缓存池的下一个任务
   *
   * @return 下载任务 or null
   */
  public TASK getNextTask();
}