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
 */
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Download {
  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onPre {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskPre {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskResume {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskStart {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskStop {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskCancel {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskFail {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskComplete {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface onTaskRunning {
  }

  @Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD)
  public @interface onNoSupportBreakPoint {
  }
}
