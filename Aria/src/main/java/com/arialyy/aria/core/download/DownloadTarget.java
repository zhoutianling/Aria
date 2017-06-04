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
import android.util.Log;
import com.arialyy.aria.core.RequestEnum;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
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
   * 将任务设置为最高优先级任务，最高优先级任务有以下特点：
   * 1、在下载队列中，有且只有一个最高优先级任务
   * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
   * 3、任务调度器不会暂停最高优先级任务
   * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
   * 5、如果下载队列中已经满了，则会停止队尾的任务
   * 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务
   */
  @Override public void setHighestPriority() {
    super.setHighestPriority();
  }

  /**
   * 重定向后，新url的key，默认为location
   */
  public DownloadTarget setRedirectUrlKey(String redirectUrlKey) {
    super._setRedirectUrlKey(redirectUrlKey);
    return this;
  }

  @Override public int getPercent() {
    if (entity == null) {
      Log.e("DownloadTarget", "下载管理器中没有该任务");
      return 0;
    }
    if (entity.getFileSize() != 0) {
      return (int) (entity.getCurrentProgress() * 100 / entity.getFileSize());
    }
    return super.getPercent();
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
   * 下载任务是否存在
   */
  @Override public boolean taskExists() {
    return DownloadTaskQueue.getInstance().getTask(entity.getDownloadUrl()) != null;
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
   *
   * @deprecated {@link #setFileName(String)}
   */
  @Deprecated public DownloadTarget setDownloadName(@NonNull String downloadName) {
    if (TextUtils.isEmpty(downloadName)) {
      throw new IllegalArgumentException("文件名不能为null");
    }
    entity.setFileName(downloadName);
    return this;
  }

  /**
   * 设置文件名
   */
  public DownloadTarget setFileName(@NonNull String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      throw new IllegalArgumentException("文件名不能为null");
    }
    entity.setFileName(fileName);
    return this;
  }

  private DownloadEntity getDownloadEntity() {
    return entity;
  }

  /**
   * 是否在下载
   */
  public boolean isDownloading() {
    DownloadTask task = DownloadTaskQueue.getInstance().getTask(entity);
    return task != null && task.isRunning();
  }
}
