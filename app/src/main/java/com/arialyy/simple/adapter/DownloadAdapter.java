/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.arialyy.simple.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.arialyy.absadapter.common.AbsHolder;
import com.arialyy.absadapter.recycler_view.AbsRVAdapter;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.command.CmdFactory;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import com.arialyy.downloadutil.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lyy on 2016/9/27.
 * 下载列表适配器
 */
public class DownloadAdapter extends AbsRVAdapter<DownloadEntity, DownloadAdapter.MyHolder> {
  private static final String TAG = "DownloadAdapter";
  private DownloadManager mManager;
  private CmdFactory      mFactory;
  private Map<String, Integer> mPositions = new HashMap<>();

  public DownloadAdapter(Context context, List<DownloadEntity> data) {
    super(context, data);
    mFactory = CmdFactory.getInstance();
    mManager = DownloadManager.getInstance();
    List<IDownloadCmd> addCmd = new ArrayList<>();
    int                i      = 0;
    for (DownloadEntity entity : data) {
      mPositions.put(entity.getDownloadUrl(), i);
      addCmd.add(mFactory.createCmd(entity, CmdFactory.TASK_CREATE));
      i++;
    }
    mManager.setCmds(addCmd).exe();
  }

  @Override protected MyHolder getViewHolder(View convertView, int viewType) {
    return new MyHolder(convertView);
  }

  @Override protected int setLayoutId(int type) {
    return R.layout.item_download;
  }

  public synchronized void updateState(DownloadEntity entity) {
    if (entity.getState() == DownloadEntity.STATE_CANCEL) {
      mPositions.clear();
      int i = 0;
      for (DownloadEntity entity_1 : mData) {
        mPositions.put(entity_1.getDownloadUrl(), i);
        i++;
      }
      notifyDataSetChanged();
    } else {
      int position = indexItem(entity.getDownloadUrl());
      mData.set(position, entity);
      notifyItemChanged(position);
    }
  }

  public synchronized void setProgress(DownloadEntity entity) {
    String url      = entity.getDownloadUrl();
    int    position = indexItem(url);
    mData.set(position, entity);
    notifyItemChanged(position);
  }

  private synchronized int indexItem(String url) {
    Set set = mPositions.entrySet();
    for (Object aSet : set) {
      Map.Entry entry = (Map.Entry) aSet;
      if (entry.getKey().equals(url)) {
        return (int) entry.getValue();
      }
    }
    return -1;
  }

  @Override protected void bindData(MyHolder holder, int position, final DownloadEntity item) {
    long size     = item.getFileSize();
    int  current  = 0;
    long progress = item.getCurrentProgress();
    long speed    = item.getSpeed();
    current = size == 0 ? 0 : (int) (progress * 100 / size);
    holder.progress.setProgress(current);
    BtClickListener listener = new BtClickListener(position, item);
    holder.bt.setOnClickListener(listener);
    String str   = "";
    int    color = android.R.color.holo_green_light;
    switch (item.getState()) {
      case DownloadEntity.STATE_WAIT:
      case DownloadEntity.STATE_OTHER:
      case DownloadEntity.STATE_FAIL:
        str = "开始";
        break;
      case DownloadEntity.STATE_STOP:
        str = "恢复";
        color = android.R.color.holo_blue_light;
        break;
      case DownloadEntity.STATE_PRE:
      case DownloadEntity.STATE_POST_PRE:
      case DownloadEntity.STATE_DOWNLOAD_ING:
        str = "暂停";
        color = android.R.color.holo_red_light;
        break;
      case DownloadEntity.STATE_COMPLETE:
        str = "重新开始？";
        holder.progress.setProgress(100);
        break;
    }
    holder.bt.setText(str);
    holder.bt.setTextColor(getColor(color));
    holder.speed.setText(CommonUtil.formatFileSize(speed) + "/s");
    holder.fileSize.setText(covertCurrentSize(progress) + "/" + CommonUtil.formatFileSize(size));
    holder.cancel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mData.remove(item);
        notifyDataSetChanged();
        IDownloadCmd cancelCmd = mFactory.createCmd(item, CmdFactory.TASK_CANCEL);
        mManager.setCmd(cancelCmd).exe();
      }
    });
  }

  public void setDownloadNum(int num) {
    mManager.getTaskQueue().setDownloadNum(num);
  }

  private String covertCurrentSize(long currentSize) {
    String size = CommonUtil.formatFileSize(currentSize);
    return size.substring(0, size.length() - 1);
  }

  private int getColor(int color) {
    return Resources.getSystem().getColor(color);
  }

  private class BtClickListener implements View.OnClickListener {
    private DownloadEntity entity;
    private int            position;

    BtClickListener(int position, DownloadEntity entity) {
      this.entity = entity;
      this.position = position;
    }

    @Override public void onClick(View v) {
      switch (entity.getState()) {
        case DownloadEntity.STATE_WAIT:
        case DownloadEntity.STATE_OTHER:
        case DownloadEntity.STATE_FAIL:
        case DownloadEntity.STATE_STOP:
        case DownloadEntity.STATE_COMPLETE:
          start(entity);
          break;
        case DownloadEntity.STATE_DOWNLOAD_ING:
          stop(entity);
          break;
      }
    }

    private void start(DownloadEntity entity) {
      IDownloadCmd startCmd = mFactory.createCmd(entity, CmdFactory.TASK_START);
      mManager.setCmd(startCmd).exe();
    }

    private void stop(DownloadEntity entity) {
      IDownloadCmd stopCmd = mFactory.createCmd(entity, CmdFactory.TASK_STOP);
      mManager.setCmd(stopCmd).exe();
    }
  }

  class MyHolder extends AbsHolder {
    @Bind(R.id.progressBar) HorizontalProgressBarWithNumber progress;
    @Bind(R.id.bt)          Button                          bt;
    @Bind(R.id.speed)       TextView                        speed;
    @Bind(R.id.fileSize)    TextView                        fileSize;
    @Bind(R.id.del)         TextView                        cancel;

    MyHolder(View itemView) {
      super(itemView);
    }
  }
}