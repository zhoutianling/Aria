package com.arialyy.downloadutil.core;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import com.arialyy.downloadutil.orm.DbEntity;
import com.arialyy.downloadutil.orm.DbUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2016/8/11.
 * 下载管理器，通过命令的方式控制下载
 */
public class DownloadManager {
  private static final    String          TAG      = "DownloadManager";
  private static final    Object          LOCK     = new Object();
  private static volatile DownloadManager INSTANCE = null;

  /**
   * 预处理完成
   */
  public static final String ACTION_PRE = "ACTION_PRE";

  /**
   * 下载开始前事件
   */
  public static final String ACTION_POST_PRE = "ACTION_POST_PRE";

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
  public static final String ENTITY = "DOWNLOAD_ENTITY";

  /**
   * 位置
   */
  public static final String CURRENT_LOCATION = "CURRENT_LOCATION";

  /**
   * 速度
   */
  public static final String CURRENT_SPEED = "CURRENT_SPEED";

  private List<IDownloadCmd> mCommands = new ArrayList<>();

  private DownloadManager() {

  }

  private Context mContext;

  private DownloadManager(Context context) {
    mContext = context;
    DownloadTaskQueue.init(context);
    DbUtil.init(context);
  }

  public static DownloadManager init(Context context) {
    if (context instanceof Application) {
      if (INSTANCE == null) {
        synchronized (LOCK) {
          INSTANCE = new DownloadManager(context.getApplicationContext());
        }
      }
    } else {
      Log.e(TAG, "Context 只能为application");
    }
    return INSTANCE;
  }

  public static DownloadManager getInstance() {
    if (INSTANCE == null) {
      throw new NullPointerException("请在Application中调用init进行下载器注册");
    }
    return INSTANCE;
  }

  public List<DownloadEntity> getAllDownloadEntity() {
    return DbEntity.findAllData(DownloadEntity.class);
  }

  /**
   * 设置命令
   */
  public DownloadManager setCmd(IDownloadCmd command) {
    mCommands.add(command);
    return this;
  }

  /**
   * 设置一组命令
   */
  public DownloadManager setCmds(List<IDownloadCmd> commands) {
    if (commands != null && commands.size() > 0) {
      mCommands.addAll(commands);
    }
    return this;
  }

  /**
   * 执行所有设置的命令
   */
  public synchronized void exe() {
    for (IDownloadCmd command : mCommands) {
      command.executeComment();
    }
    mCommands.clear();
  }
}
