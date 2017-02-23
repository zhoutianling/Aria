package com.arialyy.aria.exception;

/**
 * Created by Aria.Lao on 2017/1/18.
 * Aria 文件异常
 */
public class FileException extends NullPointerException {
  private static final String ARIA_FILE_EXCEPTION = "Aria Exception:";

  public FileException(String detailMessage) {
    super(ARIA_FILE_EXCEPTION + detailMessage);
  }
}
