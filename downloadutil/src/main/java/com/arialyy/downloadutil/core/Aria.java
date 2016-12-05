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

/**
 * Created by lyy on 2016/12/1.
 * Aria启动，管理全局任务
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class Aria {
  private static final Object LOCK = new Object();
  private static volatile Aria INSTANCE = null;
  private DownloadManager mDownloadManager;

  private Aria() {
    //mDownloadManager = DownloadManager.getInstance();
  }

  public static AMReceiver whit(Context context) {
    return AriaManager.getInstance(context).get(context);
  }

  public static AriaManager get(Context context){
    return AriaManager.getInstance(context);
  }
}
