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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.PopupWindow;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadReceiver;
import com.arialyy.aria.core.inf.ICmd;
import com.arialyy.aria.core.inf.IReceiver;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.core.queue.UploadTaskQueue;
import com.arialyy.aria.core.upload.UploadReceiver;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.DbUtil;
import com.arialyy.aria.util.CAConfiguration;
import com.arialyy.aria.util.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lyy on 2016/12/1.
 * https://github.com/AriaLyy/Aria
 * Aria管理器，任务操作在这里执行
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public class AriaManager {
  private static final String TAG = "AriaManager";
  private static final String DOWNLOAD = "_download";
  private static final String UPLOAD = "_upload";
  private static final Object LOCK = new Object();
  @SuppressLint("StaticFieldLeak") private static volatile AriaManager INSTANCE = null;
  private Map<String, IReceiver> mReceivers = new HashMap<>();

  public static Context APP;
  private List<ICmd> mCommands = new ArrayList<>();

  private AriaManager(Context context) {
    DbUtil.init(context.getApplicationContext());
    APP = context;
    regAppLifeCallback(context);
  }

  public static AriaManager getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new AriaManager(context);
      }
    }
    return INSTANCE;
  }

  public Map<String, IReceiver> getReceiver() {
    return mReceivers;
  }

  public List<DownloadEntity> getAllDownloadEntity() {
    return DbEntity.findAllData(DownloadEntity.class);
  }

  /**
   * 设置命令
   */
  public AriaManager setCmd(ICmd command) {
    mCommands.add(command);
    return this;
  }

  /**
   * 设置一组命令
   */
  public <T extends ICmd> AriaManager setCmds(List<T> commands) {
    if (commands != null && commands.size() > 0) {
      mCommands.addAll(commands);
    }
    return this;
  }

  /**
   * 执行所有设置的命令
   */
  public synchronized void exe() {
    for (ICmd command : mCommands) {
      command.executeCmd();
    }
    mCommands.clear();
  }

  /**
   * 处理下载操作
   */
  DownloadReceiver download(Object obj) {
    IReceiver receiver = mReceivers.get(getKey(true, obj));
    if (receiver == null) {
      receiver = putReceiver(true, obj);
    }
    return (receiver instanceof DownloadReceiver) ? (DownloadReceiver) receiver : null;
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
   * 设置下载超时时间
   */
  @Deprecated private AriaManager setTimeOut(int timeOut) {
    Configuration.getInstance().setTimeOut(timeOut);
    return this;
  }

  /**
   * 设置失败重试次数
   */
  public AriaManager setReTryNum(int reTryNum) {
    Configuration.getInstance().setReTryNum(reTryNum);
    return this;
  }

  /**
   * 设置失败重试间隔
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
    DownloadTaskQueue.getInstance().setDownloadNum(maxDownloadNum);
    return this;
  }

  private IReceiver putReceiver(boolean isDownload, Object obj) {
    final String key = getKey(isDownload, obj);
    IReceiver receiver = mReceivers.get(key);
    final WidgetLiftManager widgetLiftManager = new WidgetLiftManager();
    if (obj instanceof Dialog) {
      widgetLiftManager.handleDialogLift((Dialog) obj);
    } else if (obj instanceof PopupWindow) {
      widgetLiftManager.handlePopupWindowLift((PopupWindow) obj);
    }

    if (receiver == null) {
      if (isDownload) {
        DownloadReceiver dReceiver = new DownloadReceiver();
        dReceiver.targetName = obj.getClass().getName();
        mReceivers.put(key, dReceiver);
        receiver = dReceiver;
      } else {
        UploadReceiver uReceiver = new UploadReceiver();

        receiver = uReceiver;
      }
    }
    return receiver;
  }

  /**
   * 根据功能类型和控件类型获取对应的key
   */
  private String getKey(boolean isDownload, Object obj) {
    String clsName = obj.getClass().getName();
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
      } else if (obj instanceof PopupWindow) {
        Context context = ((PopupWindow) obj).getContentView().getContext();
        if (context instanceof Activity) {
          key = clsName + "_" + context.getClass().getName();
        } else {
          key = clsName;
        }
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
    key += isDownload ? DOWNLOAD : UPLOAD;
    return key;
  }

  /**
   * 注册APP生命周期回调
   */
  private void regAppLifeCallback(Context context) {
    Context app = context.getApplicationContext();
    if (app instanceof Application) {
      LifeCallback mLifeCallback = new LifeCallback();
      ((Application) app).registerActivityLifecycleCallbacks(mLifeCallback);
    }
  }

  /**
   * onDestroy
   */
  void destroySchedulerListener(Object obj) {
    String clsName = obj.getClass().getName();
    for (Iterator<Map.Entry<String, IReceiver>> iter = mReceivers.entrySet().iterator();
        iter.hasNext(); ) {
      Map.Entry<String, IReceiver> entry = iter.next();
      String key = entry.getKey();
      if (key.contains(clsName)) {
        IReceiver receiver = mReceivers.get(key);
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
