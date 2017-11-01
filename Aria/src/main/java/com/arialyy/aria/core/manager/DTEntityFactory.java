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
package com.arialyy.aria.core.manager;

import android.text.TextUtils;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.orm.DbEntity;

/**
 * Created by Aria.Lao on 2017/11/1.
 * 任务实体工厂
 */
class DTaskEntityFactory implements ITaskEntityFactory<DownloadEntity, DownloadTaskEntity> {
  private static final String TAG = "DTaskEntityFactory";
  private static volatile DTaskEntityFactory INSTANCE = null;

  private DTaskEntityFactory() {
  }

  public static DTaskEntityFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (DTaskEntityFactory.class) {
        INSTANCE = new DTaskEntityFactory();
      }
    }
    return INSTANCE;
  }

  @Override public DownloadTaskEntity create(DownloadEntity entity) {
    DownloadTaskEntity taskEntity =
        DbEntity.findFirst(DownloadTaskEntity.class, "key=? and isGroupTask='false' and url=?",
            entity.getDownloadPath(), entity.getUrl());
    if (taskEntity == null) {
      taskEntity = new DownloadTaskEntity();
      taskEntity.save(entity);
    } else if (taskEntity.entity == null || TextUtils.isEmpty(taskEntity.entity.getUrl())) {
      taskEntity.save(entity);
    } else if (!taskEntity.entity.getUrl().equals(entity.getUrl())) {  //处理地址切换而保存路径不变
      taskEntity.save(entity);
    }
    return taskEntity;
  }
}
