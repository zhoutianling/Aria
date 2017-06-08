/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
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
package com.arialyy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Aria.Lao on 2017/6/6.
 * Aria下载事件被注解的方法中，参数仅能有一个，参数类型为{@link com.arialyy.aria.core.download.DownloadTask}
 * <pre>
 *   <code>
 *       protected void onPre(DownloadTask task) {
 *        if (task.getKey().equals(DOWNLOAD_URL)) {
 *           mUpdateHandler.obtainMessage(DOWNLOAD_PRE, task.getDownloadEntity().getFileSize()).sendToTarget();
 *        }
 *       }
 *   </code>
 * </pre>
 */
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Download {
  /**
   * 如果你在方法中添加{@code @Download.onPre}注解，在预处理完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onPre {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskPre}注解，在任务预处理完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskPre {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskResume}注解，在任务恢复下载时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskResume {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskStart {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskStop}注解，在任务停止时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskStop {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskCancel}l注解，在任务取消时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskCancel {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskFail)注解，在任务预失败时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskFail {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskComplete}注解，在任务完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskComplete {
  }

  /**
   * 如果你在方法中添加{@code @Download.onTaskRunning}注解，在任务正在下载，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskRunning {
  }

  /**
   * 如果你在方法中添加{@code @Download.onNoSupportBreakPoint}注解，如果该任务不支持断点，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD)
  public @interface onNoSupportBreakPoint {
  }
}
