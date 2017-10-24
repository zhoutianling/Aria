package com.arialyy.aria.core;

import java.net.InetAddress;

/**
 * Created by Aria.Lao on 2017/10/24.
 * ftp url 信息链接实体
 */
public class FtpUrlEntity {

  public String remotePath;

  public String account;

  /**
   * 原始url
   */
  public String url;

  /**
   * ftp协议
   */
  public String protocol;

  /**
   * 用户
   */
  public String user;
  /**
   * 密码
   */
  public String password;

  /**
   * 端口
   */
  public String port;

  /**
   * 主机域名
   */
  public String hostName;

  /**
   * 是否需要登录
   */
  public boolean needLogin = false;

  /**
   * 有效的ip地址
   */
  public InetAddress validAddr;
}
