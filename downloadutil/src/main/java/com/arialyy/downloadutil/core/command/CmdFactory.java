package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadEntity;

/**
 * Created by Lyy on 2016/9/23.
 * 命令工厂
 */
public class CmdFactory {
  /**
   * 创建任务
   */
  public static final int TASK_CREATE = 0x122;
  /**
   * 启动任务
   */
  public static final int TASK_START  = 0x123;
  /**
   * 恢复任务
   */
  public static final int TASK_RESUME = 0x127;
  /**
   * 取消任务
   */
  public static final int TASK_CANCEL = 0x124;
  /**
   * 停止任务
   */
  public static final int TASK_STOP   = 0x125;
  /**
   * 获取任务状态
   */
  public static final int TASK_STATE  = 0x126;

  private static final    Object     LOCK     = new Object();
  private static volatile CmdFactory INSTANCE = null;

  private CmdFactory() {

  }

  public static CmdFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new CmdFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * @param context context
   * @param entity 下载实体
   * @param type 命令类型{@link #TASK_CREATE}、{@link #TASK_START}、{@link #TASK_CANCEL}、{@link
   * #TASK_STOP}、{@link #TASK_STATE}
   */
  public IDownloadCmd createCmd(Context context, DownloadEntity entity, int type) {
    switch (type) {
      case TASK_CREATE:
        return createAddCmd(context, entity);
      case TASK_RESUME:
      case TASK_START:
        return createStartCmd(context, entity);
      case TASK_CANCEL:
        return createCancelCmd(context, entity);
      case TASK_STOP:
        return createStopCmd(context, entity);
      case TASK_STATE:
        return createStateCmd(context, entity);
      default:
        return null;
    }
  }

  /**
   * 创建获取任务状态的命令
   *
   * @return {@link StateCmd}
   */
  private StateCmd createStateCmd(Context context, DownloadEntity entity) {
    return new StateCmd(context, entity);
  }

  /**
   * 创建停止命令
   *
   * @return {@link StopCmd}
   */
  private StopCmd createStopCmd(Context context, DownloadEntity entity) {
    return new StopCmd(context, entity);
  }

  /**
   * 创建下载任务命令
   *
   * @return {@link AddCmd}
   */
  private AddCmd createAddCmd(Context context, DownloadEntity entity) {
    return new AddCmd(context, entity);
  }

  /**
   * 创建启动下载命令
   *
   * @return {@link StartCmd}
   */
  private StartCmd createStartCmd(Context context, DownloadEntity entity) {
    return new StartCmd(context, entity);
  }

  /**
   * 创建 取消下载的命令
   *
   * @return {@link CancelCmd}
   */
  private CancelCmd createCancelCmd(Context context, DownloadEntity entity) {
    return new CancelCmd(context, entity);
  }
}
