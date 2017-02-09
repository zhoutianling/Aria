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

import android.support.annotation.NonNull;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.IReceiver;
import com.arialyy.aria.core.download.command.CmdFactory;
import com.arialyy.aria.core.download.command.IDownloadCmd;
import com.arialyy.aria.core.download.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.download.scheduler.OnSchedulerListener;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2016/12/5.
 * 下载功能接收器
 */
public class DownloadReceiver implements IReceiver {
  private static final String TAG = "DownloadReceiver";
  public String targetName;
  public OnSchedulerListener listener;

  /**
   * {@link #load(String)}，请使用该方法
   */
  @Deprecated public DownloadTarget load(DownloadEntity entity) {
    return new DownloadTarget(entity, targetName);
  }

  /**
   * 读取下载链接
   */
  public DownloadTarget load(@NonNull String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    DownloadEntity entity =
        DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
    if (entity == null) {
      entity = new DownloadEntity();
    }
    entity.setDownloadUrl(downloadUrl);
    return new DownloadTarget(entity, targetName);
  }

  /**
   * 添加调度器回调
   */
  public DownloadReceiver addSchedulerListener(OnSchedulerListener listener) {
    this.listener = listener;
    DownloadSchedulers.getInstance().addSchedulerListener(targetName, listener);
    return this;
  }

  /**
   * 移除回调
   */
  @Override
  public void removeSchedulerListener() {
    if (listener != null) {
      DownloadSchedulers.getInstance().removeSchedulerListener(targetName, listener);
    }
  }

  @Override
  public void destroy() {
    targetName = null;
    listener = null;
  }

  /**
   * 获取下载列表
   */
  public List<DownloadEntity> getDownloadList() {
    return DownloadEntity.findAllData(DownloadEntity.class);
  }

  /**
   * 通过下载链接获取下载实体
   */
  public DownloadEntity getDownloadEntity(String downloadUrl) {
    CheckUtil.checkDownloadUrl(downloadUrl);
    return DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
  }

  /**
   * 下载任务是否存在
   */
  public boolean taskExists(String downloadUrl) {
    return DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", downloadUrl) != null;
  }

  /**
   * 停止所有正在下载的任务
   */
  public void stopAllTask() {
    final AriaManager ariaManager = AriaManager.getInstance(AriaManager.APP);
    List<DownloadEntity> allEntity = ariaManager.getAllDownloadEntity();
    List<IDownloadCmd> stopCmds = new ArrayList<>();
    for (DownloadEntity entity : allEntity) {
      if (entity.getState() == DownloadEntity.STATE_DOWNLOAD_ING) {
        stopCmds.add(
            CommonUtil.createDownloadCmd(new DownloadTaskEntity(entity), CmdFactory.TASK_STOP));
      }
    }
    ariaManager.setCmds(stopCmds).exe();
  }

  /**
   * 删除所有任务
   */
  public void cancelAllTask() {
    final AriaManager ariaManager = AriaManager.getInstance(AriaManager.APP);
    List<DownloadEntity> allEntity = ariaManager.getAllDownloadEntity();
    List<IDownloadCmd> cancelCmds = new ArrayList<>();
    for (DownloadEntity entity : allEntity) {
      cancelCmds.add(
          CommonUtil.createDownloadCmd(new DownloadTaskEntity(entity), CmdFactory.TASK_CANCEL));
    }
    ariaManager.setCmds(cancelCmds).exe();
    Set<String> keys = ariaManager.getReceiver().keySet();
    for (String key : keys) {
      IReceiver receiver = ariaManager.getReceiver().get(key);
      receiver.removeSchedulerListener();
      ariaManager.getReceiver().remove(key);
    }
  }
}