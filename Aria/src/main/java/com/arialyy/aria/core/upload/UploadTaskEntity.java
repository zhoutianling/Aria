package com.arialyy.aria.core.upload;

import com.arialyy.aria.core.RequestEnum;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aria.Lao on 2017/2/9.
 */

public class UploadTaskEntity {
  public UploadEntity uploadEntity;
  public RequestEnum requestEnum = RequestEnum.GET;
  public String uploadUrl; //上传路径
  public String uploadKey;  //文件上传需要的key
  public String contentType = "multipart/form-data"; //上传的文件类型
  public Map<String, String> headers = new HashMap<>();

  public UploadTaskEntity(UploadEntity downloadEntity) {
    this.uploadEntity = downloadEntity;
  }
}
