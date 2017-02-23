package com.arialyy.aria.core;

/**
 * Created by Aria.Lao on 2017/1/23.
 */

public enum RequestEnum {
  GET("GET"), POST("POST");

  String name;

  RequestEnum(String name) {
    this.name = name;
  }

}
