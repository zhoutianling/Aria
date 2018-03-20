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
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.delegate.HttpHeaderDelegate;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IHttpHeaderTarget;
import com.arialyy.aria.util.CheckUtil;
import java.util.Map;

/**
 * Created by lyy on 2017/2/28.
 * http 单文件上传
 */
public class UploadTarget extends BaseNormalTarget<UploadTarget>
    implements IHttpHeaderTarget<UploadTarget> {
  private static final String TAG = "UploadTarget";
  private HttpHeaderDelegate<UploadTarget, UploadEntity, UploadTaskEntity> mDelegate;

  UploadTarget(String filePath, String targetName) {
    this.mTargetName = targetName;
    initTask(filePath);
  }

  private void initTask(String filePath) {
    initTarget(filePath);

    //http暂时不支持断点上传
    mTaskEntity.isSupportBP = false;
    mTaskEntity.requestType = AbsTaskEntity.U_HTTP;
    mDelegate = new HttpHeaderDelegate<>(this, mTaskEntity);
  }

  @Override public UploadTarget setUploadUrl(@NonNull String uploadUrl) {
    uploadUrl = CheckUtil.checkUrl(uploadUrl);
    if (mEntity.getUrl().equals(uploadUrl)) {
      return this;
    }
    mEntity.setUrl(uploadUrl);
    mEntity.update();
    return this;
  }

  /**
   * 设置userAgent
   */
  public UploadTarget setUserAngent(@NonNull String userAgent) {
    mTaskEntity.userAgent = userAgent;
    return this;
  }

  /**
   * 设置服务器需要的附件key
   *
   * @param attachment 附件key
   */
  public UploadTarget setAttachment(@NonNull String attachment) {
    mTaskEntity.attachment = attachment;
    return this;
  }

  /**
   * 设置上传文件类型
   *
   * @param contentType tip：multipart/form-data
   */
  public UploadTarget setContentType(String contentType) {
    mTaskEntity.contentType = contentType;
    return this;
  }

  @Override public UploadTarget addHeader(@NonNull String key, @NonNull String value) {
    return mDelegate.addHeader(key, value);
  }

  @Override public UploadTarget addHeaders(Map<String, String> headers) {
    return mDelegate.addHeaders(headers);
  }

  @Override public UploadTarget setRequestMode(RequestEnum requestEnum) {
    return mDelegate.setRequestMode(requestEnum);
  }
}
