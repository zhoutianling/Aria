package com.arialyy.downloadutil.core;

import android.content.Context;

/**
 * Created by lyy on 2016/8/11.
 * 下载管理器，通过命令的方式控制下载
 */
public class DownloadManager {
    /**
     * 下载开始前事件
     */
    public static final String ACTION_PRE = "ACTION_PRE";

    /**
     * 开始下载事件
     */
    public static final String ACTION_START = "ACTION_START";

    /**
     * 恢复下载事件
     */
    public static final String ACTION_RESUME = "ACTION_RESUME";

    /**
     * 正在下载事件
     */
    public static final String ACTION_RUNNING = "ACTION_RUNNING";

    /**
     * 停止下载事件
     */
    public static final String ACTION_STOP = "ACTION_STOP";

    /**
     * 取消下载事件
     */
    public static final String ACTION_CANCEL = "ACTION_CANCEL";

    /**
     * 下载完成事件
     */
    public static final String ACTION_COMPLETE = "ACTION_COMPLETE";

    /**
     * 下载失败事件
     */
    public static final String ACTION_FAIL = "ACTION_FAIL";

    /**
     * 下载实体
     */
    public static final String DATA = "DOWNLOAD_ENTITY";

    /**
     * 位置
     */
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";

    private Context mContext;

    private DownloadManager(Context context) {
        mContext = context;
    }

    public static DownloadManager getInstance(Context context) {
        return new DownloadManager(context);
    }

}
