package com.arialyy.simple.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import butterknife.Bind;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseDialog;
import com.arialyy.simple.databinding.DialogDownloadNumBinding;

/**
 * Created by “AriaLyy@outlook.com” on 2016/11/14.
 * 设置下载数量对话框
 */
public class DownloadNumDialog extends BaseDialog<DialogDownloadNumBinding> implements RadioGroup.OnCheckedChangeListener{
  public static final int RESULT_CODE = 1001;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.rg) RadioGroup mRg;

  @Override protected int setLayoutId() {
    return R.layout.dialog_download_num;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mCancel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dismiss();
      }
    });
    for (int i = 0, count = mRg.getChildCount(); i < count; i++) {
      RadioButton rb = (RadioButton) mRg.getChildAt(i);
      rb.setId(i);
    }
    mRg.setOnCheckedChangeListener(this);
  }

  @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
    RadioButton rb = (RadioButton) group.getChildAt(checkedId);
    if (rb.isChecked()) {
      getSimplerModule().onDialog(RESULT_CODE, rb.getTag());
    }
  }
}
