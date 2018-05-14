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

import android.text.TextUtils;
import com.arialyy.aria.util.ALog;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by lyy on 2017/5/22.
 * 读取配置文件
 */
class ConfigHelper extends DefaultHandler {
  private final String TAG = "ConfigHelper";

  private boolean isDownloadConfig = false, isUploadConfig = false, isAppConfig = false;
  private Configuration.DownloadConfig mDownloadConfig = Configuration.DownloadConfig.getInstance();
  private Configuration.UploadConfig mUploadConfig = Configuration.UploadConfig.getInstance();
  private Configuration.AppConfig mAppConfig = Configuration.AppConfig.getInstance();

  @Override public void startDocument() throws SAXException {
    super.startDocument();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    if (qName.equals("download")) {
      isDownloadConfig = true;
      isUploadConfig = false;
      isAppConfig = false;
    } else if (qName.equals("upload")) {
      isUploadConfig = true;
      isDownloadConfig = false;
      isAppConfig = false;
    } else if (qName.equals("app")) {
      isUploadConfig = false;
      isDownloadConfig = false;
      isAppConfig = true;
    }

    if (isDownloadConfig || isUploadConfig) {

      String value = attributes.getValue("value");
      switch (qName) {
        case "threadNum":
          loadThreadNum(value);
          break;
        case "maxTaskNum":
          loadMaxQueue(value);
          break;
        case "reTryNum":
          loadReTry(value);
          break;
        case "connectTimeOut":
          loadConnectTime(value);
          break;
        case "iOTimeOut":
          loadIOTimeout(value);
          break;
        case "reTryInterval":
          loadReTryInterval(value);
          break;
        case "buffSize":
          loadBuffSize(value);
          break;
        case "ca":
          String caName = attributes.getValue("name");
          String caPath = attributes.getValue("path");
          loadCA(caName, caPath);
          break;
        case "convertSpeed":
          loadConvertSpeed(value);
          break;
        case "maxSpeed":
          loadMaxSpeed(value);
          break;
        case "queueMod":
          loadQueueMod(value);
          break;
        case "updateInterval":
          loadUpdateInterval(value);
          break;
        case "notNetRetry":
          loadNotNetRetry(value);
          break;
      }
    } else if (isAppConfig) {
      String value = attributes.getValue("value");
      switch (qName) {
        case "useAriaCrashHandler":
          loadUseAriaCrashHandler(value);
          break;
        case "logLevel":
          loadLogLevel(value);
          break;
      }
    }
  }

  private void loadNotNetRetry(String value) {
    if (isDownloadConfig) {
      mDownloadConfig.notNetRetry = checkBoolean(value) ? Boolean.valueOf(value) : false;
    }
    if (isUploadConfig) {
      mUploadConfig.notNetRetry = checkBoolean(value) ? Boolean.valueOf(value) : false;
    }
  }

  private void loadLogLevel(String value) {
    int level;
    try {
      level = Integer.parseInt(value);
    } catch (NumberFormatException e) {
      e.printStackTrace();
      level = ALog.LOG_LEVEL_VERBOSE;
    }
    if (level < ALog.LOG_LEVEL_VERBOSE || level > ALog.LOG_CLOSE) {
      ALog.w(TAG, "level【" + level + "】错误");
      mAppConfig.logLevel = ALog.LOG_LEVEL_VERBOSE;
    } else {
      mAppConfig.logLevel = level;
    }
  }

  private void loadUseAriaCrashHandler(String value) {
    if (checkBoolean(value)) {
      mAppConfig.useAriaCrashHandler = Boolean.parseBoolean(value);
    } else {
      ALog.w(TAG, "useAriaCrashHandler【" + value + "】错误");
      mAppConfig.useAriaCrashHandler = true;
    }
  }

  private void loadUpdateInterval(String value) {
    long temp = checkLong(value) ? Long.parseLong(value) : 1000;
    if (isDownloadConfig) {
      mDownloadConfig.updateInterval = temp;
    }
    if (isUploadConfig) {
      mUploadConfig.updateInterval = temp;
    }
  }

