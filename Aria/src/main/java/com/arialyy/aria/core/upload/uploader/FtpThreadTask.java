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

import android.util.Log;
import com.arialyy.aria.core.common.AbsFtpThreadTask;
import com.arialyy.aria.core.common.StateConstance;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.OnFtpInputStreamListener;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/**
 * Created by Aria.Lao on 2017/7/28.
 * FTP 单线程上传任务，需要FTP 服务器给用户打开删除和读入IO的权限
 */
class FtpThreadTask extends AbsFtpThreadTask<UploadEntity, UploadTaskEntity> {
  private final String TAG = "FtpThreadTask";
  private String dir, remotePath;

  FtpThreadTask(StateConstance constance, IEventListener listener,
      SubThreadConfig<UploadTaskEntity> info) {
    super(constance, listener, info);
  }

  @Override public void run() {
    FTPClient client = null;
    BufferedRandomAccessFile file = null;
    try {
      Log.d(TAG, "任务【"
          + mConfig.TEMP_FILE.getName()
          + "】线程__"
          + mConfig.THREAD_ID
          + "__开始上传【开始位置 : "
          + mConfig.START_LOCATION
          + "，结束位置："
          + mConfig.END_LOCATION
          + "】");
      //当前子线程的下载位置
      mChildCurrentLocation = mConfig.START_LOCATION;
      client = createClient();
      if (client == null) return;
      initPath();
      client.makeDirectory(dir);
      client.changeWorkingDirectory(dir);
      client.setRestartOffset(mConfig.START_LOCATION);
      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      if (mConfig.START_LOCATION != 0) {
        file.skipBytes((int) mConfig.START_LOCATION);
        //file.seek(mConfig.START_LOCATION);
      }
      upload(client, file);
      if (STATE.isCancel || STATE.isStop) return;
      Log.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__上传完毕");
      writeConfig(true, 1);
      STATE.COMPLETE_THREAD_NUM++;
      if (STATE.isComplete()) {
        File configFile = new File(mConfigFPath);
        if (configFile.exists()) {
          configFile.delete();
        }
        STATE.isRunning = false;
        mListener.onComplete();
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
    String url = mEntity.getUrl();
    String temp = url.substring(url.indexOf(port) + port.length(), url.length());
    dir = new String(temp.getBytes(charSet), SERVER_CHARSET);
    remotePath = new String((temp + "/" + mEntity.getFileName()).getBytes(charSet), SERVER_CHARSET);
  }

  private void upload(final FTPClient client, final BufferedRandomAccessFile bis)
      throws IOException {
    //client.storeFile(remotePath,
    //    new ProgressInputStream(bis, new ProgressInputStream.ProgressCallback() {
    //      @Override public void onProgressCallback(byte[] buffer, int byteOffset, int byteCount)
    //          throws IOException {
    //        if (STATE.isCancel || STATE.isStop) {
    //          long s = client.abor();
    //          Log.d(TAG, "s = " + s);
    //          client.disconnect();
    //        }
    //        progress(byteCount);
    //      }
    //    }));



    try {
      client.storeFile(remotePath, new ProgressInputStream(bis), new OnFtpInputStreamListener() {
        @Override public void onFtpInputStream(FTPClient client, long totalBytesTransferred,
            int bytesTransferred, long streamSize) {
          if (STATE.isCancel || STATE.isStop) {
            try {
              client.abor();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          progress(bytesTransferred);
        }
      });

    } catch (IOException e) {
      //e.printStackTrace();
    }

    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      client.disconnect();
      fail(mChildCurrentLocation, "上传文件错误，错误码为：" + reply, null);
    }

  }

  /**
   * 执行上传操作
   */
  private void upload(BufferedRandomAccessFile file, OutputStream os)
      throws IOException, InterruptedException {
    int len;
    byte[] buffer = new byte[mBufSize];
    while ((len = file.read(buffer)) != -1) {
      if (STATE.isCancel) break;
      if (STATE.isStop) break;
      if (mSleepTime > 0) Thread.sleep(mSleepTime);
      if (mChildCurrentLocation + len >= mConfig.END_LOCATION) {
        len = (int) (mConfig.END_LOCATION - mChildCurrentLocation);
        os.write(buffer, 0, len);
        progress(len);
        break;
      } else {
        os.write(buffer, 0, len);
        progress(len);
      }
      Log.d(TAG, len + "");
    }
  }

  @Override protected String getTaskType() {
    return "FTP_UPLOAD";
  }
}
