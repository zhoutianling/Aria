package com.arialyy.compiler;

import com.arialyy.annotations.Download;
import com.arialyy.annotations.Upload;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Created by lyy on 2017/6/6.
 * 元素处理
 */
class ElementHandle {

  private Filer mFiler;
  private Map<String, Set<String>> mMethods = new HashMap<>();

  ElementHandle(Filer filer) {
    mFiler = filer;
  }

  /**
   * VariableElement 一般代表成员变量
   * ExecutableElement 一般代表类中的方法
   * TypeElement 一般代表代表类
   * PackageElement 一般代表Package
   */
  void handleDownload(RoundEnvironment roundEnv) {
    saveMethod(true, roundEnv, Download.onNoSupportBreakPoint.class);
    saveMethod(true, roundEnv, Download.onPre.class);
    saveMethod(true, roundEnv, Download.onTaskCancel.class);
    saveMethod(true, roundEnv, Download.onTaskComplete.class);
    saveMethod(true, roundEnv, Download.onTaskFail.class);
    saveMethod(true, roundEnv, Download.onTaskPre.class);
    saveMethod(true, roundEnv, Download.onTaskResume.class);
    saveMethod(true, roundEnv, Download.onTaskRunning.class);
    saveMethod(true, roundEnv, Download.onTaskStart.class);
    saveMethod(true, roundEnv, Download.onTaskStop.class);
  }

  void handleUpload(RoundEnvironment roundEnv) {
    saveMethod(false, roundEnv, Upload.onNoSupportBreakPoint.class);
    saveMethod(false, roundEnv, Upload.onPre.class);
    saveMethod(false, roundEnv, Upload.onTaskCancel.class);
    saveMethod(false, roundEnv, Upload.onTaskComplete.class);
    saveMethod(false, roundEnv, Upload.onTaskFail.class);
    saveMethod(false, roundEnv, Upload.onTaskPre.class);
    saveMethod(false, roundEnv, Upload.onTaskResume.class);
    saveMethod(false, roundEnv, Upload.onTaskRunning.class);
    saveMethod(false, roundEnv, Upload.onTaskStart.class);
    saveMethod(false, roundEnv, Upload.onTaskStop.class);
  }

  void clean() {
    mMethods.clear();
  }

  /**
   * 查找并保存扫描到的方法
   */
  private void saveMethod(boolean isDownload, RoundEnvironment roundEnv,
      Class<? extends Annotation> annotationClazz) {
    for (Element element : roundEnv.getElementsAnnotatedWith(annotationClazz)) {
      ElementKind kind = element.getKind();
      if (kind == ElementKind.METHOD) {
        ExecutableElement method = (ExecutableElement) element;
        checkDownloadMethod(isDownload, method);
        String methodName = method.getSimpleName().toString();
        String className = method.getEnclosingElement().toString();
        Set<String> methods = mMethods.get(className);
        if (methods == null) {
          methods = new HashSet<>();
          mMethods.put(className, methods);
        }
        methods.add(methodName);
      }
    }
  }

  /**
   * 检查和下载相关的方法，如果被注解的方法为private或参数不合法，则抛异常
   */
  private void checkDownloadMethod(boolean isDownload, ExecutableElement method) {
    String methodName = method.getSimpleName().toString();
    String className = method.getEnclosingElement().toString();
    Set<Modifier> modifiers = method.getModifiers();
    if (modifiers.contains(Modifier.PRIVATE)) {
      throw new IllegalAccessError(className + "." + methodName + "不能为private方法");
    }
    List<VariableElement> params = (List<VariableElement>) method.getParameters();
    if (params.size() > 1) {
      throw new IllegalArgumentException(
          className + "." + methodName + "参数错误, 参数只有一个，且参数必须是" + getCheckParams(isDownload));
    }
    if (!params.get(0).asType().toString().equals(getCheckParams(isDownload))) {
      throw new IllegalArgumentException(className
          + "."
          + methodName
          + "参数【"
          + params.get(0).getSimpleName()
          + "】类型错误，参数必须是"
          + getCheckParams(isDownload));
    }
  }

  private String getCheckParams(boolean isDownload) {
    return isDownload ? "com.arialyy.aria.core.download.DownloadTask"
        : "com.arialyy.aria.core.upload.UploadTask";
  }
}
