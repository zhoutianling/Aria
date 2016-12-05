/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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

package com.arialyy.downloadutil.util;

import android.util.Log;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.command.CmdFactory;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Created by lyy on 2016/1/22.
 */
public class CommonUtil {
  private static final String TAG = "util";

  public static IDownloadCmd createCmd(DownloadEntity entity, int cmd) {
    return CmdFactory.getInstance().createCmd(entity, cmd);
  }

  /**
   * 获取类里面的所在字段
   */
  public static Field[] getFields(Class clazz) {
    Field[] fields = null;
    fields = clazz.getDeclaredFields();
    if (fields == null || fields.length == 0) {
      Class superClazz = clazz.getSuperclass();
      if (superClazz != null) {
        fields = getFields(superClazz);
      }
    }
    return fields;
  }

  /**
   * 获取类里面的指定对象，如果该类没有则从父类查询
   */
  public static Field getField(Class clazz, String name) {
    Field field = null;
    try {
      field = clazz.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      try {
        field = clazz.getField(name);
      } catch (NoSuchFieldException e1) {
        if (clazz.getSuperclass() == null) {
          return field;
        } else {
          field = getField(clazz.getSuperclass(), name);
        }
      }
    }
    if (field != null) {
      field.setAccessible(true);
    }
    return field;
  }

  /**
   * 字符串转hashcode
   */
  public static int keyToHashCode(String str) {
    int total = 0;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (ch == '-') ch = (char) 28; // does not contain the same last 5 bits as any letter
      if (ch == '\'') ch = (char) 29; // nor this
      total = (total * 33) + (ch & 0x1F);
    }
    return total;
  }

  /**
   * 将key转换为16进制码
   *
   * @param key 缓存的key
   * @return 转换后的key的值, 系统便是通过该key来读写缓存
   */
  public static String keyToHashKey(String key) {
    String cacheKey;
    try {
      final MessageDigest mDigest = MessageDigest.getInstance("MD5");
      mDigest.update(key.getBytes());
      cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      cacheKey = String.valueOf(key.hashCode());
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

  /**
   * 获取对象名
   *
   * @param obj 对象
   * @return 对象名
   */
  public static String getClassName(Object obj) {
    String arrays[] = obj.getClass().getName().split("\\.");
    return arrays[arrays.length - 1];
  }

  /**
   * 获取对象名
   *
   * @param clazz clazz
   * @return 对象名
   */
  public static String getClassName(Class clazz) {
    String arrays[] = clazz.getName().split("\\.");
    return arrays[arrays.length - 1];
  }

  /**
   * 格式化文件大小
   *
   * @param size file.length() 获取文件大小
   */
  public static String formatFileSize(double size) {
    double kiloByte = size / 1024;
    if (kiloByte < 1) {
      return size + "b";
    }

    double megaByte = kiloByte / 1024;
    if (megaByte < 1) {
      BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
      return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "k";
    }

    double gigaByte = megaByte / 1024;
    if (gigaByte < 1) {
      BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
      return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "m";
    }

    double teraBytes = gigaByte / 1024;
    if (teraBytes < 1) {
      BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
      return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "g";
    }
    BigDecimal result4 = new BigDecimal(teraBytes);
    return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "t";
  }

  /**
   * 创建目录 当目录不存在的时候创建文件，否则返回false
   */
  public static boolean createDir(String path) {
    File file = new File(path);
    if (!file.exists()) {
      if (!file.mkdirs()) {
        Log.d(TAG, "创建失败，请检查路径和是否配置文件权限！");
        return false;
      }
      return true;
    }
    return false;
  }

  /**
   * 创建文件 当文件不存在的时候就创建一个文件，否则直接返回文件
   */
  public static File createFile(String path) {
    File file = new File(path);
    if (!file.getParentFile().exists()) {
      Log.d(TAG, "目标文件所在路径不存在，准备创建……");
      if (!createDir(file.getParent())) {
        Log.d(TAG, "创建目录文件所在的目录失败！文件路径【" + path + "】");
      }
    }
    // 创建目标文件
    try {
      if (!file.exists()) {
        if (file.createNewFile()) {
          Log.d(TAG, "创建文件成功:" + file.getAbsolutePath());
        }
        return file;
      } else {
        return file;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 设置打印的异常格式
   */
  public static String getPrintException(Throwable ex) {
    StringBuilder err = new StringBuilder();
    err.append("ExceptionDetailed:\n");
    err.append("====================Exception Info====================\n");
    err.append(ex.toString());
    err.append("\n");
    StackTraceElement[] stack = ex.getStackTrace();
    for (StackTraceElement stackTraceElement : stack) {
      err.append(stackTraceElement.toString()).append("\n");
    }
    Throwable cause = ex.getCause();
    if (cause != null) {
      err.append("【Caused by】: ");
      err.append(cause.toString());
      err.append("\n");
      StackTraceElement[] stackTrace = cause.getStackTrace();
      for (StackTraceElement stackTraceElement : stackTrace) {
        err.append(stackTraceElement.toString()).append("\n");
      }
    }
    err.append("===================================================");
    return err.toString();
  }

  /**
   * 读取下载配置文件
   */
  public static Properties loadConfig(File file) {
    Properties properties = new Properties();
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(file);
      properties.load(fis);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return properties;
  }

  /**
   * 保存配置文件
   */
  public static void saveConfig(File file, Properties properties) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, false);
      properties.store(fos, null);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) {
          fos.flush();
          fos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}