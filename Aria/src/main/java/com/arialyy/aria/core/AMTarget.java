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
package com.arialyy.aria.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.arialyy.aria.core.command.CmdFactory;
import com.arialyy.aria.core.command.IDownloadCmd;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class AMTarget {
  private AMReceiver mReceiver;

  AMTarget(AMReceiver receiver) {
    this.mReceiver = receiver;
  }

  /**
   * 设置文件存储路径
   */
  public AMTarget setDownloadPath(@NonNull String downloadPath) {
    if (TextUtils.isEmpty(downloadPath)) {
      throw new IllegalArgumentException("文件保持路径不能为null");
    }
    mReceiver.entity.setDownloadPath(downloadPath);
    return this;
  }

  /**
   * 设置文件名
   */
  public AMTarget setDownloadName(@NonNull String downloadName) {
    if (TextUtils.isEmpty(downloadName)) {
      throw new IllegalArgumentException("文件名不能为null");
    }
    mReceiver.entity.setFileName(downloadName);
    return this;
  }

  /**
   * 获取下载文件大小
   */
  public long getFileSize() {
    DownloadEntity entity = getDownloadEntity(mReceiver.entity.getDownloadUrl());
    if (entity == null) {
      throw new NullPointerException("下载管理器中没有改任务");
    }
    return entity.getFileSize();
  }

  /**
   * 获取当前下载进度，如果下載实体存在，则返回当前进度
   */
  public long getCurrentProgress() {
    DownloadEntity entity = getDownloadEntity(mReceiver.entity.getDownloadUrl());
    if (entity == null) {
      throw new NullPointerException("下载管理器中没有改任务");
    }
    return entity.getCurrentProgress();
  }

  private DownloadEntity getDownloadEntity(String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    return DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
  }

  /**
   * 添加任务
   */
  public void add() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(mReceiver.obj, mReceiver.entity, CmdFactory.TASK_CREATE))
        .exe();
  }

  /**
   * 开始下载
   */
  public void start() {
    List<IDownloadCmd> cmds = new ArrayList<>();
    cmds.add(CommonUtil.createCmd(mReceiver.obj, mReceiver.entity, CmdFactory.TASK_CREATE));
    cmds.add(CommonUtil.createCmd(mReceiver.obj, mReceiver.entity, CmdFactory.TASK_START));
    DownloadManager.getInstance().setCmds(cmds).exe();
    cmds.clear();
  }

  /**
   * 停止下载
   */
  public void stop() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(mReceiver.obj, mReceiver.entity, CmdFactory.TASK_STOP))
        .exe();
  }

  /**
   * 恢复下载
   */
  public void resume() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(mReceiver.obj, mReceiver.entity, CmdFactory.TASK_START))
        .exe();
  }

  /**
   * 取消下载
   */
  public void cancel() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(mReceiver.obj, mReceiver.entity, CmdFactory.TASK_CANCEL))
        .exe();
  }

  /**
   * 是否在下载
   */
  public boolean isDownloading() {
    return DownloadManager.getInstance().getTaskQueue().getTask(mReceiver.entity).isDownloading();
  }

  /**
   * 重新下载
   */
  public void reStart() {
    cancel();
    start();
  }
}
