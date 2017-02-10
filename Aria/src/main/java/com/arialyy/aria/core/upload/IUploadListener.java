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

/**
 * Created by Aria.Lao on 2017/2/9.
 * 上传监听
 */
public interface IUploadListener {

  /**
   * 预处理
   */
  public void onPre();

  /**
   * 开始上传
   */
  public void onStart(long fileSize);

  /**
   * 恢复上传
   *
   * @param resumeLocation 上次上传停止位置
   */
  public void onResume(long resumeLocation);

  /**
   * 停止上传
   *
   * @param stopLocation 上传停止位置
   */
  public void onStop(long stopLocation);

  /**
   * 上传进度
   *
   * @param currentLocation 当前进度
   */
  public void onProgress(long currentLocation);

  /**
   * 取消上传
   */
  public void onCancel();

  /**
   * 上传成功
   */
  public void onComplete();

  /**
   * 上传失败
   */
  public void onFail();
}
