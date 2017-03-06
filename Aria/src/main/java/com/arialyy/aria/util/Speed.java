package com.arialyy.aria.util;

/**
 * Created by Aria.Lao on 2017/3/6.
 */
public enum Speed {
  /**
   * 最大速度为256kb
   */
  KB_256(64),
  /**
   * 最大速度为512kb
   */
  KB_512(128),
  /**
   * 最大速度为1mb
   */
  MB_1(256),
  /**
   * 最大速度为2mb
   */
  MB_2(1024),
  /**
   * 最大速度为10mb
   */
  MAX(8192);
  int buf;

  Speed(int buf) {
    this.buf = buf;
  }

}
