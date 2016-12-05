package com.arialyy.downloadutil.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import com.arialyy.downloadutil.core.command.CmdFactory;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import com.arialyy.downloadutil.util.CommonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2016/12/1.
 * Aria管理器，任务操作在这里执行
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public class AriaManager {
  private static final Object LOCK = new Object();
  private static volatile AriaManager INSTANCE = null;
  private Map<String, AMReceiver> mTargets = new HashMap<>();
  private DownloadManager mManager;
  private LifeCallback mLifeCallback;

  private AriaManager(Context context) {
    regAppLifeCallback(context);
    mManager = DownloadManager.init(context);
  }

  static AriaManager getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new AriaManager(context);
      }
    }
    return INSTANCE;
  }

  AMReceiver get(Object obj) {
    return getTarget(obj);
  }

  /**
   * 停止所有正在执行的任务
   */
  public void stopAllTask() {
    List<DownloadEntity> allEntity = mManager.getAllDownloadEntity();
    List<IDownloadCmd> stopCmds = new ArrayList<>();
    for (DownloadEntity entity : allEntity) {
      if (entity.getState() == DownloadEntity.STATE_DOWNLOAD_ING) {
        stopCmds.add(CommonUtil.createCmd(entity, CmdFactory.TASK_STOP));
      }
    }
    mManager.setCmds(stopCmds).exe();
  }

  /**
   * 删除所有任务
   */
  public void cancelAllTask() {
    List<DownloadEntity> allEntity = mManager.getAllDownloadEntity();
    List<IDownloadCmd> cancelCmds = new ArrayList<>();
    for (DownloadEntity entity : allEntity) {
      cancelCmds.add(CommonUtil.createCmd(entity, CmdFactory.TASK_CANCEL));
    }
    mManager.setCmds(cancelCmds).exe();
    Set<String> keys = mTargets.keySet();
    for (String key : keys) {
      AMReceiver target = mTargets.get(key);
      target.removeSchedulerListener();
      mTargets.remove(key);
    }
  }

  private AMReceiver putTarget(Object obj) {
    String clsName = obj.getClass().getName();
    AMReceiver target = mTargets.get(clsName);
    if (target == null) {
      target = new AMReceiver();
      target.obj = obj;
      mTargets.put(clsName, target);
    }
    return target;
  }

  private AMReceiver getTarget(Object obj) {
    AMReceiver target = mTargets.get(obj.getClass().getName());
    if (target == null) {
      target = putTarget(obj);
    }
    return target;
  }

  /**
   * 注册APP生命周期回调
   */
  private void regAppLifeCallback(Context context) {
    Context app = context.getApplicationContext();
    if (app instanceof Application) {
      mLifeCallback = new LifeCallback();
      ((Application) app).registerActivityLifecycleCallbacks(mLifeCallback);
    }
  }

  /**
   * Activity生命周期
   */
  private class LifeCallback implements Application.ActivityLifecycleCallbacks {

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override public void onActivityStarted(Activity activity) {

    }

    @Override public void onActivityResumed(Activity activity) {

    }

    @Override public void onActivityPaused(Activity activity) {

    }

    @Override public void onActivityStopped(Activity activity) {

    }

    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override public void onActivityDestroyed(Activity activity) {
      Set<String> keys = mTargets.keySet();
      for (String key : keys) {
        if (key.equals(activity.getClass().getName())) {
          AMReceiver target = mTargets.get(key);
          if (target.obj != null) {
            if (target.obj instanceof Application || target.obj instanceof Service) break;
            target.removeSchedulerListener();
            mTargets.remove(key);
          }
          break;
        }
      }
    }
  }
}
