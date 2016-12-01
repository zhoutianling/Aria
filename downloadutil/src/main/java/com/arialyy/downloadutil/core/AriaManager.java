package com.arialyy.downloadutil.core;

import android.content.Context;
import com.arialyy.downloadutil.core.scheduler.OnSchedulerListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyy on 2016/12/1.
 */
public class AriaManager {
  private Map<String, AMEntity> mAria = new HashMap<>();

  private static final Object LOCK = new Object();
  private static volatile AriaManager INSTANCE = null;

  private AriaManager(){
  }

  public static AriaManager getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new AriaManager();
      }
    }
    return INSTANCE;
  }

  public synchronized void get(Context context){

  }




}
