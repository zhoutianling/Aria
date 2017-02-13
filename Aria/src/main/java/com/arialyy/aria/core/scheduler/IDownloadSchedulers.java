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

import android.os.Handler;
import com.arialyy.aria.core.download.DownloadEntity;

/**
 * Created by “AriaLyy@outlook.com” on 2016/11/2.
 * 下载调度器接口
 */
public interface IDownloadSchedulers extends Handler.Callback {

  /**
   * 注册下载器监听，一个观察者只能注册一次监听
   *
   * @param targetName 观察者，创建该监听器的对象类名
   * @param schedulerListener {@link OnSchedulerListener}
   */
  public void addSchedulerListener(String targetName, OnSchedulerListener schedulerListener);

  /**
   * @param targetName 观察者，创建该监听器的对象类名
   * 取消注册监听器
   */
  public void removeSchedulerListener(String targetName, OnSchedulerListener schedulerListener);

  /**
   * 处理下载任务下载失败的情形
   *
   * @param entity 下载实体
   */
  public void handleFailTask(DownloadEntity entity);

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   *
   * @param entity 通过Handler传递的下载实体
   */
  public void startNextTask(DownloadEntity entity);
}