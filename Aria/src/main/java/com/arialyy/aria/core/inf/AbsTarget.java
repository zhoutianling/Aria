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
package com.arialyy.aria.core.inf;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.RequestEnum;
import com.arialyy.aria.core.command.AbsCmd;
import com.arialyy.aria.core.command.CmdFactory;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Aria.Lao on 2017/2/28.
 */
public class AbsTarget<ENTITY extends IEntity, TASK_ENTITY extends ITaskEntity> {
  protected ENTITY entity;
  protected TASK_ENTITY taskEntity;
  protected String targetName;

  /**
   * 获取任务文件大小
   *
   * @return -1，没有找到该任务
   */
  public long getFileSize() {
    if (entity instanceof DownloadEntity) {
      DownloadEntity entity = DbEntity.findData(DownloadEntity.class, "downloadUrl=?",
          ((DownloadEntity) this.entity).getDownloadUrl());
      if (entity == null) {
        throw new NullPointerException("没有找到该任务");
      }
      return entity.getFileSize();
    } else if (entity instanceof UploadEntity) {
      UploadEntity entity = DbEntity.findData(UploadEntity.class, "filePath=?",
          ((UploadEntity) this.entity).getFilePath());
      if (entity == null) {
        throw new NullPointerException("没有找到该任务");
      }
      return entity.getFileSize();
    }
    return -1;
  }

  /**
   * 获取当前任务进度，如果任务存在，则返回当前进度
   *
   * @return -1，没有找到该任务
   */
  public long getCurrentProgress() {
    if (entity instanceof DownloadEntity) {
      DownloadEntity entity = DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?",
          ((DownloadEntity) this.entity).getDownloadUrl());
      if (entity == null) {
        throw new NullPointerException("下载管理器中没有该任务");
      }
      return entity.getCurrentProgress();
    } else if (entity instanceof UploadEntity) {
      UploadEntity entity = DbEntity.findData(UploadEntity.class, "filePath=?",
          ((UploadEntity) this.entity).getFilePath());
      if (entity == null) {
        throw new NullPointerException("没有找到该任务");
      }
      return entity.getCurrentProgress();
    }
    return -1;
  }

  /**
   * 给url请求添加头部
   *
   * @param key 头部key
   * @param header 头部value
   */
  protected void _addHeader(@NonNull String key, @NonNull String header) {
    taskEntity.headers.put(key, header);
  }

  /**
   * 给url请求添加头部
   *
   * @param headers Map<Key, Value>
   */
  protected void _addHeaders(Map<String, String> headers) {
    if (headers != null && headers.size() > 0) {
      Set<String> keys = headers.keySet();
      for (String key : keys) {
        taskEntity.headers.put(key, headers.get(key));
      }
    }
  }

  /**
   * 设置请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  protected void _setRequestMode(RequestEnum requestEnum) {
    taskEntity.requestEnum = requestEnum;
  }

  /**
   * 添加任务
   */
  public void add() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_CREATE))
        .exe();
  }

  /**
   * 开始下载
   */
  public void start() {
    List<AbsCmd> cmds = new ArrayList<>();
    cmds.add(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_CREATE));
    cmds.add(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_START));
    AriaManager.getInstance(AriaManager.APP).setCmds(cmds).exe();
    cmds.clear();
  }

  /**
   * 停止下载
   */
  protected void pause() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_STOP))
        .exe();
  }

  /**
   * 恢复下载
   */
  protected void resume() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_START))
        .exe();
  }

  /**
   * 取消下载
   */
  public void cancel() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_CANCEL))
        .exe();
  }

  /**
   * 重新下载
   */
  void reStart() {
    cancel();
    start();
  }
}
