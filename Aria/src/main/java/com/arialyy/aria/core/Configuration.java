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
package com.arialyy.aria.core;

/**
 * Created by AriaL on 2016/12/8.
 * 信息配置
 */
class Configuration {
  private static final String TAG = "Configuration";
  private static final String CONFIG_FILE = "/Aria/ADConfig.properties";
  private static final String CONFIG_KEY = "ARIA_CONFIG";

  /**
   * 通用配置
   */
  static class BaseConfig {
    public boolean isOpenBreadCast = false;
    /**
     * 任务队列最大任务数， 默认为2
     */
    public int maxQueueNum = 2;
    /**
     * 下载失败，重试次数，默认为10
     */
    public int reTryNum = 10;
    /**
     * 设置重试间隔，单位为毫秒，默认2000毫秒
     */
    public long reTryInterval = 2000;
    /**
     * 设置url连接超时时间，单位为毫秒，默认5000毫秒
     */
    public long connectTimeOut = 5000;
  }

  /**
   * 下载配置
   */
  public static class DownloadConfig extends BaseConfig {
    /**
     * 设置IO流读取时间，单位为毫秒，默认20000毫秒，该时间不能少于10000毫秒
     */
    public long readTimeout = 20 * 1000;
    /**
     * 设置写文件buff大小，该数值大小不能小于2048，数值变小，下载速度会变慢
     */
    public int buffSize = 8192;
    /**
     * 设置https ca 证书信息；path 为assets目录下的CA证书完整路径
     */
    public String caPath;
    /**
     * name 为CA证书名
     */
    public String caName;
  }

  /**
   * 上传配置
   */
  public static class UploadConfig extends BaseConfig {

  }
}
