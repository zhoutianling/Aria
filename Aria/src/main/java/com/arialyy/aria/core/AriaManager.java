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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.PopupWindow;
import com.arialyy.aria.core.command.CmdFactory;
import com.arialyy.aria.util.CAConfiguration;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.core.command.IDownloadCmd;
import com.arialyy.aria.util.Configuration;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2016/12/1.
 * https://github.com/AriaLyy/Aria
 * Aria管理器，任务操作在这里执行
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public class AriaManager {
  private static final String TAG = "AriaManager";
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
   * 设置CA证书信息
   *
   * @param caAlias ca证书别名
   * @param caPath assets 文件夹下的ca证书完整路径
   */
  public void setCAInfo(String caAlias, String caPath) {
    if (TextUtils.isEmpty(caAlias)) {
      Log.e(TAG, "ca证书别名不能为null");
      return;
    } else if (TextUtils.isEmpty(caPath)) {
      Log.e(TAG, "ca证书路径不能为null");
      return;
    }
    CAConfiguration.CA_ALIAS = caAlias;
    CAConfiguration.CA_PATH = caPath;
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
   * 设置下载超时时间
   */
  @Deprecated private AriaManager setTimeOut(int timeOut) {
    Configuration.getInstance().setTimeOut(timeOut);
    return this;
  }

  /**
   * 设置下载失败重试次数
   */
  public AriaManager setReTryNum(int reTryNum) {
    Configuration.getInstance().setReTryNum(reTryNum);
    return this;
  }

  /**
   * 设置下载失败重试间隔
   */
  public AriaManager setReTryInterval(int interval) {
    Configuration.getInstance().setReTryInterval(interval);
    return this;
  }

  /**
   * 是否打开下载广播
   */
  public AriaManager openBroadcast(boolean open) {
    Configuration.getInstance().setOpenBroadcast(open);
    return this;
  }

  /**
   * 设置最大下载数，最大下载数不能小于1
   *
   * @param maxDownloadNum 最大下载数
   */
  public AriaManager setMaxDownloadNum(int maxDownloadNum) {
    if (maxDownloadNum < 1) {
      Log.w(TAG, "最大任务数不能小于 1");
      return this;
    }
    mManager.getTaskQueue().setDownloadNum(maxDownloadNum);
    return this;
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
    AMReceiver target = null;
    String key = "";
    if (!(obj instanceof Activity)) {
      if (obj instanceof android.support.v4.app.Fragment) {
        key = clsName + "_" + ((Fragment) obj).getActivity().getClass().getName();
      } else if (obj instanceof android.app.Fragment) {
        key = clsName + "_" + ((android.app.Fragment) obj).getActivity().getClass().getName();
      } else if (obj instanceof Dialog) {
        Activity activity = ((Dialog) obj).getOwnerActivity();
        if (activity != null) {
          key = clsName + "_" + activity.getClass().getName();
        } else {
          key = clsName;
        }
        handleDialogLift((Dialog) obj);
      } else if (obj instanceof PopupWindow) {
        Context context = ((PopupWindow) obj).getContentView().getContext();
        if (context instanceof Activity) {
          key = clsName + "_" + context.getClass().getName();
        } else {
          key = clsName;
        }
        handlePopupWindowLift((PopupWindow) obj);
      } else if (obj instanceof Service) {
        key = clsName;
      } else if (obj instanceof Application) {
        key = clsName;
      }
    } else {
      key = clsName;
    }
    if (TextUtils.isEmpty(key)) {
      throw new IllegalArgumentException("未知类型");
    }
    target = mTargets.get(key);
    if (target == null) {
      target = new AMReceiver();
      target.targetName = obj.getClass().getName();
      mTargets.put(key, target);
    }
    return target;
  }

  /**
   * 出来悬浮框取消或dismiss
   */
  private void handlePopupWindowLift(PopupWindow popupWindow) {
    try {
      Field dismissField = CommonUtil.getField(popupWindow.getClass(), "mOnDismissListener");
      PopupWindow.OnDismissListener listener =
          (PopupWindow.OnDismissListener) dismissField.get(popupWindow);
      if (listener != null) {
        Log.e(TAG, "你已经对PopupWindow设置了Dismiss事件。为了防止内存泄露，"
            + "请在dismiss方法中调用Aria.whit(this).removeSchedulerListener();来注销事件");
      } else {
        popupWindow.setOnDismissListener(createPopupWindowListener(popupWindow));
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * 创建popupWindow dismiss事件
   */
  private PopupWindow.OnDismissListener createPopupWindowListener(final PopupWindow popupWindow) {
    return new PopupWindow.OnDismissListener() {
      @Override public void onDismiss() {
        destroySchedulerListener(popupWindow);
      }
    };
  }

  /**
   * 处理对话框取消或dismiss
   */
  private void handleDialogLift(Dialog dialog) {
    try {
      Field dismissField = CommonUtil.getField(dialog.getClass(), "mDismissMessage");
      Message dismissMsg = (Message) dismissField.get(dialog);
      //如果Dialog已经设置Dismiss事件，则查找cancel事件
      if (dismissMsg != null) {
        Field cancelField = CommonUtil.getField(dialog.getClass(), "mCancelMessage");
        Message cancelMsg = (Message) cancelField.get(dialog);
        if (cancelMsg != null) {
          Log.e(TAG, "你已经对Dialog设置了Dismiss和cancel事件。为了防止内存泄露，"
              + "请在dismiss方法中调用Aria.whit(this).removeSchedulerListener();来注销事件");
        } else {
          dialog.setOnCancelListener(createCancelListener());
        }
      } else {
        dialog.setOnDismissListener(createDismissListener());
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
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
   * 创建Dialog取消事件
   */
  private Dialog.OnCancelListener createCancelListener() {
    return new Dialog.OnCancelListener() {

      @Override public void onCancel(DialogInterface dialog) {
        destroySchedulerListener(dialog);
      }
    };
  }

  /**
   * 创建Dialog dismiss取消事件
   */
  private Dialog.OnDismissListener createDismissListener() {
    return new Dialog.OnDismissListener() {

      @Override public void onDismiss(DialogInterface dialog) {
        destroySchedulerListener(dialog);
      }
    };
  }

  /**
   * onDestroy
   */
  private void destroySchedulerListener(Object obj) {
    Set<String> keys = mTargets.keySet();
    String clsName = obj.getClass().getName();
    for (Iterator<Map.Entry<String, AMReceiver>> iter = mTargets.entrySet().iterator();
        iter.hasNext(); ) {
      Map.Entry<String, AMReceiver> entry = iter.next();
      String key = entry.getKey();
      if (key.equals(clsName) || key.contains(clsName)) {
        AMReceiver receiver = mTargets.get(key);
        if (receiver.obj != null) {
          if (receiver.obj instanceof Application || receiver.obj instanceof Service) break;
        }
        receiver.removeSchedulerListener();
        receiver.destroy();
        iter.remove();
        break;
      }
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
      destroySchedulerListener(activity);
    }
  }
}
