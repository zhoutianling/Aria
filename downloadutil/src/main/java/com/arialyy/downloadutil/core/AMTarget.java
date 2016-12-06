package com.arialyy.downloadutil.core;

import android.content.Context;
import com.arialyy.downloadutil.core.command.CmdFactory;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import com.arialyy.downloadutil.core.scheduler.OnSchedulerListener;
import com.arialyy.downloadutil.core.task.Task;
import com.arialyy.downloadutil.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by utstarcom on 2016/12/5.
 */
public class AMTarget {
  private AMReceiver receiver;

  public AMTarget(AMReceiver receiver) {
    this.receiver = receiver;
  }

  /**
   * 开始下载
   */
  public void start() {
    List<IDownloadCmd> cmds = new ArrayList<>();
    cmds.add(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_CREATE));
    cmds.add(CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_START));
    receiver.manager.setCmds(cmds).exe();
  }

  /**
   * 停止下载
   */
  public void stop() {
    receiver.manager.setCmd(
        CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_STOP)).exe();
  }

  /**
   * 恢复下载
   */
  public void resume() {
    receiver.manager.setCmd(
        CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_START)).exe();
  }

  /**
   * 取消下载
   */
  public void cancel() {
    receiver.manager.setCmd(
        CommonUtil.createCmd(receiver.obj, receiver.entity, CmdFactory.TASK_CANCEL)).exe();
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
