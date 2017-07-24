package com.arialyy.aria.core.download.downloader;

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
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
    FTPClient client = new FTPClient();
    client.connect(mEntity.getDownloadUrl());
    client.login(mTaskEntity.userName, mTaskEntity.userPw);
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      client.disconnect();
      failDownload("无法连接到ftp服务器，错误码为：" + reply);
      return;
    }

  }
}
