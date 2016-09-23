package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by Lyy on 2016/9/23.
 * 命令工厂
 */
public class CommandFactory {
  /**
   * 创建任务
   */
  public static final int TASK_CREATE = 0x122;
  /**
   * 启动任务
   */
  public static final int TASK_START = 0x123;
  /**
   * 取消任务
   */
  public static final int TASK_CANCEL = 0x124;
  /**
   * 停止任务
   */
  public static final int TASK_STOP = 0x125;
  /**
   * 获取任务状态
   */
  public static final int TASK_STATE = 0x126;

  private static final Object LOCK = new Object();
  private static volatile CommandFactory INSTANCE = null;

  private CommandFactory() {

  }

  public static CommandFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new CommandFactory();
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
  public IDownloadCommand createCommand(Context context, DownloadEntity entity, int type) {
    switch (type) {
      case TASK_CREATE:
        return createAddCommand(context, entity);
      case TASK_START:
        return createStartCommand(context, entity);
      case TASK_CANCEL:
        return createCancelCommand(context, entity);
      case TASK_STOP:
        return createStopCommand(context, entity);
      case TASK_STATE:
        return createStateCommand(context, entity);
      default:
        return null;
    }
  }

  /**
   * 创建获取任务状态的命令
   *
   * @return {@link StateCommand}
   */
  private StateCommand createStateCommand(Context context, DownloadEntity entity) {
    return new StateCommand(context, entity);
  }

  /**
   * 创建停止命令
   *
   * @return {@link StopCommand}
   */
  private StopCommand createStopCommand(Context context, DownloadEntity entity) {
    return new StopCommand(context, entity);
  }

  /**
   * 创建下载任务命令
   *
   * @return {@link AddCommand}
   */
  private AddCommand createAddCommand(Context context, DownloadEntity entity) {
    return new AddCommand(context, entity);
  }

  /**
   * 创建启动下载命令
   *
   * @return {@link StartCommand}
   */
  private StartCommand createStartCommand(Context context, DownloadEntity entity) {
    return new StartCommand(context, entity);
  }

  /**
   * 创建 取消下载的命令
   *
   * @return {@link CancelCommand}
   */
  private CancelCommand createCancelCommand(Context context, DownloadEntity entity) {
    return new CancelCommand(context, entity);
  }
}
