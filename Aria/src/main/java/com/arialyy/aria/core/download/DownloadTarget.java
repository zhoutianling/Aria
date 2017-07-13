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
import com.arialyy.aria.core.inf.AbsNormalTarget;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class DownloadTarget
    extends AbsNormalTarget<DownloadTarget, DownloadEntity, DownloadTaskEntity> {

  DownloadTarget(DownloadEntity entity, String targetName) {
    this(entity.getDownloadUrl(), targetName);
  }

  DownloadTarget(String url, String targetName) {
    mTargetName = targetName;
    DownloadEntity entity = getEntity(url);
    mTaskEntity = DbEntity.findFirst(DownloadTaskEntity.class, "key=? and isGroupTask='false'",
        entity.getDownloadPath());
    if (mTaskEntity == null) {
      mTaskEntity = new DownloadTaskEntity();
      mTaskEntity.key = entity.getDownloadPath();
      mTaskEntity.entity = entity;
      mTaskEntity.save();
    }
    if (mTaskEntity.entity == null) {
      mTaskEntity.entity = entity;
    }
    mEntity = entity;
  }

  /**
   * 如果任务存在，但是下载实体不存在，则通过下载地址获取下载实体
   *
   * @param downloadUrl 下载地址
   */
  private DownloadEntity getEntity(String downloadUrl) {
    DownloadEntity entity =
        DownloadEntity.findFirst(DownloadEntity.class, "downloadUrl=? and isGroupChild='false'",
            downloadUrl);
    if (entity == null) {
      entity = new DownloadEntity();
      entity.setDownloadUrl(downloadUrl);
      entity.setGroupChild(false);
      entity.save();
    }
    File file = new File(entity.getDownloadPath());
    if (!file.exists()) {
      entity.setState(IEntity.STATE_WAIT);
    }
    return entity;
  }

  /**
   * 将任务设置为最高优先级任务，最高优先级任务有以下特点：
   * 1、在下载队列中，有且只有一个最高优先级任务
   * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
   * 3、任务调度器不会暂停最高优先级任务
   * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
   * 5、如果下载队列中已经满了，则会停止队尾的任务，当高优先级任务完成后，该队尾任务将自动执行
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

  /**
   * 下载任务是否存在
   */
  @Override public boolean taskExists() {
    return DownloadTaskQueue.getInstance().getTask(mEntity.getDownloadUrl()) != null;
  }

  /**
   * 设置文件存储路径
   */
  public DownloadTarget setDownloadPath(@NonNull String downloadPath) {
    if (TextUtils.isEmpty(downloadPath)) {
      throw new IllegalArgumentException("文件保持路径不能为null");
    }
    File file = new File(downloadPath);
    mEntity.setDownloadPath(downloadPath);
    mEntity.setFileName(file.getName());
    mTaskEntity.key = downloadPath;
    mTaskEntity.update();
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
    mEntity.setFileName(downloadName);
    return this;
  }

  /**
   * 设置文件名
   */
  public DownloadTarget setFileName(@NonNull String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      throw new IllegalArgumentException("文件名不能为null");
    }
    mEntity.setFileName(fileName);
    return this;
  }

  private DownloadEntity getDownloadEntity() {
    return mEntity;
  }

  /**
   * 是否在下载
   */
  public boolean isDownloading() {
    DownloadTask task = DownloadTaskQueue.getInstance().getTask(mEntity);
    return task != null && task.isRunning();
  }
}
