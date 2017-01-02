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
package com.arialyy.aria.core;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.OnSchedulerListener;
import com.arialyy.aria.util.CheckUtil;

/**
 * Created by lyy on 2016/12/5.
 * AM 接收器
 */
public class AMReceiver {
  Object              obj;
  OnSchedulerListener listener;
  DownloadEntity      entity;

  /**
   * {@link #load(String)}，请使用该方法
   */
  @Deprecated public AMTarget load(DownloadEntity entity) {
    this.entity = entity;
    return new AMTarget(this);
  }

  /**
   * 读取下载链接
   */
  public AMTarget load(@NonNull String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    if (entity == null) {
      entity = DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
    }
    if (entity == null) {
      entity = new DownloadEntity();
    }
    entity.setDownloadUrl(downloadUrl);
    return new AMTarget(this);
  }

  /**
   * 添加调度器回调
   */
  public AMReceiver addSchedulerListener(OnSchedulerListener listener) {
    this.listener = listener;
    DownloadSchedulers.getInstance().addSchedulerListener(obj, listener);
    return this;
  }

  /**
   * 移除回调
   */
  public AMReceiver removeSchedulerListener() {
    if (listener != null) {
      DownloadSchedulers.getInstance().removeSchedulerListener(obj, listener);
    }
    return this;
  }

  void destroy() {
    obj = null;
    listener = null;
  }
}