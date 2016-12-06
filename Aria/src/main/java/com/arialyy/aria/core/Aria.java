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

package com.arialyy.aria.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.os.Build;

/**
 * Created by lyy on 2016/12/1.
 * Aria启动，管理全局任务
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) public class Aria {

  private Aria() {
  }

  public static AMReceiver whit(Context context) {
    if (context == null) throw new IllegalArgumentException("context 不能为 null");
    if (context instanceof Activity
        || context instanceof Service
        || context instanceof Application) {
      return AriaManager.getInstance(context).get(context);
    } else {
      throw new IllegalArgumentException("这是不支持的context");
    }
  }

  public static AMReceiver whit(Fragment fragment) {
    return AriaManager.getInstance(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? fragment.getContext()
            : fragment.getActivity()).get(fragment);
  }

  public static AriaManager get(Context context) {
    if (context == null) throw new IllegalArgumentException("context 不能为 null");
    if (context instanceof Activity
        || context instanceof Service
        || context instanceof Application) {
      return AriaManager.getInstance(context);
    } else {
      throw new IllegalArgumentException("这是不支持的context");
    }
  }
}
