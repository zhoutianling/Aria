package com.arialyy.aria.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aria.Lao on 2017/1/23.
 * 下载任务实体
 */
public class DownloadTaskEntity {

  public DownloadEntity downloadEntity;
  public RequestEnum requestEnum = RequestEnum.GET;
  public Map<String, String> headers = new HashMap<>();

  public DownloadTaskEntity(DownloadEntity downloadEntity) {
    this.downloadEntity = downloadEntity;
  }
}
