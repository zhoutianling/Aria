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
package com.arialyy.aria.util;

import android.util.Log;
import com.arialyy.aria.core.DownloadManager;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by AriaL on 2016/12/8.
 * 信息配置
 */
public class Configuration {
  private static final String TAG               = "Configuration";
  private static final String CONFIG_FILE       = "/Aria/ADConfig.properties";
  /**
   * 当前调度器最大下载数，默认最大下载数为 “2”
   */
  private static final String DOWNLOAD_NUM      = "DOWNLOAD_NUM";
  /**
   * 失败重试次数，默认最多重试 10 次
   */
  private static final String RE_TRY_NUM        = "RE_TRY_NUM";
  /**
   * 是否打开下载广播，默认 false
   */
  private static final String OPEN_BROADCAST    = "OPEN_BROADCAST";
  /**
   * 失败重试间隔时间，默认 4000 ms
   */
  private static final String RE_TRY_INTERVAL   = "RE_TRY_INTERVAL";
  /**
   * 超时时间，默认 10000 ms
   */
  private static final String DOWNLOAD_TIME_OUT = "DOWNLOAD_TIME_OUT";
  public static boolean isOpenBreadCast = false;

  private static       Configuration INSTANCE    = null;
  private              File          mConfigFile = null;
  private static final Object        LOCK        = new Object();

  public static Configuration getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new Configuration();
      }
    }
    return INSTANCE;
  }

  private Configuration() {
    mConfigFile = new File(DownloadManager.APP.getFilesDir().getPath() + CONFIG_FILE);
    try {
      if (!mConfigFile.exists()) {
        mConfigFile.getParentFile().mkdirs();
        mConfigFile.createNewFile();
        init();
      }else {
        isOpenBreadCast = isOpenBroadcast();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void init() {
    Map<String, String> config = new WeakHashMap<>();
    config.put(DOWNLOAD_NUM, 2 + "");
    config.put(RE_TRY_NUM, 3 + "");
    config.put(OPEN_BROADCAST, false + "");
    config.put(RE_TRY_INTERVAL, 4000 + "");
    config.put(DOWNLOAD_TIME_OUT, 10000 + "");
    saveConfig(config);
  }

  private void saveConfig(Map<String, String> config) {
    if (config == null || config.size() == 0) {
      return;
    }
    Properties  properties = CommonUtil.loadConfig(mConfigFile);
    Set<String> keys       = config.keySet();
    for (String key : keys) {
      properties.setProperty(key, config.get(key));
    }
    CommonUtil.saveConfig(mConfigFile, properties);
  }

  private void save(String key, String value) {
    Map<String, String> map = new WeakHashMap<>();
    map.put(key, value);
    saveConfig(map);
  }

  /**
   * 获取下载超时时间
   *
   * @return 默认4000ms
   */
  public int getTimeOut() {
    return Integer.parseInt(CommonUtil.loadConfig(mConfigFile).getProperty(DOWNLOAD_TIME_OUT));
  }

  /**
   * 设置重试间隔
   */
  public void setTimeOut(int timeOut) {
    if (timeOut < 10000) {
      Log.w(TAG, "下载超时时间不能小于 10000 ms");
      return;
    }
    save(DOWNLOAD_TIME_OUT, timeOut + "");
  }

  /**
   * 获取失败重试间隔时间
   *
   * @return 默认4000ms
   */
  public int getReTryInterval() {
    return Integer.parseInt(CommonUtil.loadConfig(mConfigFile).getProperty(RE_TRY_INTERVAL));
  }

  /**
   * 设置重试间隔
   */
  public void setReTryInterval(int reTryInterval) {
    if (reTryInterval < 4000) {
      Log.w(TAG, "重试间隔不能小于4000ms");
      return;
    }
    save(RE_TRY_INTERVAL, reTryInterval + "");
  }

  /**
   * 获取最大下载数
   *
   * @return 默认返回2
   */
  public int getDownloadNum() {
    return Integer.parseInt(CommonUtil.loadConfig(mConfigFile).getProperty(DOWNLOAD_NUM));
  }

  /**
   * 设置最大下载数
   */
  public void setDownloadNum(int downloadNum) {
    if (downloadNum < 1) {
      Log.w(TAG, "最大下载数不能小于1");
      return;
    }
    save(DOWNLOAD_NUM, downloadNum + "");
  }

  /**
   * 获取最大重试数
   *
   * @return 默认返回 10
   */
  public int getReTryNum() {
    return Integer.parseInt(CommonUtil.loadConfig(mConfigFile).getProperty(RE_TRY_NUM));
  }

  /**
   * 设置重试数
   */
  public void setReTryNum(int reTryNum) {
    if (reTryNum < 1) {
      Log.w(TAG, "最大下载数不能小于1");
      return;
    }
    save(RE_TRY_NUM, reTryNum + "");
  }

  /**
   * 是否打开下载广播
   *
   * @return 默认false
   */
  public boolean isOpenBroadcast() {
    return Boolean.parseBoolean(CommonUtil.loadConfig(mConfigFile).getProperty(RE_TRY_NUM));
  }

  /**
   * 设置是否打开下载广播
   */
  public void setOpenBroadcast(boolean openBroadcast) {
    isOpenBreadCast = openBroadcast;
    save(OPEN_BROADCAST, openBroadcast + "");
  }
}
