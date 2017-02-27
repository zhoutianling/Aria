package com.arialyy.aria.core.inf;

/**
 * Created by Aria.Lao on 2017/2/13.
 */

public interface ITask {

  /**
   * 唯一标识符，DownloadTask 为下载地址，UploadTask 为文件路径
   */
  public String getKey();

  /**
   * 是否真正执行
   * @return true，正在执行；
   */
  public boolean isRunning();

  /**
   * 获取工具实体
   */
  public IEntity getEntity();

  public void start();

  public void stop();

  public void cancel();

  public long getSpeed();

  public long getFileSize();

  public long getCurrentProgress();

  public void setTargetName(String targetName);
}
