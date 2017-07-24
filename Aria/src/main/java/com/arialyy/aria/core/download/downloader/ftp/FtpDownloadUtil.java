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
package com.arialyy.aria.core.download.downloader.ftp;

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.downloader.IDownloadListener;
import com.arialyy.aria.core.download.downloader.IDownloadUtil;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Created by Aria.Lao on 2017/7/21.
 */
public class FtpDownloadUtil implements IDownloadUtil, Runnable {

  private IDownloadListener mListener;
  private DownloadTaskEntity mTaskEntity;
  private DownloadEntity mEntity;

  public FtpDownloadUtil(DownloadTaskEntity entity, IDownloadListener downloadListener) {
    mTaskEntity = entity;
    mListener = downloadListener;
    mEntity = mTaskEntity.getEntity();
  }

  @Override public long getFileSize() {
    return 0;
  }

  @Override public long getCurrentLocation() {
    return 0;
  }

  @Override public boolean isDownloading() {
    return false;
  }

  @Override public void cancelDownload() {

  }

  @Override public void stopDownload() {

  }

  @Override public void startDownload() {
    mListener.onPre();
    new Thread(this).start();
  }

  @Override public void resumeDownload() {

  }

  @Override public void setMaxSpeed(double maxSpeed) {

  }

  @Override public void run() {

  }

  private void failDownload(String msg) {

  }

  private void test() throws IOException {

  }
}