  private void loadQueueMod(String value) {
    String mod = "now";
    if (!TextUtils.isEmpty(value) && (value.equalsIgnoreCase("now") || value.equalsIgnoreCase(
        "wait"))) {
      mod = value;
    }
    if (isDownloadConfig) {
      mDownloadConfig.queueMod = mod;
    }
    if (isUploadConfig) {
      mUploadConfig.queueMod = mod;
    }
  }

  private void loadMaxSpeed(String value) {
    int maxSpeed = checkInt(value) ? Integer.parseInt(value) : 0;
    if (isDownloadConfig) {
      mDownloadConfig.maxSpeed = maxSpeed;
    }
  }

  private void loadConvertSpeed(String value) {
    boolean open = true;
    if (checkBoolean(value)) {
      open = Boolean.parseBoolean(value);
    }

    if (isDownloadConfig) {
      mDownloadConfig.isConvertSpeed = open;
    }
    if (isUploadConfig) {
      mUploadConfig.isConvertSpeed = open;
    }
  }

  private void loadReTryInterval(String value) {
    int time = checkInt(value) ? Integer.parseInt(value) : 2 * 1000;

    if (time < 2 * 1000) {
      time = 2 * 1000;
    }

    if (isDownloadConfig) {
      mDownloadConfig.reTryInterval = time;
    }
  }

  private void loadCA(String name, String path) {
    if (isDownloadConfig) {
      mDownloadConfig.caName = name;
      mDownloadConfig.caPath = path;
    }
  }

  private void loadBuffSize(String value) {
    int buffSize = checkInt(value) ? Integer.parseInt(value) : 8192;

    if (buffSize < 2048) {
      buffSize = 2048;
    }

    if (isDownloadConfig) {
      mDownloadConfig.buffSize = buffSize;
    }

    if (isUploadConfig){
      mUploadConfig.buffSize = buffSize;
    }
  }

  private void loadIOTimeout(String value) {
    int time = checkInt(value) ? Integer.parseInt(value) : 10 * 1000;

    if (time < 10 * 1000) {
      time = 10 * 1000;
    }

    if (isDownloadConfig) {
      mDownloadConfig.iOTimeOut = time;
    }

    if (isUploadConfig) {
      mUploadConfig.iOTimeOut = time;
    }
  }

  private void loadConnectTime(String value) {
    int time = checkInt(value) ? Integer.parseInt(value) : 5 * 1000;

    if (isDownloadConfig) {
      mDownloadConfig.connectTimeOut = time;
    }
    if (isUploadConfig) {
      mUploadConfig.connectTimeOut = time;
    }
  }

  private void loadReTry(String value) {
    int num = checkInt(value) ? Integer.parseInt(value) : 0;

    if (isDownloadConfig) {
      mDownloadConfig.reTryNum = num;
    }
    if (isUploadConfig) {
      mUploadConfig.reTryNum = num;
    }
  }

  private void loadMaxQueue(String value) {
    int num = checkInt(value) ? Integer.parseInt(value) : 2;
    if (num < 1) {
      ALog.w(TAG, "任务队列数不能小于 1");
      num = 2;
    }
    if (isDownloadConfig) {
      mDownloadConfig.maxTaskNum = num;
    }
    if (isUploadConfig) {
      mUploadConfig.maxTaskNum = num;
    }
  }

  private void loadThreadNum(String value) {
    int num = checkInt(value) ? Integer.parseInt(value) : 3;
    if (num < 1) {
      ALog.e(TAG, "下载线程数不能小于 1");
      num = 1;
    }
    if (isDownloadConfig) {
      mDownloadConfig.threadNum = num;
    }
  }

  /**
   * 检查是否int值是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkInt(String value) {
    if (TextUtils.isEmpty(value)) {
      return false;
    }
    try {
      Integer l = Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 检查是否long值是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkLong(String value) {
    if (TextUtils.isEmpty(value)) {
      return false;
    }
    try {
      Long l = Long.parseLong(value);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 检查boolean值是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkBoolean(String value) {
    return !TextUtils.isEmpty(value) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase(
        "false"));
  }

  @Override public void characters(char[] ch, int start, int length) throws SAXException {
    super.characters(ch, start, length);
  }

  @Override public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName);
  }

  @Override public void endDocument() throws SAXException {
    super.endDocument();
    mDownloadConfig.saveAll();
    mUploadConfig.saveAll();
    mAppConfig.saveAll();
  }
}
