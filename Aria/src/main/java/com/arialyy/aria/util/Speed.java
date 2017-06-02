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
package com.arialyy.aria.util;

/**
 * Created by lyy on 2017/3/6.
 */
public enum Speed {
  /**
   * 最大速度为256kb
   */
  KB_256(64), /**
   * 最大速度为512kb
   */
  KB_512(128), /**
   * 最大速度为1mb
   */
  MB_1(256), /**
   * 最大速度为2mb
   */
  MB_2(1024), /**
   * 最大速度为10mb
   */
  MAX(8192);
  int buf;

  Speed(int buf) {
    this.buf = buf;
  }

}
