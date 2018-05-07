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
 * Created by AriaL on 2017/6/29.
 */
public interface ITarget<TARGET extends ITarget> {
  /**
   * 获取任务状态
   *
   * @return {@link IEntity}
   */
  int getTaskState();

  /**
   * 任务是否在执行
   *
   * @return {@code true} 任务正在执行
   */
  boolean isRunning();

  /**
   * 任务是否存在
   *
   * @return {@code true} 任务存在
   */
  boolean taskExists();

  /**
   * 任务文件大小
   */
  long getSize();

  /**
   * 转换后的大小
   */
  String getConvertSize();

  /**
   * 获取任务进度百分比
   */
  int getPercent();

  /**
   * 获取任务进度，如果任务存在，则返回当前进度
   */
  long getCurrentProgress();

  /**
   * 开始下载
   */
  void start();

  /**
   * 停止下载
   */
  void stop();

  /**
   * 恢复下载
   */
  void resume();

  /**
   * 取消下载
   */
  void cancel();
}
