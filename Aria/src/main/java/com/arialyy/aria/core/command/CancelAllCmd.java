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

package com.arialyy.aria.core.command;

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.List;

/**
 * Created by AriaL on 2017/6/27.
 * 删除所有任务，并且删除所有回掉
 */
final class CancelAllCmd<T extends AbsTaskEntity> extends AbsCmd<T> {
  /**
   * @param targetName 产生任务的对象名
   */
  CancelAllCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    mQueue.removeAllTask();
    if (mTaskEntity instanceof DownloadTaskEntity) {
      handleDownloadRemove();
    } else {
      handleUploadRemove();
    }
  }

  /**
   * 处理上传的删除
   */
  private void handleUploadRemove() {
    List<UploadEntity> allEntity = DbEntity.findAllData(UploadEntity.class);
    for (UploadEntity entity : allEntity) {
      CommonUtil.delUploadTaskConfig(mTaskEntity.removeFile, entity);
    }
  }

  /**
   * 处理下载的删除
   */
  private void handleDownloadRemove() {
    List<DownloadEntity> allEntity = DbEntity.findAllData(DownloadEntity.class);
    for (DownloadEntity entity : allEntity) {
      CommonUtil.delDownloadTaskConfig(mTaskEntity.removeFile, entity);
    }
  }
}
