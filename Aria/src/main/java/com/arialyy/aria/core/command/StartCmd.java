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

import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.core.task.Task;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
class StartCmd extends IDownloadCmd {

  StartCmd(Object target, DownloadEntity entity) {
    super(target, entity);
  }

  StartCmd(DownloadEntity entity) {
    super(entity);
  }

  @Override public void executeCmd() {
    Task task = mQueue.getTask(mEntity);
    if (task == null) {
      task = mQueue.createTask(mTarget, mEntity);
    }
    if (task != null) {
      task.setmTargetName(mTarget.getClass().getName());
      mQueue.startTask(task);
    }
  }
}