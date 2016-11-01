package com.arialyy.downloadutil.core.inf;

/**
 * Created by “AriaLyy@outlook.com” on 2016/10/31.
 * 抽象的下载接口
 */
public interface IDownloadUtil {

  /**
   * 获取当前下载位置
   */
  public long getCurrentLocation();

  /**
   * 是否正在下载
   *
   * @return true, 正在下载
   */
  public boolean isDownloading();

  /**
   * 取消下载
   */
  public void cancelDownload();

  /**
   * 停止下载
   */
  public void stopDownload();

  /**
   * 开始下载
   */
  public void startDownload();

  /**
   * 从上次断点恢复下载
   */
  public void resumeDownload();

  /**
   * 删除下载记录文件
   */
  public void delConfigFile();

  /**
   * 删除temp文件
   */
  public void delTempFile();
}
