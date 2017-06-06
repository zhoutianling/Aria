package com.arialyy.compiler;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Created by Aria.Lao on 2017/6/6.
 */

public class PrintLog {

  private volatile static PrintLog INSTANCE = null;
  private Messager mMessager;

  public static PrintLog init(Messager msg) {
    if (INSTANCE == null) {
      synchronized (PrintLog.class) {
        INSTANCE = new PrintLog(msg);
      }
    }
    return INSTANCE;
  }

  public static PrintLog getInstance() {
    return INSTANCE;
  }

  private PrintLog() {
  }

  private PrintLog(Messager msg) {
    mMessager = msg;
  }

  public void error(Element e, String msg, Object... args) {
    mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  public void error(String msg, Object... args) {
    mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  private void warning(String msg) {
    mMessager.printMessage(Diagnostic.Kind.WARNING, msg);
  }

  public void error(String msg) {
    mMessager.printMessage(Diagnostic.Kind.ERROR, msg);
  }

  public void info(String str) {
    mMessager.printMessage(Diagnostic.Kind.NOTE, str);
  }
}
