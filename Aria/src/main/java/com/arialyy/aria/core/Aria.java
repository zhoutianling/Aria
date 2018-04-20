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
import android.content.Context;
import android.os.Build;
import com.arialyy.aria.core.download.DownloadReceiver;
import com.arialyy.aria.core.upload.UploadReceiver;

/**
 * Created by lyy on 2016/12/1.
 * https://github.com/AriaLyy/Aria
 * Aria启动，管理全局任务
 * <pre>
 *   <code>
 *   //下载
 *   Aria.download(this)
 *       .load(URL)     //下载地址，必填
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
 *
 * 如果你需要在【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
 * 之外的java中使用Aria，那么你应该在Application或Activity初始化的时候调用{@link #init(Context)}对Aria进行初始化
 * 然后才能使用{@link #download(Object)}、{@link #upload(Object)}
 *
 * <pre>
 *   <code>
 *       Aria.init(getContext());
 *
 *      Aria.download(this)
 *       .load(URL)     //下载地址，必填
 *       //文件保存路径，必填
 *       .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")
 *       .start();
 *
 *   </code>
 *
 * </pre>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) public class Aria {

  private Aria() {
  }

  /**
   * 初始化下载
   *
   * @param context 支持类型有【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
   */
  public static DownloadReceiver download(Context context) {
    return get(context).download(context);
  }

  /**
   * 初始化上传
   *
   * @param context 支持类型有【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
   */
  public static UploadReceiver upload(Context context) {
    return get(context).upload(context);
  }

  /**
   * 在任意对象中初始化下载，前提是你需要在Application或Activity初始化的时候调用{@link #init(Context)}对Aria进行初始化
   *
   * @param obj 任意对象
   */
  public static DownloadReceiver download(Object obj) {
    return AriaManager.getInstance().download(obj);
  }

  /**
   * 在任意对象中初始化上传，前提是你需要在Application或Activity初始化的时候调用{@link #init(Context)}对Aria进行初始化
   *
   * @param obj 任意对象
   */
  public static UploadReceiver upload(Object obj) {
    return AriaManager.getInstance().upload(obj);
  }

  /**
   * 处理通用事件
   */
  public static AriaManager get(Context context) {
    return AriaManager.getInstance(context);
  }

  /**
   * 初始化Aria，如果你需要在【Activity、Service、Application、DialogFragment、Fragment、PopupWindow、Dialog】
   * 之外的java中使用Aria，那么你应该在Application或Activity初始化的时候调用本方法对Aria进行初始化
   * 只需要初始化一次就可以
   * {@link #download(Object)}、{@link #upload(Object)}
   */
  public static AriaManager init(Context context) {
    return AriaManager.getInstance(context);
  }
}
