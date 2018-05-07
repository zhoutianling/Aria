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
package com.arialyy.aria.core.delegate;

import android.text.TextUtils;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IFtpTarget;
import com.arialyy.aria.core.inf.ITarget;
import com.arialyy.aria.util.ALog;

/**
 * Created by laoyuyu on 2018/3/9.
 * ftp 委托
 */
public class FtpDelegate<TARGET extends ITarget, ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements IFtpTarget<TARGET> {
  private static final String TAG = "FtpDelegate";
  private ENTITY mEntity;
  private TASK_ENTITY mTaskEntity;
  private TARGET mTarget;

  public FtpDelegate(TARGET target, TASK_ENTITY taskEntity) {
    mTarget = target;
    mTaskEntity = taskEntity;
    mEntity = mTaskEntity.getEntity();
  }

  @Override public TARGET charSet(String charSet) {
    if (TextUtils.isEmpty(charSet)) return mTarget;
    mTaskEntity.setCharSet(charSet);
    return mTarget;
  }

  @Override public TARGET login(String userName, String password) {
    return login(userName, password, null);
  }

  @Override public TARGET login(String userName, String password, String account) {
    if (TextUtils.isEmpty(userName)) {
      ALog.e(TAG, "用户名不能为null");
      return mTarget;
    } else if (TextUtils.isEmpty(password)) {
      ALog.e(TAG, "密码不能为null");
      return mTarget;
    }
    mTaskEntity.getUrlEntity().needLogin = true;
    mTaskEntity.getUrlEntity().user = userName;
    mTaskEntity.getUrlEntity().password = password;
    mTaskEntity.getUrlEntity().account = account;
    return mTarget;
  }
}
