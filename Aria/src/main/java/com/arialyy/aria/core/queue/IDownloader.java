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

import com.arialyy.aria.core.task.DownloadTask;

/**
 * Created by lyy on 2016/8/16.
 * 下载功能接口
 */
public interface IDownloader {
  /**
   * 开始任务
   *
   * @param task {@link DownloadTask}
   */
  public void startTask(DownloadTask task);

  /**
   * 停止任务
   *
   * @param task {@link DownloadTask}
   */
  public void stopTask(DownloadTask task);

  /**
   * 取消任务
   *
   * @param task {@link DownloadTask}
   */
  public void cancelTask(DownloadTask task);

  /**
   * 重试下载
   *
   * @param task {@link DownloadTask}
   */
  public void reTryStart(DownloadTask task);
}