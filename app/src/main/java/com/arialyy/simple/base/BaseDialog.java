package com.arialyy.simple.base;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import com.arialyy.frame.core.AbsDialogFragment;

/**
 * Created by “AriaLyy@outlook.com” on 2016/11/14.
 */
public abstract class BaseDialog<VB extends ViewDataBinding> extends AbsDialogFragment<VB>{

  protected BaseDialog(Object obj){
    super(obj);
  }

  @Override protected void init(Bundle savedInstanceState) {

  }

  @Override protected void dataCallback(int result, Object data) {

  }
}
