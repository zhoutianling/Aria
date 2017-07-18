package com.arialyy.aria.core.download.downloader;

import com.arialyy.aria.core.download.DownloadTaskEntity;
import java.io.File;

/**
 * 子线程下载信息类
 */
final class ChildThreadConfigEntity {
  //线程Id
  int THREAD_ID;
  //下载文件大小
  long FILE_SIZE;
  //子线程启动下载位置
  long START_LOCATION;
  //子线程结束下载位置
  long END_LOCATION;
  //下载路径
  File TEMP_FILE;
  String DOWNLOAD_URL;
  String CONFIG_FILE_PATH;
  DownloadTaskEntity DOWNLOAD_TASK_ENTITY;
  boolean IS_SUPPORT_BREAK_POINT = true;
}