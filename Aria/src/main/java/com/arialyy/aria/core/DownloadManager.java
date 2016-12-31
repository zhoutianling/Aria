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

import android.content.Context;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.orm.DbUtil;
import com.arialyy.aria.core.command.IDownloadCmd;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.Configuration;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2016/8/11.
 * 下载管理器，通过命令的方式控制下载
 */
public class DownloadManager {
  private static final    String             TAG              = "DownloadManager";
  private static final    Object             LOCK             = new Object();
  private static volatile DownloadManager    INSTANCE         = null;
  private                 List<IDownloadCmd> mCommands        = new ArrayList<>();
  public static  Context       APP;
  private        ITaskQueue    mTaskQueue;
  private static Configuration mConfig;

  private DownloadManager() {

  }

  private DownloadManager(Context context) {
    APP = context;
    DownloadTaskQueue.Builder builder = new DownloadTaskQueue.Builder(context);
    mTaskQueue = builder.build();
    DbUtil.init(context);
  }

  static DownloadManager init(Context context) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadManager(context.getApplicationContext());
      }
    }
    return INSTANCE;
  }

  public static DownloadManager getInstance() {
    if (INSTANCE == null) {
      throw new NullPointerException("请在Application中调用init进行下载器注册");
    }
    return INSTANCE;
  }

  List<DownloadEntity> getAllDownloadEntity() {
    return DbEntity.findAllData(DownloadEntity.class);
  }

  /**
   * 获取任务队列
   */
  public ITaskQueue getTaskQueue() {
    return mTaskQueue;
  }

  /**
   * 设置命令
   */
  DownloadManager setCmd(IDownloadCmd command) {
    mCommands.add(command);
    return this;
  }

  /**
   * 设置一组命令
   */
  DownloadManager setCmds(List<IDownloadCmd> commands) {
    if (commands != null && commands.size() > 0) {
      mCommands.addAll(commands);
    }
    return this;
  }

  /**
   * 执行所有设置的命令
   */
  synchronized void exe() {
    for (IDownloadCmd command : mCommands) {
      command.executeCmd();
    }
    mCommands.clear();
  }
}