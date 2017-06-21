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

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.QueueMod;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.AbsTaskEntity;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
class StartCmd<T extends AbsTaskEntity> extends AbsCmd<T> {

  StartCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    if (!canExeCmd) return;
    ITask task = mQueue.getTask(mEntity.getEntity());
    if (task == null) {
      task = mQueue.createTask(mTargetName, mEntity);
    }
    if (task != null) {
      if (!TextUtils.isEmpty(mTargetName)) {
        task.setTargetName(mTargetName);
      }
      String mod;
      int maxTaskNum;
      AriaManager manager = AriaManager.getInstance(AriaManager.APP);
      if (isDownloadCmd) {
        mod = manager.getDownloadConfig().getQueueMod();
        maxTaskNum = manager.getDownloadConfig().getMaxTaskNum();
      } else {
        mod = manager.getUploadConfig().getQueueMod();
        maxTaskNum = manager.getUploadConfig().getMaxTaskNum();
      }
      if (mod.equals(QueueMod.NOW.getTag())) {
        mQueue.startTask(task);
      }else if (mod.equals(QueueMod.WAIT.getTag())){
        if (mQueue.getExeTaskNum() < maxTaskNum){
          mQueue.startTask(task);
        }
      }
    }
  }
}