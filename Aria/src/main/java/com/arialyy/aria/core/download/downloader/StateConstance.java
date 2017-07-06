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
package com.arialyy.aria.core.download.downloader;

/**
 * Created by lyy on 2017/1/18.
 * 下载状态常量
 */
final class StateConstance {
  int CANCEL_NUM = 0;
  int STOP_NUM = 0;
  int FAIL_NUM = 0;
  int CONNECT_TIME_OUT; //连接超时时间
  int READ_TIME_OUT; //流读取的超时时间
  int COMPLETE_THREAD_NUM = 0;
  int THREAD_NUM;
  long CURRENT_LOCATION = 0;
  boolean isDownloading = false;
  boolean isCancel = false;
  boolean isStop = false;

  StateConstance() {
  }

  void cleanState() {
    isCancel = false;
    isStop = false;
    isDownloading = true;
    CURRENT_LOCATION = 0;
    CANCEL_NUM = 0;
    STOP_NUM = 0;
    FAIL_NUM = 0;
  }

  void setThreadNum(int threadNum) {
    THREAD_NUM = threadNum;
  }

  /**
   * 所有子线程是否都已经停止下载
   */
  boolean isStop() {
    return STOP_NUM == THREAD_NUM;
  }

  /**
   * 所有子线程是否都已经下载失败
   */
  boolean isFail() {
    return FAIL_NUM == THREAD_NUM;
  }

  /**
   * 所有子线程是否都已经完成下载
   */
  boolean isComplete() {
    return COMPLETE_THREAD_NUM == THREAD_NUM;
  }

  /**
   * 所有子线程是否都已经取消下载
   */
  boolean isCancel() {
    return CANCEL_NUM == THREAD_NUM;
  }
}
