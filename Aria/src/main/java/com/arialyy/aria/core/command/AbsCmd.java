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

import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.core.inf.ICmd;
import com.arialyy.aria.core.queue.UploadTaskQueue;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class AbsCmd<T extends AbsTaskEntity> implements ICmd {
  ITaskQueue mQueue;
  T mEntity;
  String TAG;
  String mTargetName;
  /**
   * 能否执行命令
   */
  boolean canExeCmd = true;

  /**
   * @param targetName 产生任务的对象名
   */
  AbsCmd(String targetName, T entity) {
    canExeCmd = CheckUtil.checkCmdEntity(entity,
        !(this instanceof CancelCmd) || !(this instanceof StopCmd));
    mTargetName = targetName;
    mEntity = entity;
    TAG = CommonUtil.getClassName(this);
    if (entity instanceof DownloadTaskEntity) {
      mQueue = DownloadTaskQueue.getInstance();
    } else if (entity instanceof UploadTaskEntity) {
      mQueue = UploadTaskQueue.getInstance();
    }
  }
}