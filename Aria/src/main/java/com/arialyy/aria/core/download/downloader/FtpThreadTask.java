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
package com.arialyy.aria.core.download.downloader;

import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Created by Aria.Lao on 2017/7/24.
 * Ftp下载任务
 */
class FtpThreadTask extends AbsThreadTask {
  private final String TAG = "FtpThreadTask";

  FtpThreadTask(StateConstance constance, IDownloadListener listener,
      SubThreadConfig downloadInfo) {
    super(constance, listener, downloadInfo);
  }

  @Override public void run() {
    FTPClient client = null;
    InputStream is = null;
    BufferedRandomAccessFile file = null;
    try {
      Log.d(TAG, "任务【"
          + mConfig.TEMP_FILE.getName()
          + "】线程__"
          + mConfig.THREAD_ID
          + "__开始下载【开始位置 : "
          + mConfig.START_LOCATION
          + "，结束位置："
          + mConfig.END_LOCATION
          + "】");
      String[] temp = mEntity.getDownloadUrl().split("/");
      String[] pp = temp[2].split(":");
      client = new FTPClient();
      client.connect(pp[0], Integer.parseInt(pp[1]));
      if (!TextUtils.isEmpty(mTaskEntity.account)) {
        client.login(mTaskEntity.userName, mTaskEntity.userPw);
      } else {
        client.login(mTaskEntity.userName, mTaskEntity.userPw, mTaskEntity.account);
      }
      int reply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        client.disconnect();
        failDownload(STATE.CURRENT_LOCATION, "无法连接到ftp服务器，错误码为：" + reply, null);
        return;
      }
      client.setDataTimeout(STATE.READ_TIME_OUT);
      client.enterLocalPassiveMode();
      client.setFileType(FTP.BINARY_FILE_TYPE);
      client.setRestartOffset(mConfig.START_LOCATION);
      client.allocate(mBufSize);
      is = client.retrieveFileStream(mConfig.remotePath);
      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      file.seek(mConfig.START_LOCATION);
      byte[] buffer = new byte[mBufSize];
      int len;
      //当前子线程的下载位置
      mChildCurrentLocation = mConfig.START_LOCATION;
      while ((len = is.read(buffer)) != -1) {
        if (STATE.isCancel) break;
        if (STATE.isStop) break;
        if (mSleepTime > 0) Thread.sleep(mSleepTime);
        if (mChildCurrentLocation + len >= mConfig.END_LOCATION) {
          len = (int) (mConfig.END_LOCATION - mChildCurrentLocation);
          file.write(buffer, 0, len);
          progress(len);
          break;
        } else {
          file.write(buffer, 0, len);
          progress(len);
        }
      }
      if (STATE.isCancel || STATE.isStop) return;
      Log.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__下载完毕");
      writeConfig(true, 1);
      STATE.COMPLETE_THREAD_NUM++;
      if (STATE.isComplete()) {
        File configFile = new File(mConfigFPath);
        if (configFile.exists()) {
          configFile.delete();
        }
        STATE.isDownloading = false;
        mListener.onComplete();
      }
    } catch (IOException e) {
      failDownload(mChildCurrentLocation, "下载失败【" + mConfig.DOWNLOAD_URL + "】", e);
    } catch (Exception e) {
      failDownload(mChildCurrentLocation, "获取流失败", e);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
        if (is != null) {
          is.close();
        }
        if (client != null && client.isConnected()) {
          client.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
