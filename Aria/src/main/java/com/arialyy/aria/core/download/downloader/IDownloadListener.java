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

import com.arialyy.aria.core.inf.IEventListener;

/**
 * 下载监听
 */
interface IDownloadListener extends IEventListener {

  /**
   * 支持断点回调
   *
   * @param support true,支持；false 不支持
   */
  void supportBreakpoint(boolean support);

  /**
   * 单一线程的结束位置
   */
  void onChildComplete(long finishLocation);

  /**
   * 子程恢复下载的位置
   */
  void onChildResume(long resumeLocation);
}