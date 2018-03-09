/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.inf;

import android.support.annotation.NonNull;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DownloadEntity;
import java.util.Map;

/**
 * Created by laoyuyu on 2018/3/9.
 * HTTP Header功能接口
 */
public interface IHttpHeaderTarget<TARGET extends ITarget> {

  /**
   * 给url请求添加Header数据
   * 如果新的header数据和数据保存的不一致，则更新数据库中对应的header数据
   *
   * @param key header对应的key
   * @param value header对应的value
   */
  TARGET addHeader(@NonNull String key, @NonNull String value);

  /**
   * 给url请求添加一组header数据
   * 如果新的header数据和数据保存的不一致，则更新数据库中对应的header数据
   *
   * @param headers 一组http header数据
   */
  TARGET addHeaders(Map<String, String> headers);

  /**
   * 设置HTTP请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  TARGET setRequestMode(RequestEnum requestEnum);

  /**
   * 如果你的下载链接的header中含有md5码信息，那么你可以通过设置key，来获取从header获取该md5码信息。
   * key默认值为：Content-MD5
   * 获取md5信息：{@link DownloadEntity#getMd5Code()}
   */
  TARGET setHeaderMd5Key(String md5Key);

  /**
   * 如果你的文件长度是放在header中，那么你需要配置key来让Aria知道正确的文件长度
   * key默认值为：Content-Length
   */
  TARGET setHeaderContentLengthKey(String contentLength);

  /**
   * 如果你的下载链接的header中含有文件描述信息，那么你可以通过设置key，来获取从header获取该文件描述信息。
   * key默认值为：Content-Disposition
   * 获取文件描述信息：{@link DownloadEntity#getDisposition()}
   */
  TARGET setHeaderDispositionKey(String dispositionKey);

  /**
   * 从文件描述信息{@link #setHeaderDispositionKey(String)}中含有文件名信息，你可以通过设置key来获取header中的文件名
   * key默认值为：attachment;filename
   * 获取文件名信息：{@link DownloadEntity#getServerFileName()}
   */
  TARGET setHeaderDispositionFileKey(String dispositionFileKey);
}
