# Aria
![图标](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/app/src/main/res/mipmap-hdpi/ic_launcher.png)</br>
Aria项目源于15年工作中遇到的一个文件下载管理的需求，当时被下载折磨的痛不欲生，从那时起便萌生了编写一个简单易用，稳当高效的下载框架，aria经历了1.0到3.0的开发，算是越来越接近当初所制定的目标了。

Aria有以下特点：
 + 简单、方便
   - 可以在Activity、Service、Fragment、Dialog、popupWindow、Notification等组件中使用
   - 支持任务自动调度，使用者不需要关心任务状态切换的逻辑
   - [通过Aria的事件，能很容易获取当前下载任务的下载状态](#下载状态获取)
   - [一句代码加可以获取当前的下载速度](#常用接口)
   - [一句代码就可以动态设置最大下载数](#代码中设置参数)
   - [通过修改配置文件很容易就能修改下载线程数](#配置文件设置参数)
 + 支持https地址下载
   - 在配置文件中很容易就可以设置CA证书的信息
 + 支持300、301、302重定向下载链接下载
 + 支持上传操作


Aria怎样使用？
* [下载](#使用)
* [上传](#上传)

如果你觉得Aria对你有帮助，您的star和issues将是对我最大支持.`^_^`

## 下载
[![Download](https://api.bintray.com/packages/arialyy/maven/Aria/images/download.svg)](https://bintray.com/arialyy/maven/Aria/_latestVersion)</br>
```java
compile 'com.arialyy.aria:Aria:3.1.0'
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

### 下载状态获取
如果你希望读取下载进度或下载信息，那么你需要创建事件类，并在onResume(Activity、Fragment)或构造函数(Dialog、PopupWindow)，将该事件类注册到Aria管理器。
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

### Aria参数配置
#### 配置文件设置参数
创建`aria_config.xml`文件，将其放在`assets`目录下，添加以下内容
```xml
<?xml version="1.0" encoding="utf-8"?>
<aria>

  <!--注意，修改该配置文件中的属性会覆盖代码中所设置的属性-->
  <download>

    <!--设置下载线程，线程下载数改变后，新的下载任务才会生效-->
    <threadNum value="4"/>

    <!--是否打开下载广播，默认为false-->
    <openBroadcast value="false"/>

    <!--设置下载队列最大任务数， 默认为2-->
    <maxTaskNum value="2"/>

    <!--设置下载失败，重试次数，默认为10-->
    <reTryNum value="10"/>

    <!--设置重试间隔，单位为毫秒，默认2000毫秒-->
    <reTryInterval value="2000"/>

    <!--设置url连接超时时间，单位为毫秒，默认5000毫秒-->
    <connectTimeOut value="5000"/>

    <!--设置IO流读取时间，单位为毫秒，默认20000毫秒，该时间不能少于10000毫秒-->
    <iOTimeOut value="20000"/>

    <!--设置写文件buff大小，该数值大小不能小于2048，数值变小，下载速度会变慢-->
    <buffSize value="8192"/>

    <!--设置https ca 证书信息；path 为assets目录下的CA证书完整路径，name 为CA证书名-->
    <ca name="" path=""/>

    <!--是否需要转换速度单位，转换完成后为：1b/s、1k/s、1m/s、1g/s、1t/s，如果不需要将返回byte长度-->
    <cnvertSpeed value="false"/>

  </download>

  <upload>
    <!--是否打开上传广播，默认为false-->
    <openBroadcast value="false"/>

    <!--设置上传队列最大任务数， 默认为2-->
    <maxTaskNum value="2"/>

    <!--设置上传失败，重试次数，默认为10-->
    <reTryNum value="10"/>

    <!--设置重试间隔，单位为毫秒-->
    <reTryInterval value="2000"/>

    <!--设置url连接超时时间，单位为毫秒，默认5000毫秒-->
    <connectTimeOut value="5000"/>
  </upload>

</aria>
```

#### 代码中设置参数
除了文件方式外修改Aria参数外，同样的，你也可以在代码中动态修改Aria参数</br>
通过`Aria.get(this).getDownloadConfig()`或`Aria.get(this).getUploadConfig()`直接获取配置文件，然后修改参数</br>
如以下所示：
```java
// 修改最大下载数，调用完成后，立即生效
// 如当前下载任务数是4，修改完成后，当前任务数会被Aria自动调度任务数
Aria.get(this).getDownloadConfig().setMaxTaskNum(3);
```

### 常用接口
* 停止所有任务

```java
Aria.download(this).stopAllTask();
```
* 删除所有任务

```java
Aria.download(this).removeAllTask();
```
* 获取当前任务的下载速度
速度参数有点特殊，需要[下载事件支持](#下载状态获取)
``` java
@Override public void onTaskRunning(DownloadTask task) {
  //如果你打开了速度单位转换配置，将可以通过以下方法获取带单位的下载速度，如：1 m/s
  String convertSpeed = task.getConvertSpeed();
  //如果你有自己的单位格式，可以通过以下方法获取原始byte长度
  long speed = task.getSpeed();
}
```
* 获取下载的文件大小
同样的，你也可以在DownloadTask对象中获取下载的文件大小
```
@Override public void onTaskRunning(DownloadTask task) {
  //获取文件大小
  long fileSize = task.getFileSize();
}
```

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
 Aria.upload(this).load(filePath).cancel();
 ```

## 其他
 有任何问题，可以在[issues](https://github.com/AriaLyy/Aria/issues)给我留言反馈。

***

## 后续版本开发规划
* ~~实现上传队列调度功能~~

## 开发日志
  + v_3.1.0 添加Aria配置文件，优化代码
  + v_3.0.3 修复暂停后删除任务，闪退问题，添加删除记录的api
  + v_3.0.2 支持30x重定向链接下载
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
