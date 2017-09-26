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
package com.arialyy.aria.core.upload.uploader;

import com.arialyy.aria.core.common.AbsFtpInfoThread;
import com.arialyy.aria.core.common.OnFileInfoCallback;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by Aria.Lao on 2017/9/26.
 * FTP远程服务器文件信息
 */
class FtpFileInfoThread extends AbsFtpInfoThread<UploadEntity, UploadTaskEntity> {
  static final int CODE_EXISTS = 0xab1;
  private boolean exists = false;

  FtpFileInfoThread(UploadTaskEntity taskEntity, OnFileInfoCallback callback) {
    super(taskEntity, callback);
  }

  @Override protected String setRemotePath() {
    String url = mEntity.getUrl();
    return url.substring(url.indexOf(mPort) + mPort.length(), url.length())
        + "/"
        + mEntity.getFileName();
  }

  @Override protected void handleFile(String remotePath, FTPFile ftpFile) {
    super.handleFile(remotePath, ftpFile);
    //远程文件已完成
    if (ftpFile != null && ftpFile.getSize() == mEntity.getFileSize()) {
      exists = true;
    }
  }

  @Override protected void onPreComplete(int code) {
    super.onPreComplete(code);
    mCallback.onComplete(mEntity.getKey(), exists ? CODE_EXISTS : code);
  }
}
