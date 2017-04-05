# Aria
![图标](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/app/src/main/res/mipmap-hdpi/ic_launcher.png)</br>
Aria，让上传、下载更容易实现</br>
+ Aria有以下特点：
 - 简单
 - 可在Dialog、popupWindow等组件中使用
 - 支持多线程、多任务下载
 - 支持多任务自动调度
 - 可以直接获取速度
 - 支持https地址下载
 - 支持上传操作

Aria怎样使用？
* [下载](#使用)
* [上传](#上传)

如果你觉得Aria对你有帮助，您的star和issues将是对我最大支持.`^_^`

## 下载
[![Download](https://api.bintray.com/packages/arialyy/maven/Aria/images/download.svg)](https://bintray.com/arialyy/maven/Aria/_latestVersion)</br>
```java
compile 'com.arialyy.aria:Aria:3.0.0'
```

## 示例
![多任务下载](https://github.com/AriaLyy/DownloadUtil/blob/master/img/download_img.gif)
![上传](https://github.com/AriaLyy/DownloadUtil/blob/master/img/sing_upload.gif)

## 性能
![性能展示](https://github.com/AriaLyy/DownloadUtil/blob/master/img/performance.png)

***
## 使用
由于Aria涉及到文件和网络的操作，因此需要你在manifest文件中添加以下权限
```xml
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## 使用Aria进行下载
* 添加任务（不进行下载），当其他下载任务完成时，将自动下载等待中的任务
  ```java
  Aria.download(this)
      .load(DOWNLOAD_URL)
      .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")	//文件保存路径
      .add();
  ```

* 下载

  ```java
  Aria.download(this)
      .load(DOWNLOAD_URL)     //读取下载地址
      .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")    //设置文件保存的完整路径
      .start();   //启动下载
  ```
* 暂停

  ```java
  Aria.download(this).load(DOWNLOAD_URL).pause();
  ```
* 恢复下载

  ```java
  Aria.download(this).load(DOWNLOAD_URL).resume();
  ```
* 取消下载

  ```java
  Aria.download(this).load(DOWNLOAD_URL).cancel();
  ```

### 二、如果你希望读取下载进度或下载信息，那么你需要创建事件类，并在onResume(Activity、Fragment)或构造函数(Dialog、PopupWindow)，将该事件类注册到Aria管理器。
* 创建事件类

  ```java
  final static class MySchedulerListener extends Aria.DownloadSchedulerListener{
    @Override public void onTaskPre(DownloadTask task) {
      super.onTaskPre(task);
    }

    @Override public void onTaskStop(DownloadTask task) {
      super.onTaskStop(task);
    }

    @Override public void onTaskCancel(DownloadTask task) {
      super.onTaskCancel(task);
    }

    @Override public void onTaskRunning(DownloadTask task) {
      super.onTaskRunning(task);
    }
  }
  ```

* 将事件注册到Aria

  ```java
  @Override protected void onResume() {
    super.onResume();
    Aria.whit(this).addSchedulerListener(new MySchedulerListener());
  }
  ```

### 关于下载的其它api
[Download API](https://github.com/AriaLyy/Aria/blob/master/DownloadApi.md)

**tips:为了防止内存泄露的情况，事件类需要使用staic进行修饰**

## 上传
 * 添加任务(只添加，不上传)

 ```java
 Aria.upload(this)
     .load(filePath)     //文件路径
     .setUploadUrl(uploadUrl)  //上传路径
     .setAttachment(fileKey)   //服务器读取文件的key
     .add();
 ```

 * 上传

 ```java
 Aria.upload(this)
     .load(filePath)     //文件路径
     .setUploadUrl(uploadUrl)  //上传路径
     .setAttachment(fileKey)   //服务器读取文件的key
     .start();
 ```
 * 取消上传

 ```java
 Aria.upload(this)
     .load(filePath)
     .cancel();
 ```

## 其他
 有任何问题，可以在[issues](https://github.com/AriaLyy/Aria/issues)给我留言反馈。

***

## 开发日志
  + v_3.0.0 添加上传任务支持，修复一些已发现的bug
  + v_2.4.4 修复不支持断点的下载链接拿不到文件大小的问题
  + v_2.4.3 修复404链接卡顿的问题
  + v_2.4.2 修复失败重试无效的bug
  + v_2.4.1 修复下载慢的问题，修复application、service 不能使用的问题
  + v_2.4.0 支持https链接下载
  + v_2.3.8 修复数据错乱的bug、添加fragment支持
  + v_2.3.6 添加dialog、popupWindow支持
  + v_2.3.3 添加断点支持、修改下载逻辑，让使用更加简单、修复一个内存泄露的bug
  + v_2.3.1 重命名为Aria，下载流程简化
  + v_2.1.1 增加，选择最大下载任务数接口

License
-------

    Copyright 2016 AriaLyy(https://github.com/AriaLyy/Aria)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
