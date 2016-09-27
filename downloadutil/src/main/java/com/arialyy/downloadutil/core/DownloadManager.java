package com.arialyy.downloadutil.core;

import android.content.Context;
import com.arialyy.downloadutil.core.command.IDownloadCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2016/8/11.
 * 下载管理器，通过命令的方式控制下载
 */
public class DownloadManager {
  private static final Object LOCK = new Object();
  private static volatile DownloadManager INSTANCE = null;
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

  private List<IDownloadCommand> mCommands = new ArrayList<>();

  private DownloadManager() {

  }

  private Context mContext;

  private DownloadManager(Context context) {
    mContext = context;
  }

  public static DownloadManager getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new DownloadManager(context.getApplicationContext());
      }
    }
    return INSTANCE;
  }

  /**
   * 设置命令
   */
  public void setCommant(IDownloadCommand command) {
    mCommands.add(command);
  }

  /**
   * 设置一组命令
   */
  public void setCommands(List<IDownloadCommand> commands) {
    if (commands != null && commands.size() > 0) {
      mCommands.addAll(commands);
    }
  }

  /**
   * 执行所有设置的命令
   */
  public synchronized void exe() {
    for (IDownloadCommand command : mCommands) {
      command.executeComment();
    }
    mCommands.clear();
  }
}
