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
package com.arialyy.aria.core.receiver;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.AMTarget;
import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.OnSchedulerListener;
import com.arialyy.aria.util.CheckUtil;

/**
 * Created by lyy on 2016/12/5.
 * AM 接收器
 */
public class DownloadReceiver {
  public String targetName;
  public OnSchedulerListener listener;

  /**
   * {@link #load(String)}，请使用该方法
   */
  @Deprecated public AMTarget load(DownloadEntity entity) {
    return new AMTarget(entity, targetName);
  }

  /**
   * 读取下载链接
   */
  public AMTarget load(@NonNull String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    DownloadEntity entity =
        DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
    if (entity == null) {
      entity = new DownloadEntity();
    }
    entity.setDownloadUrl(downloadUrl);
    return new AMTarget(entity, targetName);
  }

  /**
   * 添加调度器回调
   */
  public DownloadReceiver addSchedulerListener(OnSchedulerListener listener) {
    this.listener = listener;
    DownloadSchedulers.getInstance().addSchedulerListener(targetName, listener);
    return this;
  }

  /**
   * 移除回调
   */
  public DownloadReceiver removeSchedulerListener() {
    if (listener != null) {
      DownloadSchedulers.getInstance().removeSchedulerListener(targetName, listener);
    }
    return this;
  }

  public void destroy() {
    targetName = null;
    listener = null;
  }
}