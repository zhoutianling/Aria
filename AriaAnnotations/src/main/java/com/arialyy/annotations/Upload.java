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
 * Created by lyy on 2017/6/6.
 * Aria下载事件注解
 */
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Upload {

  /**
   * 如果你在方法中添加{@code @Upload.onPre}注解，在预处理完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onPre {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskPre}注解，在任务预处理完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskPre {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskResume}注解，在任务恢复下载时，Aria会调用该方法
   */
  //@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskResume {
  //}

  /**
   * 如果你在方法中添加{@code @Upload.onTaskStart}注解，在任务开始下载时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskStart {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskStop}注解，在任务停止时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskStop {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskCancel}l注解，在任务取消时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskCancel {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskFail)注解，在任务预失败时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskFail {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskComplete}注解，在任务完成时，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskComplete {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onTaskRunning}注解，在任务正在下载，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskRunning {
  }

  /**
   * 如果你在方法中添加{@code @Upload.onNoSupportBreakPoint}注解，如果该任务不支持断点，Aria会调用该方法
   */
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD)
  public @interface onNoSupportBreakPoint {
  }
}
