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

import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.ITaskEntity;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
class StartCmd<T extends ITaskEntity> extends AbsCmd<T> {

  StartCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    ITask task = mQueue.getTask(mEntity.getEntity());
    if (task == null) {
      task = mQueue.createTask(mTargetName, mEntity);
    }
    if (task != null) {
      task.setTargetName(mTargetName);
      mQueue.startTask(task);
    }
  }

  //StartCmd(DownloadTaskEntity entity) {
  //  super(entity);
  //}
  //
  //StartCmd(String targetName, DownloadTaskEntity entity) {
  //  super(targetName, entity);
  //}
  //
  //@Override public void executeCmd() {
  //  DownloadTask task = mQueue.getTask(mEntity.downloadEntity);
  //  if (task == null) {
  //    task = mQueue.createTask(mTargetName, mEntity);
  //  }
  //  if (task != null) {
  //    task.setTargetName(mTargetName);
  //    mQueue.startTask(task);
  //  }
  //}
}