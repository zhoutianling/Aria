package com.arialyy.compiler;

import com.arialyy.annotations.Download;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by lyy on 2017/6/6.
 * 元素处理
 */
class ElementHandle {

  private Filer mFiler;

  ElementHandle(Filer filer) {
    mFiler = filer;
  }

  /**
   * VariableElement 一般代表成员变量
   * ExecutableElement 一般代表类中的方法
   * TypeElement 一般代表代表类
   * PackageElement 一般代表Package
   */
  void handle(RoundEnvironment roundEnv) {
    handlePre(roundEnv);
  }

  /**
   * 处理{@link Download.onTaskPre}注解
   */
  private void handlePre(RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(Download.onPre.class)) {
      ElementKind kind = element.getKind();
      if (kind == ElementKind.METHOD) {
        ExecutableElement method = (ExecutableElement) element;
        String methodName = method.getSimpleName().toString();
        String className = method.getEnclosingElement().toString();
        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE)){
          PrintLog.getInstance().info("私有方法");
        }
        PrintLog.getInstance().info("注解的方法：" + methodName);
        PrintLog.getInstance().info("所在类：" + className);
        for (VariableElement te : method.getParameters()) {
          TypeMirror paramType = te.asType();
          PrintLog.getInstance()
              .info("参数名：" + te.getSimpleName().toString() + "，参数类型：" + paramType.toString());
        }
      }
    }
  }

  private void handleNoSupportBreakPoint(RoundEnvironment roundEnv) {

  }

  private void handleTaskCancel(RoundEnvironment roundEnv) {

  }
}
