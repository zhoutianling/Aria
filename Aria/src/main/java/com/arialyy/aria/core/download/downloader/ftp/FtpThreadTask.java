///*
// * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.arialyy.aria.core.download.downloader.ftp;
//
//import com.arialyy.aria.core.AriaManager;
//import com.arialyy.aria.core.download.DownloadTaskEntity;
//import com.arialyy.aria.core.download.downloader.IDownloadListener;
//import com.arialyy.aria.util.BufferedRandomAccessFile;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.math.BigDecimal;
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPReply;
//
///**
// * Created by Aria.Lao on 2017/7/24.
// * Ftp下载任务
// */
//class FtpThreadTask implements Runnable {
//
//  private FtpConfigEntity mConfig;
//  private IDownloadListener mListener;
//  private DownloadTaskEntity mTaskEntity;
//  private int mBufSize;
//  private long mSleepTime = 0;
//
//  FtpThreadTask(FtpConfigEntity config, IDownloadListener listener) {
//    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
//    mConfig = config;
//    mListener = listener;
//
//    mBufSize = manager.getDownloadConfig().getBuffSize();
//    setMaxSpeed(AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMsxSpeed());
//  }
//
//  void setMaxSpeed(double maxSpeed) {
//    if (-0.9999 < maxSpeed && maxSpeed < 0.00001) {
//      mSleepTime = 0;
//    } else {
//      BigDecimal db = new BigDecimal(((mBufSize / 1024) / maxSpeed) * 1000);
//      mSleepTime = db.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
//    }
//  }
//
//  @Override public void run() {
//    try {
//
//      FTPClient client = new FTPClient();
//      //ip和端口
//      //String[] temp = mEntity.getDownloadUrl().split("/");
//      //String[] pp = temp[2].split(":");
//      String dir = temp[temp.length - 2];
//      String fileName = temp[temp.length - 1];
//      client.connect(pp[0], Integer.parseInt(pp[1]));
//      client.login(mTaskEntity.userName, mTaskEntity.userPw);
//      int reply = client.getReplyCode();
//      if (!FTPReply.isPositiveCompletion(reply)) {
//        client.disconnect();
//        //failDownload("无法连接到ftp服务器，错误码为：" + reply);
//        return;
//      }
//      client.enterLocalPassiveMode();
//      client.setFileType(FTP.BINARY_FILE_TYPE);
//      FTPFile[] files = client.listFiles(fileName);
//      files[0].getSize();
//      BufferedRandomAccessFile file =
//          new BufferedRandomAccessFile(new File(mConfig.PATH), "rwd", 8192);
//      InputStream is = client.retrieveFileStream(fileName);
//
//      byte[] buffer = new byte[8192];
//      int len;
//      //当前子线程的下载位置
//      while ((len = is.read(buffer)) != -1) {
//        file.write(buffer, 0, len);
//      }
//    }catch (IOException e){
//
//    }
//  }
//}
