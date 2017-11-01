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

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.CommonUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aria.Lao on 2017/11/1.
 * 任务实体管理器，负责
 */
public class TaskEntityManager {
  private static final String TAG = "TaskManager";
  private static volatile TaskEntityManager INSTANCE = null;
  private Map<String, AbsTaskEntity> map = new ConcurrentHashMap<>();

  public static TaskEntityManager getInstance() {
    if (INSTANCE == null) {
      synchronized (TaskEntityManager.class) {
        INSTANCE = new TaskEntityManager();
      }
    }
    return INSTANCE;
  }

  /**
   * 通过下载实体获取下载任务实体，如果查找不到任务实体，则重新创建任务实体
   */
  public DownloadTaskEntity getDTEntity(DownloadEntity entity) {
    AbsTaskEntity tEntity = map.get(convertKey(entity.getKey()));
    if (tEntity == null || !(tEntity instanceof DownloadTaskEntity)) {
      tEntity = DTEntityFactory.getInstance().create(entity);
      map.put(convertKey(entity.getKey()), tEntity);
    }
    return (DownloadTaskEntity) tEntity;
  }

  /**
   * 通过下载实体获取下载任务组实体，如果查找不到任务组实体，则重新创建任务组实体
   */
  public DownloadGroupTaskEntity getDGTEntity(DownloadGroupEntity entity) {
    AbsTaskEntity tEntity = map.get(convertKey(entity.getKey()));
    if (tEntity == null || !(tEntity instanceof DownloadGroupTaskEntity)) {
      tEntity = DGTEntityFactory.getInstance().create(entity);
      map.put(convertKey(entity.getKey()), tEntity);
    }
    return (DownloadGroupTaskEntity) tEntity;
  }

  /**
   * 通过下载实体获取下载任务组实体，如果查找不到任务组实体，则重新创建任务组实体
   */
  public UploadTaskEntity getUTEntity(UploadEntity entity) {
    AbsTaskEntity tEntity = map.get(convertKey(entity.getKey()));
    if (tEntity == null || !(tEntity instanceof UploadTaskEntity)) {
      tEntity = UTEntityFactory.getInstance().create(entity);
      map.put(convertKey(entity.getKey()), tEntity);
    }
    return (UploadTaskEntity) tEntity;
  }

  /**
   * 通过key删除任务实体
   */
  public AbsTaskEntity removeTEntity(String key) {
    return map.remove(convertKey(key));
  }

  private String convertKey(String key) {
    return CommonUtil.encryptBASE64(key);
  }
}
