package com.arialyy.simple.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;

import com.arialyy.absadapter.common.AbsHolder;
import com.arialyy.absadapter.recycler_view.AbsRVAdapter;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.command.CommandFactory;
import com.arialyy.downloadutil.core.command.IDownloadCommand;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;

/**
 * Created by Lyy on 2016/9/27.
 * 下载列表适配器
 */
public class DownloadAdapter extends AbsRVAdapter<DownloadEntity, DownloadAdapter.MyHolder> {
    private static final String TAG = "DownloadAdapter";
    private DownloadManager mManager;
    private CommandFactory  mFactory;
    private Map<String, Long>    mProgress  = new HashMap<>();
    private Map<String, Integer> mPositions = new HashMap<>();

    public DownloadAdapter(Context context, List<DownloadEntity> data) {
        super(context, data);
        int i = 0;
        for (DownloadEntity entity : data) {
            mProgress.put(entity.getDownloadUrl(), entity.getCurrentProgress());
            mPositions.put(entity.getDownloadUrl(), i);
            i++;
        }
        mFactory = CommandFactory.getInstance();
        mManager = DownloadManager.getInstance();
    }

    @Override protected MyHolder getViewHolder(View convertView, int viewType) {
        return new MyHolder(convertView);
    }

    @Override protected int setLayoutId(int type) {
        return R.layout.item_download;
    }

    public synchronized void updateState(DownloadEntity entity) {
        notifyItemChanged(indexItem(entity.getDownloadUrl()));
    }

    public synchronized void setProgress(String url, long currentPosition) {
        mProgress.put(url, currentPosition);
//        int index = indexItem(url);
//        L.d(TAG, "index ==> " + index);
//        notifyItemChanged(index);
        notifyItemChanged(indexItem(url));
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

    @Override protected void bindData(MyHolder holder, int position, DownloadEntity item) {
        //holder.progress.setProgress(item.getCurrentProgress());
        long size    = item.getFileSize();
        int  current = 0;
        if (size == 0) {
            current = 0;
        }
        current = (int) (mProgress.get(item.getDownloadUrl()) * 100 / item.getFileSize());
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
            L.d(TAG, "position ==> " + position);
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

            List<IDownloadCommand> commands = new ArrayList<>();
            IDownloadCommand addCommand = mFactory.createCommand(getContext(), entity,
                                                                 CommandFactory.TASK_CREATE);
            IDownloadCommand startCommand = mFactory.createCommand(getContext(), entity,
                                                                   CommandFactory.TASK_START);
            commands.add(addCommand);
            commands.add(startCommand);
            mManager.setCommands(commands).exe();
        }

        private void stop(DownloadEntity entity) {
            IDownloadCommand stopCommand = mFactory.createCommand(getContext(), entity,
                                                                  CommandFactory.TASK_STOP);
            mManager.setCommand(stopCommand).exe();
        }

        private void cancel(DownloadEntity entity) {
            IDownloadCommand cancelCommand = mFactory.createCommand(getContext(), entity,
                                                                    CommandFactory.TASK_CANCEL);
            mManager.setCommand(cancelCommand).exe();
        }
    }

    class MyHolder extends AbsHolder {
        @Bind(R.id.progressBar) HorizontalProgressBarWithNumber progress;
        @Bind(R.id.bt)          Button                          bt;

        MyHolder(View itemView) {
            super(itemView);
        }
    }
}
