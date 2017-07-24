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
package com.arialyy.aria.core.download;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class FtpDownloadTarget extends DownloadTarget {
  private final String TAG = "FtpDownloadTarget";

  /**
   * @param serverIp ftp服务器地址
   * @param port ftp端口号
   */
  FtpDownloadTarget(String serverIp, String port, String filePath, String targetName) {
    this(serverIp + ":" + port + "/" + filePath, targetName);
  }

  /**
   * @param url url 为 serverIp:port/filePath
   */
  private FtpDownloadTarget(String url, String targetName) {
    super(url, targetName);
    int lastIndex = url.lastIndexOf("/");
    mTaskEntity.downloadType = AbsTaskEntity.FTP;
    mTargetName = targetName;
    mEntity.setFileName(url.substring(lastIndex + 1, url.length()));
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   */
  public FtpDownloadTarget login(String userName, String password) {
    return login(userName, password, null);
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   * @param account ftp账号
   */
  public FtpDownloadTarget login(String userName, String password, String account) {
    if (TextUtils.isEmpty(userName)) {
      Log.e(TAG, "用户名不能为null");
      return this;
    } else if (TextUtils.isEmpty(password)) {
      Log.e(TAG, "密码不能为null");
      return this;
    }
    mTaskEntity.userName = userName;
    mTaskEntity.userPw = password;
    mTaskEntity.account = account;
    return this;
  }
}
