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
package com.arialyy.aria.core.upload;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.delegate.FtpDelegate;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IFtpTarget;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by Aria.Lao on 2017/7/27.
 * ftp单任务上传
 */
public class FtpUploadTarget extends BaseNormalTarget<FtpUploadTarget>
    implements IFtpTarget<FtpUploadTarget> {
  private final String TAG = "FtpUploadTarget";
  private FtpDelegate<FtpUploadTarget, UploadEntity, UploadTaskEntity> mDelegate;

  FtpUploadTarget(String filePath, String targetName) {
    this.mTargetName = targetName;
    initTask(filePath);
  }

  private void initTask(String filePath) {
    initTarget(filePath);
    mTaskEntity.requestType = AbsTaskEntity.U_FTP;
    mDelegate = new FtpDelegate<>(this, mTaskEntity);
  }

  /**
   * 设置上传路径，FTP上传路径必须是从"/"开始的完整路径
   *
   * @param uploadUrl 上传路径
   */
  @Override
  public FtpUploadTarget setUploadUrl(@NonNull String uploadUrl) {
    uploadUrl = CheckUtil.checkUrl(uploadUrl);
    if (!uploadUrl.endsWith("/")) {
      uploadUrl += "/";
    }
    mTaskEntity.urlEntity = CommonUtil.getFtpUrlInfo(uploadUrl);
    if (mEntity.getUrl().equals(uploadUrl)) return this;
    mEntity.setUrl(uploadUrl);
    mEntity.update();
    return this;
  }

  /**
   * 添加任务
   */
  public void add() {
    if (checkEntity()) {
      AriaManager.getInstance(AriaManager.APP)
          .setCmd(CommonUtil.createNormalCmd(mTargetName, mTaskEntity, NormalCmdFactory.TASK_CREATE,
              checkTaskType()))
          .exe();
    }
  }

  @Override public FtpUploadTarget charSet(String charSet) {
    return mDelegate.charSet(charSet);
  }

  @Override public FtpUploadTarget login(String userName, String password) {
    return mDelegate.login(userName, password);
  }

  @Override public FtpUploadTarget login(String userName, String password, String account) {
    return mDelegate.login(userName, password, account);
  }
}
