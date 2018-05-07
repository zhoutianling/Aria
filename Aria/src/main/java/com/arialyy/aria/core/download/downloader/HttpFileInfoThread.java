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
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.common.OnFileInfoCallback;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 下载文件信息获取
 */
class HttpFileInfoThread implements Runnable {
  private final String TAG = "HttpFileInfoThread";
  private DownloadEntity mEntity;
  private DownloadTaskEntity mTaskEntity;
  private int mConnectTimeOut;
  private OnFileInfoCallback onFileInfoListener;

  HttpFileInfoThread(DownloadTaskEntity taskEntity, OnFileInfoCallback callback) {
    this.mTaskEntity = taskEntity;
    mEntity = taskEntity.getEntity();
    mConnectTimeOut =
        AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getConnectTimeOut();
    onFileInfoListener = callback;
  }

  @Override public void run() {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(CommonUtil.convertUrl(mEntity.getUrl()));
      conn = ConnectionHelp.handleConnection(url);
      conn = ConnectionHelp.setConnectParam(mTaskEntity, conn);
      conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      conn.setConnectTimeout(mConnectTimeOut);
      //conn.setChunkedStreamingMode(0);
      conn.connect();
      handleConnect(conn);
    } catch (IOException e) {
      failDownload("下载失败【downloadUrl:"
          + mEntity.getUrl()
          + "】\n【filePath:"
          + mEntity.getDownloadPath()
          + "】\n"
          + ALog.getExceptionString(e), true);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private void handleConnect(HttpURLConnection conn) throws IOException {
    long len = conn.getContentLength();
    if (len < 0) {
      String temp = conn.getHeaderField("Content-Length");
      len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
      // 某些服务，如果设置了conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      // 会返回 Content-Range: bytes 0-225427911/225427913
      if (len < 0) {
        temp = conn.getHeaderField("Content-Range");
        if (TextUtils.isEmpty(temp)) {
          len = -1;
        } else {
          int start = temp.indexOf("/");
          len = Long.parseLong(temp.substring(start + 1, temp.length()));
        }
      }
    }
    int code = conn.getResponseCode();
    boolean end = false;
    if (TextUtils.isEmpty(mEntity.getMd5Code())) {
      String md5Code = conn.getHeaderField("Content-MD5");
      mEntity.setMd5Code(md5Code);
    }

    boolean isChunked = false;
    final String str = conn.getHeaderField("Transfer-Encoding");
    if (!TextUtils.isEmpty(str) && str.equals("chunked")) {
      isChunked = true;
    }
    //Map<String, List<String>> headers = conn.getHeaderFields();
    String disposition = conn.getHeaderField("Content-Disposition");
    if (mTaskEntity.isUseServerFileName() && !TextUtils.isEmpty(disposition)) {
      mEntity.setDisposition(CommonUtil.encryptBASE64(disposition));
      if (disposition.contains(";")) {
        String[] infos = disposition.split(";");
        for (String info : infos) {
          if (info.startsWith("filename") && info.contains("=")) {
            String[] temp = info.split("=");
            if (temp.length > 1) {
              String newName = URLDecoder.decode(temp[1], "utf-8");
              mEntity.setServerFileName(newName);
              fileRename(newName);
              break;
            }
          }
        }
      }
    }

    mTaskEntity.setCode(code);
    if (code == HttpURLConnection.HTTP_PARTIAL) {
      if (!checkLen(len) && !isChunked) {
        return;
      }
      mEntity.setFileSize(len);
      mTaskEntity.setSupportBP(true);
      end = true;
    } else if (code == HttpURLConnection.HTTP_OK) {
      if (conn.getHeaderField("Content-Type").equals("text/html")) {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(ConnectionHelp.convertInputStream(conn)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
        reader.close();
        handleUrlReTurn(conn, CommonUtil.getWindowReplaceUrl(sb.toString()));
        return;
      } else if (!checkLen(len) && !isChunked) {
        return;
      }
      mEntity.setFileSize(len);
      mTaskEntity.setSupportBP(false);
      end = true;
    } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
      failDownload("任务【" + mEntity.getUrl() + "】下载失败，错误码：404", true);
    } else if (code == HttpURLConnection.HTTP_MOVED_TEMP
        || code == HttpURLConnection.HTTP_MOVED_PERM
        || code == HttpURLConnection.HTTP_SEE_OTHER) {
      handleUrlReTurn(conn, conn.getHeaderField("Location"));
    } else {
      failDownload("任务【" + mEntity.getUrl() + "】下载失败，错误码：" + code, true);
    }
    if (end) {
      mTaskEntity.setChunked(isChunked);
      mTaskEntity.update();
      if (onFileInfoListener != null) {
        CompleteInfo info = new CompleteInfo(code);
        onFileInfoListener.onComplete(mEntity.getUrl(), info);
      }
    }
  }

  /**
   * 重命名文件
   */
  private void fileRename(String newName) {
    if (TextUtils.isEmpty(newName)) {
      ALog.w(TAG, "重命名失败【服务器返回的文件名为空】");
      return;
    }
    File oldFile = new File(mEntity.getDownloadPath());
    String oldName = oldFile.getName();
    String newPath = oldFile.getParent() + "/" + newName;
    if (oldFile.exists()){
      oldFile.renameTo(new File(newPath));
    }
    mEntity.setFileName(newName);
    mEntity.setDownloadPath(newPath);
    mTaskEntity.setKey(newPath);
    CommonUtil.renameDownloadConfig(oldName, newName);
  }

  /**
   * 处理30x跳转
   */
  private void handleUrlReTurn(HttpURLConnection conn, String newUrl) throws IOException {
    ALog.d(TAG, "30x跳转，新url为【" + newUrl + "】");
    if (TextUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase("null") || !newUrl.startsWith(
        "http")) {
      if (onFileInfoListener != null) {
        onFileInfoListener.onFail(mEntity.getUrl(), "获取重定向链接失败", false);
      }
      return;
    }
    if (!CheckUtil.checkUrl(newUrl)) {
      failDownload("下载失败，重定向url错误", false);
      return;
    }
    mTaskEntity.setRedirectUrl(newUrl);
    mEntity.setRedirect(true);
    mEntity.setRedirectUrl(newUrl);
    String cookies = conn.getHeaderField("Set-Cookie");
    conn = (HttpURLConnection) new URL(newUrl).openConnection();
    conn = ConnectionHelp.setConnectParam(mTaskEntity, conn);
    conn.setRequestProperty("Cookie", cookies);
    conn.setRequestProperty("Range", "bytes=" + 0 + "-");
    conn.setConnectTimeout(mConnectTimeOut);
    conn.connect();
    handleConnect(conn);
    conn.disconnect();
  }

  /**
   * 检查长度是否合法，并且检查新获取的文件长度是否和数据库的文件长度一直，如果不一致，则表示该任务为新任务
   *
   * @param len 从服务器获取的文件长度
   * @return {@code true}合法
   */
  private boolean checkLen(long len) {
    if (len != mEntity.getFileSize()) {
      mTaskEntity.setNewTask(true);
    }
    if (len < 0) {
      failDownload("任务【" + mEntity.getUrl() + "】下载失败，文件长度小于0", true);
      return false;
    }
    return true;
  }

  private void failDownload(String errorMsg, boolean needRetry) {
    ALog.e(TAG, errorMsg);
    if (onFileInfoListener != null) {
      onFileInfoListener.onFail(mEntity.getUrl(), errorMsg, needRetry);
    }
  }
}