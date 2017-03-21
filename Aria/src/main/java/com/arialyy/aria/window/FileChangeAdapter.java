package com.arialyy.aria.window;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.arialyy.aria.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/3/21.
 */
final class FileChangeAdapter extends BaseAdapter {

  List<FileEntity> mData = new ArrayList<>();
  SparseBooleanArray mCheck = new SparseBooleanArray();
  Context mContext;

  public FileChangeAdapter(Context context, List<FileEntity> list) {
    mContext = context;
    mData.addAll(list);
    for (int i = 0, len = mData.size(); i < len; i++) {
      mCheck.append(i, false);
    }
  }

  @Override public int getCount() {
    return mData.size();
  }

  @Override public Object getItem(int position) {
    return null;
  }

  @Override public long getItemId(int position) {
    return 0;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    FileChangeHolder holder = null;
    if (convertView == null) {
      convertView = LayoutInflater.from(mContext).inflate(R.layout.item_file, null);
      holder = new FileChangeHolder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (FileChangeHolder) convertView.getTag();
    }

    holder.checkBox.setChecked(mCheck.get(position, false));
    return convertView;
  }

  public void setCheck(int position, boolean check) {
    if (position >= mData.size()) return;
    mCheck.put(position, check);
    notifyDataSetChanged();
  }

  private static class FileChangeHolder {
    TextView title, info;
    ImageView icon;
    CheckBox checkBox;

    FileChangeHolder(View view) {
      title = (TextView) view.findViewById(R.id.title);
      info = (TextView) view.findViewById(R.id.info);
      icon = (ImageView) view.findViewById(R.id.icon);
      checkBox = (CheckBox) view.findViewById(R.id.checkbox);
    }
  }
}
