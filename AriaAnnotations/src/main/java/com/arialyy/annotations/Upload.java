package com.arialyy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Aria.Lao on 2017/6/6.
 */
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Upload {
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
