# Aria
![图标](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/app/src/main/res/mipmap-hdpi/ic_launcher.png)</br>
Aria，致力于让下载傻瓜化</br>
+ Aria有以下特点：
 - 简单
 - 可自定义是否使用广播
 - 支持多线程、多任务下载
 - 支持任务自动切换
 - 支持下载速度直接获取

[Aria怎样使用？](#使用)

如果你觉得Aria对你有帮助，您的star和issues将是对我最大支持.`^_^`

## 下载
[![Download](https://api.bintray.com/packages/arialyy/maven/Aria/images/download.svg)](https://bintray.com/arialyy/maven/Aria/_latestVersion)</br>
compile 'com.arialyy.aria:Aria:2.3.1'


## 示例
![多任务下载](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/img/download_img.gif)
![单任务下载](https://github.com/AriaLyy/DownloadUtil/blob/master/img/11.gif "")

## 性能展示
![性能展示](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/img/performance.png)

***
## 使用
### 一、Aria 是实体驱动型的工具，所以，第一步，你需要创建一个下载实体
```java
  DownloadEntity mEntity = new DownloadEntity();
  mEntity.setFileName(fileName);          //设置文件名
  mEntity.setDownloadUrl(downloadUrl);    //设置下载链接
  mEntity.setDownloadPath(downloadPath);  //设置存放路径
```
### 二、为了能接收到Aria传递的数据，你需要把你的Activity或fragment注册到Aria管理器中，注册的方式很简单，在onResume
```java
@Override protected void onResume() {
    super.onResume();
    Aria.whit(this).addSchedulerListener(new MySchedulerListener());
  }
```
### 三、还记得上面的DownloadEntity吗？现在是时候使用它进行下载了
- 启动下载 
 
  ```java
  Aria.whit(this).load(mEntity).start();
  ```
- 暂停下载

  ```java
  Aria.whit(this).load(mEntity).stop();
  ```
- 恢复下载

  ```java
  Aria.whit(this).load(mEntity).resume();
  ```
- 取消下载

  ```java
  Aria.whit(this).load(mEntity).cancel();
  ```

### 四、关于Aria，你还需要知道的一些东西
- 设置下载任务数，Aria默认下载任务为**2**

  ```java
  Aria.get(getContext()).setMaxDownloadNum(num);
  ```
- 停止所有下载

  ```java
  Aria.get(this).stopAllTask();
  ```
- 设置失败重试次数，从事次数不能少于 1

  ```java
  Aria.get(this).setReTryNum(10);
  ```
- 设置失败重试间隔，重试间隔不能小于 5000ms

  ```java
  Aria.get(this).setReTryInterval(5000);
  ```
- 设置是否打开广播，如果你需要在Service后台获取下载完成情况，那么你需要打开Aria广播发送按钮，[Aria广播使用](#广播使用)

  ```java
  Aria.get(this).openBroadcast(true);
  ```

***
### 广播使用
#### 一、创建广播接收器，用来接收下载的各种状态
```java
private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    long len = 0;
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      //可以通过intent获取到下载实体，下载实体中包含了各种下载状态
      DownloadEntity entity = intent.getParcelableExtra(DownloadManager.ENTITY);
      switch (action) {
        case DownloadManager.ACTION_PRE:  //预处理
          break;
        case DownloadManager.ACTION_POST_PRE: //预处理完成
          //预处理完成，便可以获取文件的下载长度了
          len = entity.getFileSize();
          break;
        case DownloadManager.ACTION_START:  //开始下载
          L.d(TAG, "download start");
          break;
        case DownloadManager.ACTION_RESUME: //恢复下载
          L.d(TAG, "download resume");
          long location = intent.getLongExtra(DownloadManager.CURRENT_LOCATION, 1);
          break;
        case DownloadManager.ACTION_RUNNING:  //下载中
          long current = intent.getLongExtra(DownloadManager.CURRENT_LOCATION, 0);
          break;
        case DownloadManager.ACTION_STOP:   //停止下载
          L.d(TAG, "download stop");
          break;
        case DownloadManager.ACTION_COMPLETE: //下载完成
          break;
        case DownloadManager.ACTION_CANCEL:   //取消下载
          break;
        case DownloadManager.ACTION_FAIL:     // 下载失败
          break;
      }
    }
  };
```

#### 二、在Activity中创建广播过滤器
```java
@Override protected void onResume() {
  super.onResume();
  IntentFilter filter = new IntentFilter();
  filter.addDataScheme(getPackageName());
  filter.addAction(DownloadManager.ACTION_PRE);
  filter.addAction(DownloadManager.ACTION_POST_PRE);
  filter.addAction(DownloadManager.ACTION_RESUME);
  filter.addAction(DownloadManager.ACTION_START);
  filter.addAction(DownloadManager.ACTION_RUNNING);
  filter.addAction(DownloadManager.ACTION_STOP);
  filter.addAction(DownloadManager.ACTION_CANCEL);
  filter.addAction(DownloadManager.ACTION_COMPLETE);
  filter.addAction(DownloadManager.ACTION_FAIL);
  registerReceiver(mReceiver, filter);
}
```
***
## 开发日志
 + v_2.1.0 修复大量bug
 + v_2.1.1 增加，选择最大下载任务数接口
 + v_2.3.1 重命名为Aria，下载流程简化

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
