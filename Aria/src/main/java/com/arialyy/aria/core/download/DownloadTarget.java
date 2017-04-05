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

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.arialyy.aria.core.RequestEnum;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.util.CheckUtil;
import java.io.File;
import java.util.Map;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class DownloadTarget extends AbsTarget<DownloadEntity, DownloadTaskEntity> {

  DownloadTarget(DownloadEntity entity, String targetName) {
    this.entity = entity;
    this.targetName = targetName;
    taskEntity = new DownloadTaskEntity(entity);
  }

  @Override public void pause() {
    super.pause();
  }

  @Override public void resume() {
    super.resume();
  }

  /**
   * 给url请求添加头部
   *
   * @param key 头部key
   * @param header 头部value
   */
  public DownloadTarget addHeader(@NonNull String key, @NonNull String header) {
    super._addHeader(key, header);
    return this;
  }

  /**
   * 给url请求添加头部
   *
   * @param headers key为http头部的key，Value为http头对应的配置
   */
  public DownloadTarget addHeaders(Map<String, String> headers) {
    super._addHeaders(headers);
    return this;
  }

  /**
   * 设置请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  public DownloadTarget setRequestMode(RequestEnum requestEnum) {
    super._setRequestMode(requestEnum);
    return this;
  }

  /**
   * 设置文件存储路径
   */
  public DownloadTarget setDownloadPath(@NonNull String downloadPath) {
    if (TextUtils.isEmpty(downloadPath)) {
      throw new IllegalArgumentException("文件保持路径不能为null");
    }
    File file = new File(downloadPath);
    entity.setDownloadPath(downloadPath);
    entity.setFileName(file.getName());
    return this;
  }

  /**
   * 设置文件名
   */
  @Deprecated public DownloadTarget setDownloadName(@NonNull String downloadName) {
    if (TextUtils.isEmpty(downloadName)) {
      throw new IllegalArgumentException("文件名不能为null");
    }
    entity.setFileName(downloadName);
    return this;
  }

  private DownloadEntity getDownloadEntity(String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    return DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
  }

  /**
   * 是否在下载
   */
  public boolean isDownloading() {
    return DownloadTaskQueue.getInstance().getTask(entity).isRunning();
  }
}
