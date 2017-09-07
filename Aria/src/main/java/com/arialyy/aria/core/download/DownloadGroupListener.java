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

import android.os.Handler;
import com.arialyy.aria.core.download.downloader.IDownloadGroupListener;
import com.arialyy.aria.core.scheduler.ISchedulers;

/**
 * Created by Aria.Lao on 2017/7/20.
 * 任务组下载事件
 */
class DownloadGroupListener extends BaseDListener<DownloadGroupEntity, DownloadGroupTask>
    implements IDownloadGroupListener {
  private final String TAG = "DownloadGroupListener";

  DownloadGroupListener(DownloadGroupTask task, Handler outHandler) {
    super(task, outHandler);
  }

  @Override public void onSubPre(DownloadEntity subEntity) {
    sendInState2Target(ISchedulers.SUB_PRE);
  }

  @Override public void supportBreakpoint(boolean support, DownloadEntity subEntity) {

  }

  @Override public void onSubStart(DownloadEntity subEntity) {
    sendInState2Target(ISchedulers.SUB_START);
  }

  @Override public void onSubStop(DownloadEntity subEntity) {
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_STOP);
  }

  @Override public void onSubComplete(DownloadEntity subEntity) {
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_COMPLETE);
  }

  @Override public void onSubFail(DownloadEntity subEntity) {
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_FAIL);
  }

  @Override public void onSubCancel(DownloadEntity entity) {
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_CANCEL);
  }

  @Override public void onSubRunning(DownloadEntity subEntity) {
    sendInState2Target(ISchedulers.SUB_RUNNING);
  }

  /**
   * 将任务状态发送给下载器
   *
   * @param state {@link ISchedulers#START}
   */
  private void sendInState2Target(int state) {
    if (outHandler.get() != null) {
      outHandler.get().obtainMessage(state, ISchedulers.IS_SUB_TASK, 0, mTask).sendToTarget();
    }
  }

  private void saveCurrentLocation() {
    long location = 0;
    for (DownloadEntity e : mEntity.getSubTask()) {
      location += e.getCurrentProgress();
    }
    mEntity.setCurrentProgress(location);
    mEntity.update();
  }
}
