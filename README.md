# Aria
![图标](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/app/src/main/res/mipmap-hdpi/ic_launcher.png)</br>
## [ENGLISH DOC](https://github.com/AriaLyy/Aria/blob/master/ENGLISH_README.md)</br>
Aria项目源于工作中遇到的一个文件下载管理的需求，当时被下载折磨的痛不欲生，从那时起便萌生了编写一个简单易用，稳当高效的下载框架，aria经历了1.0到3.0的开发，算是越来越接近当初所制定的目标了。

Aria有以下特点：
 + 简单、方便
   - 可以在Activity、Service、Fragment、Dialog、popupWindow、Notification等组件中使用
   - 支持任务自动调度，使用者不需要关心任务状态切换的逻辑
   - [通过Aria的事件，能很容易获取当前下载任务的下载状态](#下载状态获取)
   - [一句代码加可以获取当前的下载速度](#常用接口)
   - [一句代码就可以动态设置最大下载数](#代码中设置参数)
   - [一句代码实现速度限制](#常用接口)
   - [通过修改配置文件很容易就能修改下载线程数](#配置文件设置参数)
   - [优先下载某一个任务](#常用接口)
   - [支持任务组下载，多个任务可共享一个progress](#一组任务下载)
 + 支持https地址下载
   - 在配置文件中很容易就可以设置CA证书的信息
 + 支持300、301、302重定向下载链接下载
 + 支持上传操作


Aria怎样使用？
* [下载](#使用)
* [上传](#上传)

如果你觉得Aria对你有帮助，您的star和issues将是对我最大支持.`^_^`

## 下载
[![Download](https://api.bintray.com/packages/arialyy/maven/AriaApi/images/download.svg)](https://bintray.com/arialyy/maven/AriaApi/_latestVersion)
[![Download](https://api.bintray.com/packages/arialyy/maven/AriaCompiler/images/download.svg)](https://bintray.com/arialyy/maven/AriaCompiler/_latestVersion)
```java
compile 'com.arialyy.aria:aria-core:3.2.7'
annotationProcessor 'com.arialyy.aria:aria-compiler:3.2.7'
```

## 示例
![多任务下载](https://github.com/AriaLyy/DownloadUtil/blob/master/img/download_img.gif)
![网速下载限制](https://github.com/AriaLyy/DownloadUtil/blob/master/img/max_speed.gif)
![下载任务组](https://github.com/AriaLyy/DownloadUtil/blob/master/img/download_group.gif)

## 性能
![性能展示](https://github.com/AriaLyy/DownloadUtil/blob/master/img/performance.png)

***
## 使用
由于Aria涉及到文件和网络的操作，因此需要你在manifest文件中添加以下权限，如果你希望在6.0以上的系统中使用Aria，那么你需要动态向安卓系统申请文件系统读写权限，[如何使用安卓系统权限](https://developer.android.com/training/permissions/index.html?hl=zh-cn)
```xml
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## 使用Aria进行下载
### 普通任务下载
* 下载\恢复下载

  ```java
  Aria.download(this)
      .load(DOWNLOAD_URL)     //读取下载地址
      .setDownloadPath(DOWNLOAD_PATH)    //设置文件保存的完整路径
      .start();   //启动下载
  ```
* 暂停

  ```java
  Aria.download(this).load(DOWNLOAD_URL).pause();
  ```

* 取消下载

  ```java
  Aria.download(this).load(DOWNLOAD_URL).cancel();
  ```
### 一组任务下载
任务组的下载和普通任务的下载基本上差不多，区别在于，任务组下载不需要对每一个子任务设置保存路径，**但是需要设置任务组保存文件夹路径，所有子任务都保存在该文件夹下**

* 下载\恢复下载

  ```java
  Aria.download(this)
      .load(urls)     //设置一主任务，参数为List<String>
      .setDownloadDirPath(groupDirPath)    //设置任务组的文件夹路径
      .start();   //启动下载
  ```
* 暂停

  ```java
  Aria.download(this).load(urls).pause();
  ```

* 取消下载

  ```java
  Aria.download(this).load(urls).cancel();
  ```

### 下载状态获取
如果你希望读取下载进度或下载信息，那么你需要创建事件类，并在onResume(Activity、Fragment)或构造函数(Dialog、PopupWindow)，将该事件类注册到Aria管理器。

1. 将对象注册到Aria

 `Aria.download(this).register();`或`Aria.upload(this).register();`
 ```java
 @Override
 protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Aria.download(this).register();
  }
 ```

2. 使用`@Download`或`@Upload`或`@DownloadGroup`注解你的函数

 **注意：**
 - 注解回掉采用Apt的方式实现，所以，你不需要担心这会影响你机器的性能
 - 被注解的方法**不能被private修饰**
 - 被注解的方法**只能有一个参数，并且参数类型必须是`DownloadTask`或`UploadTask`或`DownloadGroupTask`**
 - 方法名可以为任意字符串

3. 除了在widget（Activity、Fragment、Dialog、Popupwindow）中使用注解方法外，你还可以在Service、Notification等组件中使用注解函数。

  ```java
  @Download.onPre(DOWNLOAD_URL)
  protected void onPre(DownloadTask task) {}

  @Download.onTaskStart
  void taskStart(DownloadTask task) {}

  @Download.onTaskRunning
  protected void running(DownloadTask task) {}

  @Download.onTaskResume
  void taskResume(DownloadTask task) {}

  @Download.onTaskStop
  void taskStop(DownloadTask task) {}

  @Download.onTaskCancel
  void taskCancel(DownloadTask task) {}

  @Download.onTaskFail
  void taskFail(DownloadTask task) {}

  @Download.onTaskComplete
  void taskComplete(DownloadTask task) {}

  @Download.onNoSupportBreakPoint
  public void onNoSupportBreakPoint(DownloadTask task) {}

  ```
4. 如果你希望对单个任务，或某一些特定任务设置监听器。<br>
 **在注解中添加任务的下载地址，则表示只有该任务才会触发被注解的方法**。

 ```java
 @Download.onTaskRunning({
      "https://test.xx.apk",
      "http://test.xx2.apk"
  }) void taskRunning(DownloadTask task) {
    mAdapter.setProgress(task.getDownloadEntity());
  }
 ```
在上面的例子中，只有下载地址是`https://test.xx.apk`和`http://test.xx2.apk`才会触发
`taskRunning(DownloadTask task)`方法。

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

    <!--是否打开下载广播，默认为false，不建议使用广播，你可以使用Download注解来实现事件回调-->
    <openBroadcast value="false"/>

    <!--设置下载队列最大任务数， 默认为2-->
    <maxTaskNum value="2"/>

    <!--设置下载失败，重试次数，默认为10-->
    <reTryNum value="10"/>

    <!--设置重试间隔，单位为毫秒，默认2000毫秒-->
    <reTryInterval value="5000"/>

    <!--设置url连接超时时间，单位为毫秒，默认5000毫秒-->
    <connectTimeOut value="5000"/>

    <!--设置IO流读取时间，单位为毫秒，默认20000毫秒，该时间不能少于10000毫秒-->
    <iOTimeOut value="20000"/>

    <!--设置写文件buff大小，该数值大小不能小于2048，数值变小，下载速度会变慢-->
    <buffSize value="8192"/>

    <!--设置https ca 证书信息；path 为assets目录下的CA证书完整路径，name 为CA证书名-->
    <ca name="" path=""/>

    <!--是否需要转换速度单位，转换完成后为：1b/s、1kb/s、1mb/s、1gb/s、1tb/s，如果不需要将返回byte长度-->
    <convertSpeed value="true"/>

    <!--设置最大下载速度，单位：kb, 为0表示不限速-->
    <maxSpeed value="0"/>

  </download>

  <upload>
    <!--是否打开上传广播，默认为false，不建议使用广播，你可以使用Upload注解来实现事件回调-->
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

* 恢复所有停止的任务

 ```java
Aria.download(this).resumeAllTask();
 ```

* 删除所有任务

 ```java
 Aria.download(this).removeAllTask();
 ```

* 最大下载速度限制
 ```java
 //单位为 kb
 Aria.download(this).setMaxSpeed(speed);
 ```

* 获取当前任务的下载速度<br>
速度参数有点特殊，需要[下载事件支持](#下载状态获取)
``` java
@Override public void onTaskRunning(DownloadTask task) {
  //如果你打开了速度单位转换配置，将可以通过以下方法获取带单位的下载速度，如：1 mb/s
  String convertSpeed = task.getConvertSpeed();
  //如果你有自己的单位格式，可以通过以下方法获取原始byte长度
  long speed = task.getSpeed();
}
```

* 获取下载的文件大小、当前进度百分比</br>
同样的，你也可以在DownloadTask对象中获取下载的文件大小
```
@Override public void onTaskRunning(DownloadTask task) {
  //获取文件大小
  long fileSize = task.getFileSize();
  //获取单位转换后的文件大小
  String fileSize1 = task.getConvertFileSize();
  //当前进度百分比
  int percent = task.getPercent();
}
```

* 设置高优先级任务<br>
 如果你希望优先下载某一个任务，你可以
``` java
Aria.download(this).load(DOWNLOAD_URL).setDownloadPath(PATH).setHighestPriority();
```

* 设置扩展字段<br>
 有的时候，你可能希望在下载的时候存放一些自己的数据</br>
**TIP**: 如果你数据比较多，或者数据比较复杂，你可以先把数据转换为**JSON**，然后再将其存到Aria的下载实体中
```java
Aria.download(this).load(DOWNLOAD_URL).setExtendField(str)
```

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

## 混淆配置
```
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
}

```

## 其他
 有任何问题，可以在[issues](https://github.com/AriaLyy/Aria/issues)给我留言反馈。</br>
 ![交流](https://github.com/AriaLyy/Aria/blob/master/img/aria_qq.png)

***

## 后续版本开发规划
* ~~ftp断点下载~~
* ~~http、scoket断点上传~~
* ~~实现上传队列调度功能~~


## 开发日志
  + v_3.2.7 移除设置文件名的api接口，修复断开网络时出现的进度条错误的问题
  + v_3.2.6 移除广播事件，增加任务组下载功能
  + v_3.1.9 修复stopAll队列没有任务时崩溃的问题，增加针对单个任务监听的功能
  + v_3.1.7 修复某些文件下载不了的bug，增加apt注解方法，事件获取更加简单了
  + v_3.1.6 取消任务时onTaskCancel回调两次的bug
  + v_3.1.5 优化代码结构，增加优先下载任务功能。
  + v_3.1.4 修复快速切换，暂停、恢复功能时，概率性出现的重新下载问题，添加onPre()回调，onPre()用于请求地址之前执行界面UI更新操作。
  + v_3.1.0 添加Aria配置文件，优化代码
  + v_3.0.3 修复暂停后删除任务，闪退问题，添加删除记录的api
  + v_3.0.2 支持30x重定向链接下载
  + v_3.0.0 添加上传任务支持，修复一些已发现的bug

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
