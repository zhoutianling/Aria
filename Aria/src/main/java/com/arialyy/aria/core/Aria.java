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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.widget.PopupWindow;
import com.arialyy.aria.core.download.DownloadReceiver;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.core.scheduler.IDownloadSchedulerListener;
import com.arialyy.aria.core.scheduler.ISchedulerListener;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.upload.UploadReceiver;
import com.arialyy.aria.core.upload.UploadTask;

/**
 * Created by lyy on 2016/12/1.
 * https://github.com/AriaLyy/Aria
 * Aria启动，管理全局任务
 * <pre>
 *   <code>
 *   //下载
 *   Aria.download(this)
 *       .load(DOWNLOAD_URL)     //下载地址，必填
 *       //文件保存路径，必填
 *       .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")
 *       .start();
 *   </code>
 *   <code>
 *    //上传
 *    Aria.upload(this)
 *        .load(filePath)     //文件路径，必填
 *        .setUploadUrl(uploadUrl)  //上传路径，必填
 *        .setAttachment(fileKey)   //服务器读取文件的key，必填
 *        .start();
 *   </code>
 * </pre>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) public class Aria {
  /**
   * 不支持断点
   */
  public static final String ACTION_SUPPORT_BREAK_POINT = "ACTION_SUPPORT_BREAK_POINT";
  /**
   * 预处理完成
   */
  public static final String ACTION_PRE = "ACTION_PRE";
  /**
   * 下载开始前事件
   */
  public static final String ACTION_POST_PRE = "ACTION_POST_PRE";
  /**
   * 开始下载事件
   */
  public static final String ACTION_START = "ACTION_START";
  /**
   * 恢复下载事件
   */
  public static final String ACTION_RESUME = "ACTION_RESUME";
  /**
   * 正在下载事件
   */
  public static final String ACTION_RUNNING = "ACTION_RUNNING";
  /**
   * 停止下载事件
   */
  public static final String ACTION_STOP = "ACTION_STOP";
  /**
   * 取消下载事件
   */
  public static final String ACTION_CANCEL = "ACTION_CANCEL";
  /**
   * 下载完成事件
   */
  public static final String ACTION_COMPLETE = "ACTION_COMPLETE";
  /**
   * 下载失败事件
   */
  public static final String ACTION_FAIL = "ACTION_FAIL";
  /**
   * 下载实体
   */
  public static final String ENTITY = "DOWNLOAD_ENTITY";
  /**
   * 位置
   */
  public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
  /**
   * 速度
   */
  public static final String CURRENT_SPEED = "CURRENT_SPEED";

  private Aria() {
  }

  /**
   * 初始化下载
   *
   * @param obj 支持类型有【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
   */
  public static DownloadReceiver download(Object obj) {
    return get(obj).download(obj);
  }

  /**
   * 初始化上传
   *
   * @param obj 支持类型有【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
   */
  public static UploadReceiver upload(Object obj) {
    return get(obj).upload(obj);
  }

  /**
   * 处理通用事件
   *
   * @param obj 支持类型有【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
   */
  public static AriaManager get(Object obj) {
    if (obj instanceof Activity || obj instanceof Service || obj instanceof Application) {
      return AriaManager.getInstance((Context) obj);
    } else if (obj instanceof DialogFragment) {
      DialogFragment dialog = (DialogFragment) obj;
      return AriaManager.getInstance(
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? dialog.getContext()
              : dialog.getActivity());
    } else if (obj instanceof android.support.v4.app.Fragment) {
      android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) obj;
      return AriaManager.getInstance(
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? fragment.getContext()
              : fragment.getActivity());
    } else if (obj instanceof Fragment) {
      Fragment fragment = (Fragment) obj;
      return AriaManager.getInstance(
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? fragment.getContext()
              : fragment.getActivity());
    } else if (obj instanceof PopupWindow) {
      PopupWindow popupWindow = (PopupWindow) obj;
      return AriaManager.getInstance(popupWindow.getContentView().getContext());
    } else if (obj instanceof Dialog) {
      Dialog dialog = (Dialog) obj;
      return AriaManager.getInstance(dialog.getContext());
    } else {
      throw new IllegalArgumentException("不支持的类型");
    }
  }

  /**
   * 上传任务状态监听
   */
  public static class UploadSchedulerListener implements ISchedulerListener<UploadTask> {

    @Override public void onTaskPre(UploadTask task) {

    }

    @Override public void onTaskResume(UploadTask task) {

    }

    @Override public void onTaskStart(UploadTask task) {

    }

    @Override public void onTaskStop(UploadTask task) {

    }

    @Override public void onTaskCancel(UploadTask task) {

    }

    @Override public void onTaskFail(UploadTask task) {

    }

    @Override public void onTaskComplete(UploadTask task) {

    }

    @Override public void onTaskRunning(UploadTask task) {

    }
  }

  /**
   * 下载任务状态监听
   */
  public static class DownloadSchedulerListener implements
      IDownloadSchedulerListener<DownloadTask> {

    @Override public void onTaskPre(DownloadTask task) {

    }

    @Override public void onTaskResume(DownloadTask task) {

    }

    @Override public void onTaskStart(DownloadTask task) {

    }

    @Override public void onTaskStop(DownloadTask task) {

    }

    @Override public void onTaskCancel(DownloadTask task) {

    }

    @Override public void onTaskFail(DownloadTask task) {

    }

    @Override public void onTaskComplete(DownloadTask task) {

    }

    @Override public void onTaskRunning(DownloadTask task) {

    }

    @Override public void onNoSupportBreakPoint(DownloadTask task) {

    }
  }
}
