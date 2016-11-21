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


package com.arialyy.downloadutil.core.task;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.scheduler.IDownloadSchedulers;

/**
 * Created by lyy on 2016/8/18.
 * 任务工厂
 */
public class TaskFactory {
  private static final String TAG = "TaskFactory";

  private static final    Object      LOCK     = new Object();
  private static volatile TaskFactory INSTANCE = null;

  private TaskFactory() {

  }

  public static TaskFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new TaskFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * 创建普通下载任务
   *
   * @param entity 下载实体
   * @param schedulers {@link IDownloadSchedulers}
   */
  public Task createTask(Context context, DownloadEntity entity, IDownloadSchedulers schedulers) {
    Task.Builder builder = new Task.Builder(context, entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }
}