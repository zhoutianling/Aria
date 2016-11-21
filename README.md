# DownloadUtil
![图标](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/app/src/main/res/mipmap-hdpi/ic_launcher.png)</br>
这是一个 android 智能切换多任务断点续传工具，使用该工具，你可以很容易实现`多线程下载功能和复杂的任务自动切换功能`</br>
+ 该工具具有以下特点：
 - 通过命令控制下载
 - 可在广播中接收任务的各种下载状态
 - 支持任务自动切换
 - 支持下载速度直接获取

如果你觉得我的代码对你有帮助，您的star和issues将是对我最大支持.`^_^`

#下载
[![Download](https://api.bintray.com/packages/arialyy/maven/MTDownloadUtil/images/download.svg)](https://bintray.com/arialyy/maven/MTDownloadUtil/_latestVersion)<br/>
compile 'com.arialyy.downloadutil:DownloadUtil:2.1.1'


#示例
![多任务下载](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/img/download_img.gif)

# 性能展示
![性能展示](https://github.com/AriaLyy/DownloadUtil/blob/v_2.0/img/performance.png)

# 使用
* 一、在Application注册下载器
```java
public class BaseApplication extends Application {
  @Override public void onCreate() {
    super.onCreate();
    //注册下载器
    DownloadManager.init(this);
  }
}
```

* 二、创建广播接收器，用来接收下载的各种状态
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

* 三、在Activity中创建广播过滤器
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

* 四、创建下载实体
```java
  DownloadEntity entity = new DownloadEntity();
  entity.setFileName(fileName);          //设置文件名
  entity.setDownloadUrl(downloadUrl);    //设置下载链接
  entity.setDownloadPath(downloadPath);  //设置存放路径
```

* 五、通过命令控制下载(下载状态控制，或下载任务切换将自动完成)</br>
**！！注意：命令需要第四步的下载实体支持**

 - 获取命令工厂实例和下载管理器实例
 ```java
 CmdFactory factory = CmdFactory.getInstance();
 DownloadManager manager = DownloadManager.getInstance();
 ```
 - 开始命令、恢复下载命令都是同一个
 ```java
 private void start() {
   List<IDownloadCmd> commands = new ArrayList<>();
   IDownloadCmd       addCMD   = factory.createCmd(this, entity, CmdFactory.TASK_CREATE);
   IDownloadCmd       startCmd = factory.createCmd(this, entity, CmdFactory.TASK_START);
   commands.add(addCMD);
   commands.add(startCmd);
   manager.setCmds(commands).exe();
 }
 ```
 - 停止命令
 ```java
 private void stop() {
   IDownloadCmd stopCmd = factory.createCmd(this, entity, CmdFactory.TASK_STOP);
   manager.setCmd(stopCmd).exe();
 }
 ```
 - 取消命令（取消下载、删除下载任务）
 ```java
 private void cancel() {
    IDownloadCmd cancelCmd = factory.createCmd(this, entity, CmdFactory.TASK_CANCEL);
    manager.setCmd(cancelCmd).exe();
 }
 ```

# 修改最大任务数
```
mManager.getTaskQueue().setDownloadNum(num);

```

# 开发日志
 + v_2.1.0 修复大量bug
 + v_2.1.1 增加，选择最大下载任务数接口

License
-------

    Copyright 2016 AriaLyy(DownloadUtil)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
