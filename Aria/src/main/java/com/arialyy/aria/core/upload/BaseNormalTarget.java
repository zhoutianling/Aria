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
import android.text.TextUtils;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.manager.TEManager;
import com.arialyy.aria.core.queue.UploadTaskQueue;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;

/**
 * Created by AriaL on 2018/3/9.
 */
abstract class BaseNormalTarget<TARGET extends AbsUploadTarget>
    extends AbsUploadTarget<TARGET, UploadEntity, UploadTaskEntity> {
  private static final String TAG = "BaseNormalTarget";

  protected void initTarget(String filePath) {
    mTaskEntity = TEManager.getInstance().getTEntity(UploadTaskEntity.class, filePath);
    if (mTaskEntity == null) {
      mTaskEntity = TEManager.getInstance().createTEntity(UploadTaskEntity.class, filePath);
    }
    mEntity = mTaskEntity.entity;
    File file = new File(filePath);
    mEntity.setFileName(file.getName());
    mEntity.setFileSize(file.length());
  }

  /**
   * 设置上传路径
   *
   * @param uploadUrl 上传路径
   */
  public abstract TARGET setUploadUrl(@NonNull String uploadUrl);

  /**
   * 上传任务是否存在
   *
   * @return {@code true}存在
   */
  @Override public boolean taskExists() {
    return UploadTaskQueue.getInstance().getTask(mEntity.getFilePath()) != null;
  }

  /**
   * 是否在上传
   *
   * @deprecated {@link #isRunning()}
   */
  public boolean isUploading() {
    return isRunning();
  }

  @Override public boolean isRunning() {
    UploadTask task = UploadTaskQueue.getInstance().getTask(mEntity.getKey());
    return task != null && task.isRunning();
  }

  @Override protected boolean checkEntity() {
    return checkUrl() && checkFilePath();
  }

  /**
   * 检查上传文件路径是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkFilePath() {
    String filePath = mEntity.getFilePath();
    if (TextUtils.isEmpty(filePath)) {
      ALog.e(TAG, "上传失败，文件路径为null");
      return false;
    } else if (!filePath.startsWith("/")) {
      ALog.e(TAG, "上传失败，文件路径【" + filePath + "】不合法");
      return false;
    }

    File file = new File(mEntity.getFilePath());
    if (!file.exists()) {
      ALog.e(TAG, "上传失败，文件【" + filePath + "】不存在");
      return false;
    }
    if (file.isDirectory()) {
      ALog.e(TAG, "上传失败，文件【" + filePath + "】不能是文件夹");
      return false;
    }
    return true;
  }

  /**
   * 检查普通任务的下载地址
   *
   * @return {@code true}地址合法
   */
  private boolean checkUrl() {
    final String url = mEntity.getUrl();
    if (TextUtils.isEmpty(url)) {
      ALog.e(TAG, "上传失败，url为null");
      return false;
    } else if (!url.startsWith("http") && !url.startsWith("ftp")) {
      ALog.e(TAG, "上传失败，url【" + url + "】错误");
      return false;
    }
    int index = url.indexOf("://");
    if (index == -1) {
      ALog.e(TAG, "上传失败，url【" + url + "】不合法");
      return false;
    }
    return true;
  }
}
