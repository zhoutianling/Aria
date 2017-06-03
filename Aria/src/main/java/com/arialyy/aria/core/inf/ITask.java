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
package com.arialyy.aria.core.inf;

/**
 * Created by lyy on 2017/2/13.
 */

public interface ITask<ENTITY extends AbsEntity> {

  /**
   * 设置任务为最高优先级任务，在下载队列中，有且只有一个最高优先级任务
   */
  public void setHighestPriority(boolean isHighestPriority);

  /**
   * 该任务是否是最高优先级任务
   *
   * @return {@code true} 任务为最高优先级任务
   */
  public boolean isHighestPriorityTask();

  /**
   * 唯一标识符，DownloadTask 为下载地址，UploadTask 为文件路径
   */
  public String getKey();

  /**
   * 是否真正执行
   *
   * @return true，正在执行；
   */
  public boolean isRunning();

  /**
   * 获取工具实体
   */
  public ENTITY getEntity();

  public void start();

  public void stop();

  public void cancel();

  /**
   * 原始byte速度
   */
  public long getSpeed();

  /**
   * 转换单位后的速度
   */
  public String getConvertSpeed();

  /**
   * 获取百分比进度
   */
  public int getPercent();

  /**
   * 原始文件byte长度
   */
  public long getFileSize();

  /**
   * 转换单位后的文件长度
   */
  public String getConvertFileSize();

  /**
   * 获取当前进度
   */
  public long getCurrentProgress();

  /**
   * 获取单位转换后的进度
   * @return 返回 3mb
   */
  public String getConvertCurrentProgress();

  public void setTargetName(String targetName);

  public void removeRecord();
}
