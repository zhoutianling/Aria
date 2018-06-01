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

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.AbsFtpThreadTask;
import com.arialyy.aria.core.common.StateConstance;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.OnFtpInputStreamListener;

/**
 * Created by Aria.Lao on 2017/7/28.
 * FTP 单线程上传任务，需要FTP 服务器给用户打开append和write的权限
 */
class FtpThreadTask extends AbsFtpThreadTask<UploadEntity, UploadTaskEntity> {
  private final String TAG = "FtpUploadThreadTask";
  private String dir, remotePath;

  FtpThreadTask(StateConstance constance, IEventListener listener,
      SubThreadConfig<UploadTaskEntity> info) {
    super(constance, listener, info);
    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
    mConnectTimeOut = manager.getUploadConfig().getConnectTimeOut();
    mReadTimeOut = manager.getUploadConfig().getIOTimeOut();
    mBufSize = manager.getUploadConfig().getBuffSize();
    isNotNetRetry = manager.getUploadConfig().isNotNetRetry();
  }

  @Override public void run() {
    //当前子线程的下载位置
    mChildCurrentLocation = mConfig.START_LOCATION;
    FTPClient client = null;
    BufferedRandomAccessFile file = null;
    try {
      ALog.d(TAG, "任务【"
          + mConfig.TEMP_FILE.getName()
          + "】线程__"
          + mConfig.THREAD_ID
          + "__开始上传【开始位置 : "
          + mConfig.START_LOCATION
          + "，结束位置："
          + mConfig.END_LOCATION
          + "】");
      client = createClient();
      if (client == null) return;
      initPath();
      client.makeDirectory(dir);
      client.changeWorkingDirectory(dir);
      client.setRestartOffset(mConfig.START_LOCATION);
      int reply = client.getReplyCode();
      if (!FTPReply.isPositivePreliminary(reply) && reply != FTPReply.FILE_ACTION_OK) {
        fail(mChildCurrentLocation, "上传失败，错误码为：" + reply + "，msg：" + client.getReplyString(), null);
        client.disconnect();
        return;
      }

      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      if (mConfig.START_LOCATION != 0) {
        //file.skipBytes((int) mConfig.START_LOCATION);
        file.seek(mConfig.START_LOCATION);
      }
      upload(client, file);
      if (isBreak()) {
        return;
      }
      ALog.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__上传完毕");
      writeConfig(true, 1);
      STATE.COMPLETE_THREAD_NUM++;
      if (STATE.isComplete()) {
        STATE.TASK_RECORD.deleteData();
        STATE.isRunning = false;
        mListener.onComplete();
      }
      if (STATE.isFail()) {
        STATE.isRunning = false;
        mListener.onFail(false);
      }
    } catch (IOException e) {
      fail(mChildCurrentLocation, "上传失败【" + mConfig.URL + "】", e);
    } catch (Exception e) {
      fail(mChildCurrentLocation, "获取流失败", e);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
        if (client != null && client.isConnected()) {
          client.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void initPath() throws UnsupportedEncodingException {
    dir = new String(mTaskEntity.getUrlEntity().remotePath.getBytes(charSet), SERVER_CHARSET);
    remotePath = new String(
        ("/" + mTaskEntity.getUrlEntity().remotePath + "/" + mEntity.getFileName()).getBytes(
            charSet),
        SERVER_CHARSET);
  }

  private void upload(final FTPClient client, final BufferedRandomAccessFile bis)
      throws IOException {

    try {
      ALog.d(TAG, "remotePath【" + remotePath + "】");
      client.storeFile(remotePath, new FtpFISAdapter(bis), new OnFtpInputStreamListener() {
        boolean isStoped = false;

        @Override public void onFtpInputStream(FTPClient client, long totalBytesTransferred,
            int bytesTransferred, long streamSize) {
          if (isBreak() && !isStoped) {
            try {
              isStoped = true;
              client.abor();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          progress(bytesTransferred);
        }
      });
    } catch (IOException e) {
      if (e.getMessage().contains("IOException caught while copying")) {
        e.printStackTrace();
      } else {
        fail(mChildCurrentLocation, "上传失败", e);
      }
    }

    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      if (reply != FTPReply.TRANSFER_ABORTED) {
        fail(mChildCurrentLocation, "上传文件错误，错误码为：" + reply + "，msg:" + client.getReplyString(),
            null);
      }
      if (client.isConnected()) {
        client.disconnect();
      }
    }
  }

  @Override protected String getTaskType() {
    return "FTP_UPLOAD";
  }
}
