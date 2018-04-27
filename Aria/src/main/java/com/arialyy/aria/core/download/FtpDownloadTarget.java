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

import android.support.annotation.NonNull;
import com.arialyy.aria.core.delegate.FtpDelegate;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IFtpTarget;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class FtpDownloadTarget extends BaseNormalTarget<FtpDownloadTarget>
    implements IFtpTarget<FtpDownloadTarget> {
  private FtpDelegate<FtpDownloadTarget, DownloadEntity, DownloadTaskEntity> mDelegate;

  FtpDownloadTarget(DownloadEntity entity, String targetName, boolean refreshInfo) {
    this(entity.getUrl(), targetName, refreshInfo);
  }

  FtpDownloadTarget(String url, String targetName) {
    this(url, targetName, false);
  }

  FtpDownloadTarget(String url, String targetName, boolean refreshInfo) {
    initTarget(url, targetName, refreshInfo);
    init(refreshInfo);
  }

  private void init(boolean refreshInfo) {
    int lastIndex = url.lastIndexOf("/");
    mEntity.setFileName(url.substring(lastIndex + 1, url.length()));
    mTaskEntity.setUrlEntity(CommonUtil.getFtpUrlInfo(url));
    mTaskEntity.setRefreshInfo(refreshInfo);
    mTaskEntity.setRequestType(AbsTaskEntity.D_FTP);

    mDelegate = new FtpDelegate<>(this, mTaskEntity);
  }

  /**
   * 设置文件保存文件夹路径
   *
   * @param filePath 文件保存路径
   * @deprecated {@link #setFilePath(String)} 请使用这个api
   */
  @Deprecated
  public FtpDownloadTarget setDownloadPath(@NonNull String filePath) {
    return setFilePath(filePath);
  }

  /**
   * 设置文件保存文件夹路径
   * 关于文件名：
   * 1、如果保存路径是该文件的保存路径，如：/mnt/sdcard/file.zip，则使用路径中的文件名file.zip
   * 2、如果保存路径是文件夹路径，如：/mnt/sdcard/，则使用FTP服务器该文件的文件名
   *
   * @param filePath 路径必须为文件路径，不能为文件夹路径
   */
  public FtpDownloadTarget setFilePath(@NonNull String filePath) {
    mTempFilePath = filePath;
    return this;
  }

  @Override protected int getTargetType() {
    return FTP;
  }

  @Override public FtpDownloadTarget charSet(String charSet) {
    return mDelegate.charSet(charSet);
  }

  @Override public FtpDownloadTarget login(String userName, String password) {
    return mDelegate.login(userName, password);
  }

  @Override public FtpDownloadTarget login(String userName, String password, String account) {
    return mDelegate.login(userName, password, account);
  }
}
