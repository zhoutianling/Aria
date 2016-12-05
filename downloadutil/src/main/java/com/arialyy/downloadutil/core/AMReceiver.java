/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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
package com.arialyy.downloadutil.core;

import android.content.Context;
import com.arialyy.downloadutil.core.scheduler.OnSchedulerListener;

/**
 * AM 接收器
 */
public class AMReceiver {
  Object             obj;
  OnSchedulerListener listener;
  DownloadEntity      entity;
  DownloadManager manager = DownloadManager.getInstance();

  public AMTarget load(DownloadEntity entity) {
    this.entity = entity;
    return new AMTarget(this);
  }

  /**
   * 添加调度器回调
   */
  public AMReceiver addSchedulerListener(OnSchedulerListener listener) {
    this.listener = listener;
    manager.getTaskQueue().getDownloadSchedulers().addSchedulerListener(listener);
    return this;
  }

  /**
   * 移除回调
   */
  public AMReceiver removeSchedulerListener() {
    if (listener != null) {
      manager.getTaskQueue().getDownloadSchedulers().removeSchedulerListener(listener);
    }
    return this;
  }
}