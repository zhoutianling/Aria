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

import com.arialyy.aria.core.inf.ITask;

/**
 * Created by lyy on 2016/8/14.
 * 任务池
 */
public interface IPool<T extends ITask> {
  /**
   * 将下载任务添加到任务池中
   */
  public boolean putTask(T task);

  /**
   * 按照队列原则取出下载任务
   *
   * @return 返回null或者下载任务
   */
  public T pollTask();

  /**
   * 通过下载链接获取下载任务，当任务不为空时，队列将删除该下载任务
   *
   * @param downloadUrl 下载链接
   * @return 返回null或者下载任务
   */
  public T getTask(String downloadUrl);

  /**
   * 删除任务池中的下载任务
   *
   * @param task 下载任务
   * @return true:移除成功
   */
  public boolean removeTask(T task);

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