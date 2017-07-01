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

import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2017/6/3.
 */
public abstract class AbsNormalTask<ENTITY extends AbsEntity> extends AbsTask<ENTITY> {

  private boolean isHeighestTask = false;

  /**
   * 暂停任务，并让任务处于等待状态
   */
  public void stopAndWait() {

  }

  /**
   * 最高优先级命令，最高优先级命令有以下属性
   * 1、在下载队列中，有且只有一个最高优先级任务
   * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
   * 3、任务调度器不会暂停最高优先级任务
   * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
   * 5、如果下载队列中已经满了，则会停止队尾的任务
   * 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务
   */
  public void setHighestPriority(boolean isHighestPriority) {
    isHeighestTask = isHighestPriority;
  }

  public boolean isHighestPriorityTask() {
    return isHeighestTask;
  }

  /**
   * 获取百分比进度
   *
   * @return 返回百分比进度，如果文件长度为0，返回0
   */
  @Override public int getPercent() {
    if (mEntity.getFileSize() == 0) {
      return 0;
    }
    return (int) (mEntity.getCurrentProgress() * 100 / mEntity.getFileSize());
  }

  /**
   * 获取文件大小
   */
  @Override public long getFileSize() {
    return mEntity.getFileSize();
  }

  /**
   * 转换单位后的文件长度
   *
   * @return 如果文件长度为0，则返回0m，否则返回转换后的长度1b、1kb、1mb、1gb、1tb
   */
  @Override public String getConvertFileSize() {
    if (mEntity.getFileSize() == 0) {
      return "0mb";
    }
    return CommonUtil.formatFileSize(mEntity.getFileSize());
  }

  /**
   * 获取当前下载进度
   */
  @Override public long getCurrentProgress() {
    return mEntity.getCurrentProgress();
  }

  /**
   * 获取单位转换后的进度
   *
   * @return 如：已经下载3mb的大小，则返回{@code 3mb}
   */
  @Override public String getConvertCurrentProgress() {
    if (mEntity.getCurrentProgress() == 0) {
      return "0b";
    }
    return CommonUtil.formatFileSize(mEntity.getCurrentProgress());
  }
}
