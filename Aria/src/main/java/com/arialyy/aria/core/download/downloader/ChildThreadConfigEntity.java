package com.arialyy.aria.core.download.downloader;

import com.arialyy.aria.core.download.DownloadTaskEntity;
import java.io.File;

/**
 * 子线程下载信息类
 */
public class ChildThreadConfigEntity {
  //线程Id
  public int THREAD_ID;
  //下载文件大小
  public long FILE_SIZE;
  //子线程启动下载位置
  public long START_LOCATION;
  //子线程结束下载位置
  public long END_LOCATION;
  //下载路径
  public File TEMP_FILE;
  public String DOWNLOAD_URL;
  public String CONFIG_FILE_PATH;
  public DownloadTaskEntity DOWNLOAD_TASK_ENTITY;
  public boolean IS_SUPPORT_BREAK_POINT = true;
}