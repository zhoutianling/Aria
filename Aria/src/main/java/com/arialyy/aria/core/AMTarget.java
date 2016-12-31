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

import com.arialyy.aria.core.command.CmdFactory;
import com.arialyy.aria.core.scheduler.OnSchedulerListener;
import com.arialyy.aria.core.command.IDownloadCmd;
import com.arialyy.aria.core.task.Task;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class AMTarget {
  private AMReceiver receiver;

  AMTarget(AMReceiver receiver) {
    this.receiver = receiver;
  }

  /**
   * 添加任务
   */
  public void add() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_CREATE))
        .exe();
  }

  /**
   * 开始下载
   */
  public void start() {
    List<IDownloadCmd> cmds = new ArrayList<>();
    cmds.add(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_CREATE));
    cmds.add(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_START));
    DownloadManager.getInstance().setCmds(cmds).exe();
    cmds.clear();
  }

  /**
   * 停止下载
   */
  public void stop() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_STOP))
        .exe();
  }

  /**
   * 恢复下载
   */
  public void resume() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_START))
        .exe();
  }

  /**
   * 取消下载
   */
  public void cancel() {
    DownloadManager.getInstance()
        .setCmd(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_CANCEL))
        .exe();
  }

  /**
   * 是否在下载
   */
  public boolean isDownloading() {
    return DownloadManager.getInstance().getTaskQueue().getTask(receiver.entity).isDownloading();
  }

  /**
   * 重新下载
   */
  public void reStart() {
    cancel();
    start();
  }

  public static class SimpleSchedulerListener implements OnSchedulerListener {

    @Override public void onTaskPre(Task task) {

    }

    @Override public void onTaskResume(Task task) {

    }

    @Override public void onTaskStart(Task task) {

    }

    @Override public void onTaskStop(Task task) {

    }

    @Override public void onTaskCancel(Task task) {

    }

    @Override public void onTaskFail(Task task) {

    }

    @Override public void onTaskComplete(Task task) {

    }

    @Override public void onTaskRunning(Task task) {

    }
  }
}
