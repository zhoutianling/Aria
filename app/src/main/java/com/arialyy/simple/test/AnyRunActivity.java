package com.arialyy.simple.test;

import android.os.Bundle;
import android.view.View;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityTestBinding;
import java.util.List;

/**
 * Created by laoyuyu on 2018/4/13.
 */

public class AnyRunActivity extends BaseActivity<ActivityTestBinding> {
  AnyRunnModule module;
  String[] urls;
  int index = 0;
  String URL = "http://static.gaoshouyou.com/d/12/0d/7f120f50c80d2e7b8c4ba24ece4f9cdd.apk";

  @Override protected int setLayoutId() {
    return R.layout.activity_test;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.init(this);
    mBar.setVisibility(View.GONE);
    module = new AnyRunnModule(this);
    urls = getResources().getStringArray(R.array.group_urls);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        //module.start();
        //if (index < urls.length) {
        //  module.start(urls[index]);
        //  index++;
        //}
        module.start(URL);
        List<AbsEntity> list = Aria.download(this).getTotalTaskList();
        ALog.d(TAG, "size ==> " + list.size());
        break;
      case R.id.stop:
        //List<AbsEntity> list = Aria.download(this).getTotalTaskList();
        //
        ////module.stop();
        //module.stop(URL);

        break;
      case R.id.cancel:
        module.cancel(URL);
        //module.cancel();
        break;
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    module.unRegister();
  }
}
