package com.arialyy.simple.test;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.show.L;
import java.io.File;
import java.net.Proxy;

/**
 * Created by laoyuyu on 2018/4/13.
 */

public class AnyRunnModule {
  String TAG = "AnyRunnModule";
  private Context mContext;
  private String mUrl;

  public AnyRunnModule(Context context) {
    Aria.download(this).register();
    mContext = context;
  }

  @Download.onWait void onWait(DownloadTask task) {
    Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
  }

  @Download.onPre protected void onPre(DownloadTask task) {
    Log.d(TAG, "onPre");
  }

  @Download.onTaskStart void taskStart(DownloadTask task) {
    Log.d(TAG, "onStart");
  }

  @Download.onTaskRunning protected void running(DownloadTask task) {
    Log.d(TAG, "running；Percent = " + task.getPercent());
  }

  @Download.onTaskResume void taskResume(DownloadTask task) {
    Log.d(TAG, "resume");
  }

  @Download.onTaskStop void taskStop(DownloadTask task) {
    Log.d(TAG, "stop");
  }

  @Download.onTaskCancel void taskCancel(DownloadTask task) {
    Log.d(TAG, "cancel");
  }

  @Download.onTaskFail void taskFail(DownloadTask task) {
    Log.d(TAG, "fail");
  }

  @Download.onTaskComplete void taskComplete(DownloadTask task) {
    L.d(TAG, "path ==> " + task.getDownloadEntity().getDownloadPath());
    L.d(TAG, "md5Code ==> " + CommonUtil.getFileMD5(new File(task.getDownloadPath())));
  }

  void start(String url) {
    mUrl = url;
    String path = Environment.getExternalStorageDirectory().getPath() + "/aaa.apk";
    //File file = new File(path);
    //if (file.exists()) {
    //  file.delete();
    //}
    Aria.download(this)
        .load(url)
        //.addHeader("Accept-Encoding", "gzip")
        .addHeader("Referer", "http://www.bilibili.com/")
        .addHeader("user-agent", "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
        .setRequestMode(RequestEnum.GET)
        //.setUrlProxy(Proxy.NO_PROXY)
        .setFilePath(path)
        .resetState()
        .start();
    //String[] urls = new String[] {
    //    "http://cdn-s1.touchfound.net/1526449199142_3617.png",
    //    "http://cdn-s1.touchfound.net/mtVmResources/1526287158571.zip",
    //    "http://cdn-s1.touchfound.net/1526450206960_2186.png",
    //    "http://cdn-s1.touchfound.net/1526449199025_1476.png"
    //};
    //for (int i = 0, len = urls.length; i < len; i++){
    //
    //  Aria.download(this)
    //      .load(urls[i])
    //      //.addHeader("Accept-Encoding", "gzip")
    //      .setRequestMode(RequestEnum.GET)
    //      .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/ggsg123456.apk" + i)
    //      //.resetState()
    //      .start();
    //}
  }

  void startFtp(String url) {
    mUrl = url;
    Aria.download(this)
        .loadFtp(url)
        .login("lao", "123456")
        //.addHeader("Accept-Encoding", "gzip")
        .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/")
        //.resetState()
        .start();
    //String[] urls = new String[] {
    //    "http://cdn-s1.touchfound.net/1526449199142_3617.png",
    //    "http://cdn-s1.touchfound.net/mtVmResources/1526287158571.zip",
    //    "http://cdn-s1.touchfound.net/1526450206960_2186.png",
    //    "http://cdn-s1.touchfound.net/1526449199025_1476.png"
    //};
    //for (int i = 0, len = urls.length; i < len; i++){
    //
    //  Aria.download(this)
    //      .load(urls[i])
    //      //.addHeader("Accept-Encoding", "gzip")
    //      .setRequestMode(RequestEnum.GET)
    //      .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/ggsg123456.apk" + i)
    //      //.resetState()
    //      .start();
    //}
  }

  void stop(String url) {
    Aria.download(this).load(url).stop();
  }

  void cancel(String url) {
    Aria.download(this).load(url).cancel();
  }

  void unRegister() {
    Aria.download(this).unRegister();
  }
}
