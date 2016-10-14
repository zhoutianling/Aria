package com.arialyy.simple.base;

import android.app.Application;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.frame.core.AbsFrame;

/**
 * Created by Lyy on 2016/9/27.
 */
public class BaseApplication extends Application {
  @Override public void onCreate() {
    super.onCreate();
    AbsFrame.init(this);
    DownloadManager.init(this);
  }
}
