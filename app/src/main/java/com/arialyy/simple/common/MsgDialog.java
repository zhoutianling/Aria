package com.arialyy.simple.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import butterknife.OnClick;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseDialog;
import com.arialyy.simple.databinding.DialogMsgBinding;

/**
 * Created by AriaL on 2017/6/3.
 */
@SuppressLint("ValidFragment") public class MsgDialog extends BaseDialog<DialogMsgBinding> {

  private String mTitle, mMsg;

  public MsgDialog(Object obj, String title, String msg) {
    super(obj);
    mTitle = title;
    mMsg = msg;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    getBinding().setTitle(mTitle);
    getBinding().setMsg(mMsg);
  }

  @Override protected int setLayoutId() {
    return R.layout.dialog_msg;
  }

  @OnClick(R.id.enter)
  public void close(){
    dismiss();
  }
}
