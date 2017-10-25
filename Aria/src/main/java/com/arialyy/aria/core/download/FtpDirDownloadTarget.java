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
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by Aria.Lao on 2017/7/26.
 * ftp文件夹下载
 */
public class FtpDirDownloadTarget
    extends BaseGroupTarget<FtpDirDownloadTarget, DownloadGroupTaskEntity> {
  private final String TAG = "FtpDirDownloadTarget";

  FtpDirDownloadTarget(String url, String targetName) {
    init(url);
    mTargetName = targetName;
    mTaskEntity.urlEntity = CommonUtil.getFtpUrlInfo(url);
    mTaskEntity.requestType = AbsTaskEntity.FTP_DIR;
  }

  private void init(String key) {
    mGroupName = key;
    mTaskEntity = DbEntity.findFirst(DownloadGroupTaskEntity.class, "key=?", key);
    if (mTaskEntity == null) {
      mTaskEntity = new DownloadGroupTaskEntity();
      mTaskEntity.save(getDownloadGroupEntity());
    }
    if (mTaskEntity.entity == null) {
      mTaskEntity.save(getDownloadGroupEntity());
    }
    mEntity = mTaskEntity.entity;
  }

  /**
   * 设置字符编码
   */
  public FtpDirDownloadTarget charSet(String charSet) {
    if (TextUtils.isEmpty(charSet)) return this;
    mTaskEntity.charSet = charSet;
    return this;
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   */
  public FtpDirDownloadTarget login(String userName, String password) {
    return login(userName, password, null);
  }

  /**
   * ftp 用户登录信息
   *
   * @param userName ftp用户名
   * @param password ftp用户密码
   * @param account ftp账号
   */
  public FtpDirDownloadTarget login(String userName, String password, String account) {
    if (TextUtils.isEmpty(userName)) {
      ALog.e(TAG, "用户名不能为null");
      return this;
    } else if (TextUtils.isEmpty(password)) {
      ALog.e(TAG, "密码不能为null");
      return this;
    }
    mTaskEntity.urlEntity.needLogin = true;
    mTaskEntity.urlEntity.user = userName;
    mTaskEntity.urlEntity.password = password;
    mTaskEntity.urlEntity.account = account;
    return this;
  }
}
