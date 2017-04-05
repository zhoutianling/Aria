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
package com.arialyy.aria.core.scheduler;

import com.arialyy.aria.core.inf.ITask;

/**
 * Target处理任务监听
 */
public interface ISchedulerListener<TASK extends ITask> {
  /**
   * 任务预加载
   */
  public void onTaskPre(TASK task);

  /**
   * 任务恢复下载
   */
  public void onTaskResume(TASK task);

  /**
   * 任务开始
   */
  public void onTaskStart(TASK task);

  /**
   * 任务停止
   */
  public void onTaskStop(TASK task);

  /**
   * 任务取消
   */
  public void onTaskCancel(TASK task);

  /**
   * 任务下载失败
   */
  public void onTaskFail(TASK task);

  /**
   * 任务完成
   */
  public void onTaskComplete(TASK task);

  /**
   * 任务执行中
   */
  public void onTaskRunning(TASK task);
}