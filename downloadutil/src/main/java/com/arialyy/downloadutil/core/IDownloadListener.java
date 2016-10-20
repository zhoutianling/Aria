package com.arialyy.downloadutil.core;

/**
 * 下载监听
 */
public interface IDownloadListener {
  /**
   * 取消下载
   */
  public void onCancel();

  /**
   * 下载失败
   */
  public void onFail();

  /**
   * 预处理
   */
  public void onPre();

  /**
   * 预处理完成,准备下载---开始下载之间
   */
  public void onPostPre(long fileSize);

  /**
   * 下载监听
   */
  public void onProgress(long currentLocation);

  /**
   * 单一线程的结束位置
   */
  public void onChildComplete(long finishLocation);

  /**
   * 开始
   */
  public void onStart(long startLocation);

  /**
   * 子程恢复下载的位置
   */
  public void onChildResume(long resumeLocation);

  /**
   * 恢复位置
   */
  public void onResume(long resumeLocation);

  /**
   * 停止
   */
  public void onStop(long stopLocation);

  /**
   * 下载完成
   */
  public void onComplete();
}