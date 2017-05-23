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

import android.util.Log;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.ReflectionUtil;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;

/**
 * Created by AriaL on 2016/12/8.
 * 信息配置
 */
class Configuration {
  static final String DOWNLOAD_CONFIG_FILE = "/Aria/DownloadConfig.properties";
  static final String UPLOAD_CONFIG_FILE = "/Aria/UploadConfig.properties";

  /**
   * 通用配置
   */
  public static class BaseConfig {
    /**
     * 旧任务数
     */
    public static int oldMaxTaskNum = 2;

    /**
     * 是否发送任务广播，true，发送
     */
    boolean isOpenBreadCast = false;
    /**
     * 任务队列最大任务数， 默认为2
     */
    int maxTaskNum = 2;
    /**
     * 下载失败，重试次数，默认为10
     */
    int reTryNum = 10;
    /**
     * 设置重试间隔，单位为毫秒，默认2000毫秒
     */
    long reTryInterval = 2000;
    /**
     * 设置url连接超时时间，单位为毫秒，默认5000毫秒
     */
    long connectTimeOut = 5000;

    public boolean isOpenBreadCast() {
      return isOpenBreadCast;
    }

    public BaseConfig setOpenBreadCast(boolean openBreadCast) {
      isOpenBreadCast = openBreadCast;
      saveKey("isOpenBreadCast", openBreadCast + "");
      return this;
    }

    public int getMaxTaskNum() {
      return maxTaskNum;
    }

    public BaseConfig setMaxTaskNum(int maxTaskNum) {
      oldMaxTaskNum = this.maxTaskNum;
      this.maxTaskNum = maxTaskNum;
      saveKey("maxTaskNum", maxTaskNum + "");
      DownloadTaskQueue.getInstance().setDownloadNum(maxTaskNum);
      return this;
    }

    public int getReTryNum() {
      return reTryNum;
    }

    public BaseConfig setReTryNum(int reTryNum) {
      this.reTryNum = reTryNum;
      saveKey("reTryNum", reTryNum + "");
      return this;
    }

    public long getReTryInterval() {
      return reTryInterval;
    }

    public BaseConfig setReTryInterval(long reTryInterval) {
      this.reTryInterval = reTryInterval;
      saveKey("reTryInterval", reTryInterval + "");
      return this;
    }

    public long getConnectTimeOut() {
      return connectTimeOut;
    }

    public BaseConfig setConnectTimeOut(long connectTimeOut) {
      this.connectTimeOut = connectTimeOut;
      saveKey("connectTimeOut", connectTimeOut + "");
      return this;
    }

    /**
     * 保存key
     */
    void saveKey(String key, String value) {
      boolean isDownload = this instanceof DownloadConfig;
      File file = new File(
          AriaManager.APP.getFilesDir().getPath() + (isDownload ? DOWNLOAD_CONFIG_FILE
              : UPLOAD_CONFIG_FILE));
      if (file.exists()) {
        Properties properties = CommonUtil.loadConfig(file);
        properties.setProperty(key, value);
        CommonUtil.saveConfig(file, properties);
      }
    }

    /**
     * 加载配置
     */
    void loadConfig() {
      boolean isDownload = this instanceof DownloadConfig;
      File file = new File(
          AriaManager.APP.getFilesDir().getPath() + (isDownload ? DOWNLOAD_CONFIG_FILE
              : UPLOAD_CONFIG_FILE));
      if (file.exists()) {
        Properties properties = CommonUtil.loadConfig(file);
        List<Field> fields = ReflectionUtil.getAllFields(getClass());
        try {
          for (Field field : fields) {
            int m = field.getModifiers();
            if (Modifier.isFinal(m) || Modifier.isStatic(m)) {
              continue;
            }
            field.setAccessible(true);
            String value = properties.getProperty(field.getName());
            Class<?> type = field.getType();
            if (type == String.class) {
              field.set(this, value);
            } else if (type == int.class || type == Integer.class) {
              field.setInt(this, Integer.parseInt(value));
            } else if (type == float.class || type == Float.class) {
              field.setFloat(this, Float.parseFloat(value));
            } else if (type == double.class || type == Double.class) {
              field.setDouble(this, Double.parseDouble(value));
            } else if (type == long.class || type == Long.class) {
              field.setLong(this, Long.parseLong(value));
            } else if (type == boolean.class || type == Boolean.class) {
              field.setBoolean(this, Boolean.parseBoolean(value));
            }
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }

    /**
     * 保存配置
     */
    void saveAll() {
      List<Field> fields = ReflectionUtil.getAllFields(getClass());
      boolean isDownload = this instanceof DownloadConfig;
      try {
        File file = new File(
            AriaManager.APP.getFilesDir().getPath() + (isDownload ? DOWNLOAD_CONFIG_FILE
                : UPLOAD_CONFIG_FILE));
        Properties properties = CommonUtil.loadConfig(file);
        for (Field field : fields) {
          int m = field.getModifiers();
          if (Modifier.isFinal(m) || Modifier.isStatic(m)) {
            continue;
          }
          field.setAccessible(true);
          properties.setProperty(field.getName(), field.get(this) + "");
        }
        CommonUtil.saveConfig(file, properties);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 下载配置
   */
  public static class DownloadConfig extends BaseConfig {
    /**
     * 设置IO流读取时间，单位为毫秒，默认20000毫秒，该时间不能少于10000毫秒
     */
    long iOTimeOut = 20 * 1000;
    /**
     * 设置写文件buff大小，该数值大小不能小于2048，数值变小，下载速度会变慢
     */
    int buffSize = 8192;
    /**
     * 设置https ca 证书信息；path 为assets目录下的CA证书完整路径
     */
    String caPath;
    /**
     * name 为CA证书名
     */
    String caName;
    /**
     * 下载线程数，下载线程数不能小于1
     */
    int threadNum = 3;

    public long getiOTimeOut() {
      return iOTimeOut;
    }

    public DownloadConfig setiOTimeOut(long iOTimeOut) {
      this.iOTimeOut = iOTimeOut;
      saveKey("iOTimeOut", iOTimeOut + "");
      return this;
    }

    public int getBuffSize() {
      return buffSize;
    }

    public DownloadConfig setBuffSize(int buffSize) {
      this.buffSize = buffSize;
      saveKey("buffSize", buffSize + "");
      return this;
    }

    public String getCaPath() {
      return caPath;
    }

    public DownloadConfig setCaPath(String caPath) {
      this.caPath = caPath;
      saveKey("caPath", caPath);
      return this;
    }

    public String getCaName() {
      return caName;
    }

    public DownloadConfig setCaName(String caName) {
      this.caName = caName;
      saveKey("caName", caName);
      return this;
    }

    public int getThreadNum() {
      return threadNum;
    }

    private DownloadConfig() {
      loadConfig();
    }

    private static DownloadConfig INSTANCE = null;

    static DownloadConfig getInstance() {
      if (INSTANCE == null) {
        synchronized (DownloadConfig.class) {
          INSTANCE = new DownloadConfig();
        }
      }
      return INSTANCE;
    }
  }

  /**
   * 上传配置
   */
  public static class UploadConfig extends BaseConfig {

    private UploadConfig() {
      loadConfig();
    }

    private static UploadConfig INSTANCE = null;

    static UploadConfig getInstance() {
      if (INSTANCE == null) {
        synchronized (DownloadConfig.class) {
          INSTANCE = new UploadConfig();
        }
      }
      return INSTANCE;
    }
  }
}
