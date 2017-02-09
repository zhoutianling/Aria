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

package com.arialyy.aria.core.download.command;

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.queue.ITaskQueue;
import com.arialyy.aria.core.inf.ICmd;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class IDownloadCmd implements ICmd{
  ITaskQueue mQueue;
  DownloadTaskEntity mEntity;
  String TAG;
  String mTargetName;

  /**
   * @param entity 下载实体
   */
  IDownloadCmd(DownloadTaskEntity entity) {
    this(null, entity);
  }

  /**
   * @param targetName 产生任务的对象名
   */
  IDownloadCmd(String targetName, DownloadTaskEntity entity) {
    CheckUtil.checkDownloadEntity(entity.downloadEntity);
    mTargetName = targetName;
    mEntity = entity;
    TAG = CommonUtil.getClassName(this);
    mQueue = AriaManager.getInstance(AriaManager.APP).getTaskQueue();
  }

}