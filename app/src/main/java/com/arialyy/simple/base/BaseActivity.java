package com.arialyy.simple.base;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import com.arialyy.frame.core.AbsActivity;

/**
 * Created by Lyy on 2016/9/27.
 */
public abstract class BaseActivity<VB extends ViewDataBinding> extends AbsActivity<VB> {
  @Override protected void dataCallback(int result, Object data) {

  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
  }
}
