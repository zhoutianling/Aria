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
import com.arialyy.aria.core.delegate.FtpDelegate;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IFtpTarget;
import com.arialyy.aria.core.manager.TEManager;
import com.arialyy.aria.util.ALog;

/**
 * Created by Aria.Lao on 2017/7/26.
 * ftp文件夹下载
 */
public class FtpDirDownloadTarget extends BaseGroupTarget<FtpDirDownloadTarget>
    implements IFtpTarget<FtpDirDownloadTarget> {
  private FtpDelegate<FtpDirDownloadTarget, DownloadGroupEntity, DownloadGroupTaskEntity> mDelegate;

  FtpDirDownloadTarget(String url, String targetName) {
    mTargetName = targetName;
    init(url);
  }

  private void init(String key) {
    mGroupName = key;
    mTaskEntity = TEManager.getInstance().getFDTEntity(DownloadGroupTaskEntity.class, key);
    mTaskEntity.setRequestType(AbsTaskEntity.D_FTP_DIR);
    mEntity = mTaskEntity.getEntity();
    if (mEntity != null) {
      mDirPathTemp = mEntity.getDirPath();
    }
    mDelegate = new FtpDelegate<>(this, mTaskEntity);
  }

  @Override protected int getTargetType() {
    return GROUP_FTP_DIR;
  }

  @Override protected boolean checkEntity() {
    boolean b = getTargetType() == GROUP_FTP_DIR && checkDirPath() && checkUrl();
    if (b) {
      mEntity.save();
      mTaskEntity.save();
      if (mTaskEntity.getSubTaskEntities() != null) {
        //初始化子项的登录信息
        for (DownloadTaskEntity entity : mTaskEntity.getSubTaskEntities()) {
          entity.getUrlEntity().needLogin = mTaskEntity.getUrlEntity().needLogin;
          entity.getUrlEntity().account = mTaskEntity.getUrlEntity().account;
          entity.getUrlEntity().user = mTaskEntity.getUrlEntity().user;
          entity.getUrlEntity().password = mTaskEntity.getUrlEntity().password;
        }
      }
    }
    return b;
  }

  /**
   * 检查普通任务的下载地址
   *
   * @return {@code true}地址合法
   */
  private boolean checkUrl() {
    final String url = mGroupName;
    if (TextUtils.isEmpty(url)) {
      ALog.e(TAG, "下载失败，url为null");
      return false;
    } else if (!url.startsWith("ftp")) {
      ALog.e(TAG, "下载失败，url【" + url + "】错误");
      return false;
    }
    int index = url.indexOf("://");
    if (index == -1) {
      ALog.e(TAG, "下载失败，url【" + url + "】不合法");
      return false;
    }
    return true;
  }

  @Override public FtpDirDownloadTarget charSet(String charSet) {
    return mDelegate.charSet(charSet);
  }

  @Override public FtpDirDownloadTarget login(String userName, String password) {
    return mDelegate.login(userName, password);
  }

  @Override public FtpDirDownloadTarget login(String userName, String password, String account) {
    return mDelegate.login(userName, password, account);
  }
}
