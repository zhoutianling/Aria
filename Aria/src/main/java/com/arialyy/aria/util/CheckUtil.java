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


package com.arialyy.aria.util;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.DownloadEntity;
import java.io.File;

/**
 * Created by Lyy on 2016/9/23.
 * 检查帮助类
 */
public class CheckUtil {
  private static final String TAG = "CheckUtil";

  /**
   * 检测下载实体是否合法
   *
   * @param entity 下载实体
   * @return 合法(true)
   */
  public static boolean checkDownloadEntity(DownloadEntity entity) {
    if (entity == null) {
      Log.w(TAG, "下载实体不能为空");
      return false;
    } else if (TextUtils.isEmpty(entity.getDownloadUrl())) {
      Log.w(TAG, "下载链接不能为空");
      return false;
    } else if (TextUtils.isEmpty(entity.getFileName())) {
      Log.w(TAG, "文件名不能为空");
      return false;
    } else if (TextUtils.isEmpty(entity.getDownloadPath())) {
      Log.w(TAG, "存储地址不能为空");
      return false;
    }
    String fileName = entity.getFileName();
    if (fileName.contains(" ")) {
      fileName = fileName.replace(" ", "_");
    }
    String dPath = entity.getDownloadPath();
    File   file  = new File(dPath);
    if (file.isDirectory()) {
      dPath += fileName;
      entity.setDownloadPath(dPath);
    }
    return true;
  }
}