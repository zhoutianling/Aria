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

import android.util.Log;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.task.DownloadTask;

/**
 * Created by lyy on 2016/8/22.
 * 添加任务的命令
 */
class AddCmd extends IDownloadCmd {

  AddCmd(DownloadTaskEntity entity) {
    super(entity);
  }

  AddCmd(String targetName, DownloadTaskEntity entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    DownloadTask task = mQueue.getTask(mEntity.downloadEntity);
    if (task == null) {
      mQueue.createTask(mTargetName, mEntity);
    } else {
      Log.w(TAG, "添加命令执行失败，【该任务已经存在】");
    }
  }
}