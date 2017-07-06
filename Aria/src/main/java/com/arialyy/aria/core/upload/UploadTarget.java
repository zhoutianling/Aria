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
package com.arialyy.aria.core.upload;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.RequestEnum;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.queue.UploadTaskQueue;
import java.util.Map;

/**
 * Created by lyy on 2017/2/28.
 */
public class UploadTarget extends AbsTarget<UploadEntity, UploadTaskEntity> {

  UploadTarget(UploadEntity entity, String targetName) {
    this.entity = entity;
    this.targetName = targetName;
    taskEntity = new UploadTaskEntity(entity);
  }

  /**
   * 设置userAgent
   */
  public UploadTarget setUserAngent(@NonNull String userAgent) {
    taskEntity.userAgent = userAgent;
    return this;
  }

  /**
   * 设置上传路径
   *
   * @param uploadUrl 上传路径
   */
  public UploadTarget setUploadUrl(@NonNull String uploadUrl) {
    taskEntity.uploadUrl = uploadUrl;
    return this;
  }

  /**
   * 设置服务器需要的附件key
   *
   * @param attachment 附件key
   */
  public UploadTarget setAttachment(@NonNull String attachment) {
    taskEntity.attachment = attachment;
    return this;
  }

  /**
   * 设置文件名
   */
  public UploadTarget setFileName(String fileName) {
    entity.setFileName(fileName);
    return this;
  }

  /**
   * 设置上传文件类型
   *
   * @param contentType tip：multipart/form-data
   */
  public UploadTarget setContentType(String contentType) {
    taskEntity.contentType = contentType;
    return this;
  }

  /**
   * 给url请求添加头部
   *
   * @param key 头部key
   * @param header 头部value
   */
  public UploadTarget addHeader(@NonNull String key, @NonNull String header) {
    super._addHeader(key, header);
    return this;
  }

  /**
   * 给url请求添加头部
   *
   * @param headers key为http头部的key，Value为http头对应的配置
   */
  public UploadTarget addHeaders(Map<String, String> headers) {
    super._addHeaders(headers);
    return this;
  }

  /**
   * 下载任务是否存在
   */
  @Override public boolean taskExists() {
    return UploadTaskQueue.getInstance().getTask(entity.getFilePath()) != null;
  }

  /**
   * 设置请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  public UploadTarget setRequestMode(RequestEnum requestEnum) {
    super._setRequestMode(requestEnum);
    return this;
  }

  private UploadEntity getUploadEntity() {
    return entity;
  }

  /**
   * 是否在下载
   */
  public boolean isUploading() {
    UploadTask task = UploadTaskQueue.getInstance().getTask(entity);
    return task != null && task.isRunning();
  }
}
