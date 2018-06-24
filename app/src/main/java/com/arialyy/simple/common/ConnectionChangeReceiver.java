package com.arialyy.simple.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.arialyy.aria.core.Aria;

public class ConnectionChangeReceiver extends BroadcastReceiver {
  private static final String TAG = ConnectionChangeReceiver.class.getSimpleName();

  @Override public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "网络状态改变");
    /**
     * 获得网络连接服务
     */
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo.State state =
        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

    if (NetworkInfo.State.CONNECTED == state) {
      Aria.download(this).resumeAllTask();
      return;
    }

    Aria.download(this).stopAllTask();

    //state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
    //if (NetworkInfo.State.CONNECTED == state) {
    //
    //}
  }
}