package com.arialyy.downloadutil.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyy on 2016/12/1.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public class AriaManager {
  private static final    Object                LOCK     = new Object();
  private static volatile AriaManager           INSTANCE = null;
  private                 Map<String, AMTarget> mTargets = new HashMap<>();
  private DownloadManager mManager;
  private LifeCallback    mLifeCallback;

  private AriaManager(Context context) {
    regAppLifeCallback(context);
    mManager = DownloadManager.init(context);
  }

  public static AriaManager getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new AriaManager(context);
      }
    }
    return INSTANCE;
  }

  public AMTarget get(Context context){
    return getTarget(context);
  }

  private void putTarget(Context context) {
    String   clsName = context.getClass().getName();
    AMTarget target  = mTargets.get(clsName);
    if (target == null) {
      target = new AMTarget();
      mTargets.put(clsName, target);
    }
  }

  private AMTarget getTarget(Context context) {
    return mTargets.get(context.getClass().getName());
  }

  /**
   * 注册APP生命周期回调
   */
  private void regAppLifeCallback(Context context) {
    Context app = context.getApplicationContext();
    if (app instanceof Application) {
      mLifeCallback = new LifeCallback();
      ((Application) app).registerActivityLifecycleCallbacks(mLifeCallback);
    }
  }

  /**
   * Activity生命周期
   */
  private class LifeCallback implements Application.ActivityLifecycleCallbacks {

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override public void onActivityStarted(Activity activity) {

    }

    @Override public void onActivityResumed(Activity activity) {

    }

    @Override public void onActivityPaused(Activity activity) {

    }

    @Override public void onActivityStopped(Activity activity) {

    }

    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override public void onActivityDestroyed(Activity activity) {

    }
  }
}
