package com.arialyy.aria.core.upload;

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.orm.DbEntity;

/**
 * Created by Aria.Lao on 2017/2/9.
 * 上传文件实体
 */
public class UploadEntity {

  private String filePath;  //文件路径
  private String fileName;  //文件名
  private long fileSize;    //文件大小

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
