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

import android.support.annotation.CheckResult;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.delegate.FtpDelegate;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IFtpTarget;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by Aria.Lao on 2017/7/27.
 * ftp单任务上传
 */
public class FtpUploadTarget extends BaseNormalTarget<FtpUploadTarget>
    implements IFtpTarget<FtpUploadTarget> {
  private FtpDelegate<FtpUploadTarget, UploadEntity, UploadTaskEntity> mDelegate;

  private String mAccount, mUser, mPw;
  private boolean needLogin = false;

  FtpUploadTarget(String filePath, String targetName) {
    this.mTargetName = targetName;
    initTask(filePath);
  }

  private void initTask(String filePath) {
    initTarget(filePath);
    mTaskEntity.setRequestType(AbsTaskEntity.U_FTP);
    mDelegate = new FtpDelegate<>(this, mTaskEntity);
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

  @Override protected boolean checkUrl() {
    boolean b = super.checkUrl();
    if (!b) {
      return false;
    }
    mTaskEntity.setUrlEntity(CommonUtil.getFtpUrlInfo(mTempUrl));
    mTaskEntity.getUrlEntity().account = mAccount;
    mTaskEntity.getUrlEntity().user = mUser;
    mTaskEntity.getUrlEntity().password = mPw;
    mTaskEntity.getUrlEntity().needLogin = needLogin;
    return true;
  }

  @CheckResult
  @Override public FtpUploadTarget charSet(String charSet) {
    return mDelegate.charSet(charSet);
  }

  @Override public FtpUploadTarget login(String userName, String password) {
    needLogin = true;
    mUser = userName;
    mPw = password;
    return this;
  }

  @Override public FtpUploadTarget login(String userName, String password, String account) {
    needLogin = true;
    mUser = userName;
    mPw = password;
    mAccount = account;
    return this;
  }
}
