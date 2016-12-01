/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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

package com.arialyy.downloadutil.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import com.arialyy.downloadutil.core.command.CmdFactory;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import com.arialyy.downloadutil.core.scheduler.OnSchedulerListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2016/12/1.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class Aria {
  private static final Object LOCK = new Object();
  private static volatile Aria INSTANCE = null;
  private DownloadManager mDownloadManager;

  private Aria() {
    //mDownloadManager = DownloadManager.getInstance();
  }

  private Aria(Context context) {
    mDownloadManager = DownloadManager.init(context);
  }

  //public static AriaManager whit(Context context) {
  //  return AriaManager.getInstance().get(context);
  //}

  /**
   * 开始下载
   */
  public Aria start(DownloadEntity entity) {
    List<IDownloadCmd> cmds = new ArrayList<>();
    cmds.add(createCmd(entity, CmdFactory.TASK_CREATE));
    cmds.add(createCmd(entity, CmdFactory.TASK_START));
    mDownloadManager.setCmds(cmds).exe();
    return this;
  }

  /**
   * 停止下载
   */
  public void stop(DownloadEntity entity) {
    mDownloadManager.setCmd(createCmd(entity, CmdFactory.TASK_STOP)).exe();
  }

  /**
   * 恢复下载
   */
  public void resume(DownloadEntity entity) {
    mDownloadManager.setCmd(createCmd(entity, CmdFactory.TASK_START)).exe();
  }

  /**
   * 取消下载
   */
  public void cancel(DownloadEntity entity) {
    mDownloadManager.setCmd(createCmd(entity, CmdFactory.TASK_CANCEL)).exe();
  }

  /**
   * 添加调度器回调
   */
  public Aria addSchedulerListener(Context context, OnSchedulerListener listener) {
    //mDownloadManager.getTaskQueue().getDownloadSchedulers().addSchedulerListener(listener);
    return this;
  }

  /**
   * 移除回调
   */
  public Aria removeSchedulerListener(Context context) {
    //mDownloadManager.getTaskQueue().getDownloadSchedulers().removeSchedulerListener(listener);
    return this;
  }

  private IDownloadCmd createCmd(DownloadEntity entity, int cmd) {
    return CmdFactory.getInstance().createCmd(entity, cmd);
  }
}
