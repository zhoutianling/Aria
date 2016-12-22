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

import android.os.Environment;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lyy on 2016/9/27.
 * 下载路径帮助类
 */
public class PathUtil {

  /**
   * 下载链接转换保存路径
   *
   * @param downloadUrl 下载链接
   * @return 保存路径
   */
  public static String urlconvertPath(String downloadUrl) {
    return Environment.getDownloadCacheDirectory().getPath() + "/" + StringToHashKey(downloadUrl);
  }

  /**
   * 字符串转换为hash码
   */
  public static String StringToHashKey(String str) {
    String cacheKey;
    try {
      final MessageDigest mDigest = MessageDigest.getInstance("MD5");
      mDigest.update(str.getBytes());
      cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      cacheKey = String.valueOf(str.hashCode());
    }
    return cacheKey;
  }

  /**
   * 将普通字符串转换为16位进制字符串
   */
  public static String bytesToHexString(byte[] src) {
    StringBuilder stringBuilder = new StringBuilder("0x");
    if (src == null || src.length <= 0) {
      return null;
    }
    char[] buffer = new char[2];
    for (byte aSrc : src) {
      buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
      buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
      stringBuilder.append(buffer);
    }
    return stringBuilder.toString();
  }
}