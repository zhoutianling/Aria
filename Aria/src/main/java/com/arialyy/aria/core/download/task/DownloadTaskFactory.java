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

package com.arialyy.aria.core.download.task;

import android.content.Context;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.scheduler.IDownloadSchedulers;

/**
 * Created by lyy on 2016/8/18.
 * 任务工厂
 */
public class DownloadTaskFactory {
  private static final String TAG = "DownloadTaskFactory";

  private static final Object LOCK = new Object();
  private static volatile DownloadTaskFactory INSTANCE = null;

  private DownloadTaskFactory() {

  }

  public static DownloadTaskFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadTaskFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * 创建普通下载任务
   *
   * @param entity 下载任务实体{@link DownloadTaskEntity}
   * @param schedulers {@link IDownloadSchedulers}
   */
  public DownloadTask createTask(Context context, DownloadTaskEntity entity,
      IDownloadSchedulers schedulers) {
    return createTask("", context, entity, schedulers);
  }

  /**
   * @param entity 下载任务实体{@link DownloadTaskEntity}
   * @param schedulers {@link IDownloadSchedulers}
   */
  public DownloadTask createTask(String targetName, Context context, DownloadTaskEntity entity,
      IDownloadSchedulers schedulers) {
    DownloadTask.Builder builder = new DownloadTask.Builder(targetName, context, entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }
}