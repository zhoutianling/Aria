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
package com.arialyy.aria.core.download.downloader.ftp;

import com.arialyy.aria.core.download.DownloadTaskEntity;

/**
 * Created by Aria.Lao on 2017/7/24.
 * ftp下载信息实体
 */
class FtpConfigEntity {
  //下载文件大小
  long FILE_SIZE;
  //子线程启动下载位置
  long START_LOCATION;
  //下载路径
  String PATH;
  DownloadTaskEntity TASK_ENTITY;
  //FTP 服务器地址
  String SERVER_IP;
  //FTP 服务器端口
  String PORT;
  //FTP服务器地址
  String SERVER_FILE_PATH;
}
