/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * 文件操作工具类
 */
public class FileUtil {
  private static final String TAG = "FileUtil";

  /**
   * 合并文件
   *
   * @param targetPath 目标文件
   * @param subPaths 碎片文件路径
   * @return {@code true} 合并成功，{@code false}合并失败
   */
  public static boolean mergeFile(String targetPath, List<String> subPaths) {
    File file = new File(targetPath);
    FileOutputStream fos = null;
    FileChannel foc = null;
    try {
      if (!file.exists()) {
        file.createNewFile();
      }
      fos = new FileOutputStream(targetPath);
      foc = fos.getChannel();
      List<FileInputStream> streams = new LinkedList<>();
      for (String subPath : subPaths) {
        File f = new File(subPath);
        if (!f.exists()) {
          ALog.d(TAG, String.format("合并文件失败，文件【%s】不存在", subPath));
          for (FileInputStream fis : streams) {
            fis.close();
          }
          streams.clear();

          return false;
        }
        streams.add(new FileInputStream(subPath));
      }
      Enumeration<FileInputStream> en = Collections.enumeration(streams);
      SequenceInputStream sis = new SequenceInputStream(en);
      ReadableByteChannel fic = Channels.newChannel(sis);
      ByteBuffer bf = ByteBuffer.allocate(8196);
      while (fic.read(bf) != -1) {
        bf.flip();
        foc.write(bf);
        bf.compact();
      }
      fic.close();
      sis.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (foc != null) {
          foc.close();
        }
        if (fos != null) {
          fos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  /**
   * 分割文件
   *
   * @param filePath 被分割的文件路径
   * @param num 分割的块数
   */
  public static void splitFile(String filePath, int num) {
    try {
      final File file = new File(filePath);
      long size = file.length();
      FileInputStream fis = new FileInputStream(file);
      FileChannel fic = fis.getChannel();
      long j = 0;
      long block = size / num;
      for (int i = 0; i < num; i++) {
        if (i == num - 1) {
          block = size - block * (num - 1);
        }
        String subPath = file.getPath() + "." + i + ".part";
        ALog.d(TAG, String.format("block = %s", block));
        File subFile = new File(subPath);
        if (!subFile.exists()) {
          subFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(subFile);
        FileChannel sfoc = fos.getChannel();
        ByteBuffer bf = ByteBuffer.allocate(8196);
        int len;
        //fis.skip(block * i);
        while ((len = fic.read(bf)) != -1) {
          bf.flip();
          sfoc.write(bf);
          bf.compact();
          j += len;
          if (j >= block * (i + 1)) {
            break;
          }
        }
        ALog.d(TAG, String.format("len = %s", subFile.length()));
        fos.close();
        sfoc.close();
      }
      fis.close();
      fic.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
