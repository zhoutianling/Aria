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

package com.arialyy.aria.core.download;

/**
 * 下载监听
 */
interface IDownloadListener {

  /**
   * 支持断点回调
   *
   * @param support true,支持；false 不支持
   */
  public void supportBreakpoint(boolean support);

  /**
   * 取消下载
   */
  public void onCancel();

  /**
   * 下载失败
   */
  public void onFail();

  /**
   * 预处理
   */
  public void onPre();

  /**
   * 预处理完成,准备下载---开始下载之间
   */
  public void onPostPre(long fileSize);

  /**
   * 下载监听
   */
  public void onProgress(long currentLocation);

  /**
   * 单一线程的结束位置
   */
  public void onChildComplete(long finishLocation);

  /**
   * 开始
   */
  public void onStart(long startLocation);

  /**
   * 子程恢复下载的位置
   */
  public void onChildResume(long resumeLocation);

  /**
   * 恢复位置
   */
  public void onResume(long resumeLocation);

  /**
   * 停止
   */
  public void onStop(long stopLocation);

  /**
   * 下载完成
   */
  public void onComplete();
}