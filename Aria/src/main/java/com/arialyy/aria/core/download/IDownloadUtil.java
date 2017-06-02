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
 * Created by lyy on 2016/10/31.
 * 抽象的下载接口
 */
interface IDownloadUtil {

  /**
   * 获取当前下载位置
   */
  public long getCurrentLocation();

  /**
   * 是否正在下载
   *
   * @return true, 正在下载
   */
  public boolean isDownloading();

  /**
   * 取消下载
   */
  public void cancelDownload();

  /**
   * 停止下载
   */
  public void stopDownload();

  /**
   * 开始下载
   */
  public void startDownload();

  /**
   * 从上次断点恢复下载
   */
  public void resumeDownload();

  /**
   * 删除下载记录文件
   */
  public void delConfigFile();

  /**
   * 删除temp文件
   */
  public void delTempFile();
}