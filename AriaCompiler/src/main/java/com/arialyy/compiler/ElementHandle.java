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
package com.arialyy.compiler;

import com.arialyy.annotations.Download;
import com.arialyy.annotations.Upload;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by lyy on 2017/6/6.
 * 元素处理
 */
class ElementHandle {

  private Filer mFiler;
  private Elements mElementUtil;
  private Map<String, ProxyEntity> mMethods = new HashMap<>();

  ElementHandle(Filer filer, Elements elements) {
    mFiler = filer;
    mElementUtil = elements;
  }

  /**
   * VariableElement 一般代表成员变量
   * ExecutableElement 一般代表类中的方法
   * TypeElement 一般代表代表类
   * PackageElement 一般代表Package
   */
  void handleDownload(RoundEnvironment roundEnv) {
    saveMethod(true, roundEnv, Download.onNoSupportBreakPoint.class,
        ProxyConstance.DOWNLOAD_TASK_NO_SUPPORT_BREAKPOINT);
    saveMethod(true, roundEnv, Download.onPre.class, ProxyConstance.DOWNLOAD_PRE);
    saveMethod(true, roundEnv, Download.onTaskCancel.class, ProxyConstance.DOWNLOAD_TASK_CANCEL);
    saveMethod(true, roundEnv, Download.onTaskComplete.class,
        ProxyConstance.DOWNLOAD_TASK_COMPLETE);
    saveMethod(true, roundEnv, Download.onTaskFail.class, ProxyConstance.DOWNLOAD_TASK_FAIL);
    saveMethod(true, roundEnv, Download.onTaskPre.class, ProxyConstance.DOWNLOAD_TASK_PRE);
    saveMethod(true, roundEnv, Download.onTaskResume.class, ProxyConstance.DOWNLOAD_TASK_RESUME);
    saveMethod(true, roundEnv, Download.onTaskRunning.class, ProxyConstance.DOWNLOAD_TASK_RUNNING);
    saveMethod(true, roundEnv, Download.onTaskStart.class, ProxyConstance.DOWNLOAD_TASK_START);
    saveMethod(true, roundEnv, Download.onTaskStop.class, ProxyConstance.DOWNLOAD_TASK_STOP);
  }

  void handleUpload(RoundEnvironment roundEnv) {
    saveMethod(false, roundEnv, Upload.onNoSupportBreakPoint.class,
        ProxyConstance.UPLOAD_TASK_NO_SUPPORT_BREAKPOINT);
    saveMethod(false, roundEnv, Upload.onPre.class, ProxyConstance.UPLOAD_PRE);
    saveMethod(false, roundEnv, Upload.onTaskCancel.class, ProxyConstance.UPLOAD_TASK_CANCEL);
    saveMethod(false, roundEnv, Upload.onTaskComplete.class, ProxyConstance.UPLOAD_TASK_COMPLETE);
    saveMethod(false, roundEnv, Upload.onTaskFail.class, ProxyConstance.UPLOAD_TASK_FAIL);
    saveMethod(false, roundEnv, Upload.onTaskPre.class, ProxyConstance.UPLOAD_TASK_PRE);
    //saveMethod(false, roundEnv, Upload.onTaskResume.class);
    saveMethod(false, roundEnv, Upload.onTaskRunning.class, ProxyConstance.UPLOAD_TASK_RUNNING);
    saveMethod(false, roundEnv, Upload.onTaskStart.class, ProxyConstance.UPLOAD_TASK_START);
    saveMethod(false, roundEnv, Upload.onTaskStop.class, ProxyConstance.UPLOAD_TASK_STOP);
  }

  /**
   * 在build文件夹中生成如下代码的文件
   * <pre>
   *   <code>
   * package com.arialyy.simple.download;
   *
   * import com.arialyy.aria.core.download.DownloadTask;
   * import com.arialyy.aria.core.scheduler.AbsSchedulerListener;
   *
   * public final class SingleTaskActivity$$DownloadListenerProxy extends
   * AbsSchedulerListener<DownloadTask> {
   * private SingleTaskActivity obj;
   *
   *    public void onPre(final DownloadTask task) {
   *      obj.onPre((DownloadTask)task);
   *    }
   *
   *    public void onTaskStart(final DownloadTask task) {
   *      obj.onStart((DownloadTask)task);
   *    }
   *
   *    public void setListener(final Object obj) {
   *      this.obj = (SingleTaskActivity)obj;
   *    }
   * }
   *   </code>
   * </pre>
   */
  void createProxyFile() {
    Set<String> keys = mMethods.keySet();
    try {
      for (String key : keys) {
        ProxyEntity entity = mMethods.get(key);
        JavaFile jf = JavaFile.builder(entity.packageName, createProxyClass(entity)).build();

        jf.writeTo(mFiler);
        // 如果需要在控制台打印生成的文件，则去掉下面的注释
        //jf.writeTo(System.out);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 创建代理方法
   *
   * @param isDownload 是否是下载的注解
   * @param annotation {@link Download}、{@link Upload}
   * @param methodName 被代理类注解的方法名
   */
  private MethodSpec createProxyMethod(boolean isDownload, Class<? extends Annotation> annotation,
      String methodName) {
    ClassName task = ClassName.get(
        isDownload ? "com.arialyy.aria.core.download" : "com.arialyy.aria.core.upload",
        isDownload ? "DownloadTask" : "UploadTask");
    ParameterSpec parameterSpec =
        ParameterSpec.builder(task, "task").addModifiers(Modifier.FINAL).build();
    StringBuilder sb = new StringBuilder();
    sb.append("Set<String> keys = keyMapping.get(\"")
        .append(methodName)
        .append("\");\n");
    sb.append("if (keys != null) {\n\tif (keys.contains(task.getKey())) {\n")
        .append("\t\tobj.")
        .append(methodName)
        .append("(")
        .append(isDownload ? "(DownloadTask)" : "(UploadTask)")
        .append("task);\n")
        .append("\t}\n} else {\n")
        .append("\tobj.")
        .append(methodName)
        .append("(")
        .append(isDownload ? "(DownloadTask)" : "(UploadTask)")
        .append("task);\n}\n");

    return MethodSpec.methodBuilder(annotation.getSimpleName())
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(parameterSpec)
        .addAnnotation(Override.class)
        .addCode(sb.toString())
        .build();
  }

  /**
   * 创建代理类
   */
  private TypeSpec createProxyClass(ProxyEntity entity) {
    TypeSpec.Builder builder = TypeSpec.classBuilder(
        entity.className + (entity.isDownlaod ? ProxyConstance.DOWNLOAD_PROXY_CLASS_SUFFIX
            : ProxyConstance.UPLOAD_PROXY_CLASS_SUFFIX))
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    //添加被代理的类的字段
    ClassName obj = ClassName.get(entity.packageName, entity.className);
    FieldSpec observerField = FieldSpec.builder(obj, "obj").addModifiers(Modifier.PRIVATE).build();
    builder.addField(observerField);

    //添加url映射表
    FieldSpec mappingField = FieldSpec.builder(
        ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class),
            ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
        "keyMapping").addModifiers(Modifier.PRIVATE).initializer("new $T()", HashMap.class).build();
    builder.addField(mappingField);

    //添加注解方法
    for (Class<? extends Annotation> annotation : entity.methods.keySet()) {
      MethodSpec method =
          createProxyMethod(entity.isDownlaod, annotation, entity.methods.get(annotation));
      builder.addMethod(method);
    }

    //增加构造函数
    CodeBlock.Builder cb = CodeBlock.builder();
    cb.add("Set<String> set = null;\n");
    for (String methodName : entity.keyMappings.keySet()) {
      Set<String> keys = entity.keyMappings.get(methodName);
      if (keys == null || keys.size() == 0) continue;
      StringBuilder sb = new StringBuilder();
      sb.append("set = new $T();\n");
      for (String key : keys) {
        if (key.isEmpty()) continue;
        sb.append("set.add(\"").append(key).append("\");\n");
      }

      sb.append("keyMapping.put(\"").append(methodName).append("\", ").append("set);\n");
      cb.add(sb.toString(), ClassName.get(HashSet.class));
    }
    MethodSpec structure =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode(cb.build()).build();
    builder.addMethod(structure);

    //添加设置代理的类
    ParameterSpec parameterSpec =
        ParameterSpec.builder(Object.class, "obj").addModifiers(Modifier.FINAL).build();
    MethodSpec listener = MethodSpec.methodBuilder(ProxyConstance.SET_LISTENER)
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(parameterSpec)
        .addAnnotation(Override.class)
        .addCode("this.obj = (" + entity.className + ")obj;\n")
        .build();
    builder.addJavadoc("该文件为Aria自动生成的代理文件，请不要修改该文件的任何代码！\n");

    //创建父类参数
    ClassName superClass = ClassName.get("com.arialyy.aria.core.scheduler", "AbsSchedulerListener");
    //创建泛型
    ClassName typeVariableName = ClassName.get(
        entity.isDownlaod ? "com.arialyy.aria.core.download" : "com.arialyy.aria.core.upload",
        entity.isDownlaod ? "DownloadTask" : "UploadTask");
    builder.superclass(ParameterizedTypeName.get(superClass, typeVariableName));
    builder.addMethod(listener);
    return builder.build();
  }

  void clean() {
    mMethods.clear();
  }

  /**
   * 查找并保存扫描到的方法
   */
  private void saveMethod(boolean isDownload, RoundEnvironment roundEnv,
      Class<? extends Annotation> annotationClazz, int annotationType) {
    for (Element element : roundEnv.getElementsAnnotatedWith(annotationClazz)) {
      ElementKind kind = element.getKind();
      if (kind == ElementKind.METHOD) {
        ExecutableElement method = (ExecutableElement) element;
        TypeElement classElement = (TypeElement) method.getEnclosingElement();
        PackageElement packageElement = mElementUtil.getPackageOf(classElement);
        checkDownloadMethod(isDownload, method);
        String methodName = method.getSimpleName().toString();
        String className = method.getEnclosingElement().toString(); //全类名
        ProxyEntity proxyEntity = mMethods.get(className);
        if (proxyEntity == null) {
          proxyEntity = new ProxyEntity();
          proxyEntity.isDownlaod = isDownload;
          proxyEntity.packageName = packageElement.getQualifiedName().toString();
          proxyEntity.className = classElement.getSimpleName().toString();
          mMethods.put(className, proxyEntity);
        }
        proxyEntity.methods.put(annotationClazz, methodName);
        proxyEntity.keyMappings.put(methodName, getValues(method, isDownload, annotationType));
      }
    }
  }

  /**
   * 获取注解的内容
   */
  private Set<String> getValues(ExecutableElement method, boolean isDownload, int annotationType) {
    String[] keys = null;
    if (isDownload) {
      switch (annotationType) {
        case ProxyConstance.DOWNLOAD_PRE:
          keys = method.getAnnotation(Download.onPre.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_PRE:
          keys = method.getAnnotation(Download.onTaskPre.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_RESUME:
          keys = method.getAnnotation(Download.onTaskResume.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_START:
          keys = method.getAnnotation(Download.onTaskStart.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_RUNNING:
          keys = method.getAnnotation(Download.onTaskRunning.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_STOP:
          keys = method.getAnnotation(Download.onTaskStop.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_COMPLETE:
          keys = method.getAnnotation(Download.onTaskComplete.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_CANCEL:
          keys = method.getAnnotation(Download.onTaskCancel.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_FAIL:
          keys = method.getAnnotation(Download.onTaskFail.class).value();
          break;
        case ProxyConstance.DOWNLOAD_TASK_NO_SUPPORT_BREAKPOINT:
          keys = method.getAnnotation(Download.onNoSupportBreakPoint.class).value();
          break;
      }
    } else {
      switch (annotationType) {
        case ProxyConstance.UPLOAD_PRE:
          keys = method.getAnnotation(Upload.onPre.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_PRE:
          keys = method.getAnnotation(Upload.onTaskPre.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_RESUME:
          //keys = method.getAnnotation(Upload.onTaskResume.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_START:
          keys = method.getAnnotation(Upload.onTaskStart.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_RUNNING:
          keys = method.getAnnotation(Upload.onTaskRunning.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_STOP:
          keys = method.getAnnotation(Upload.onTaskStop.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_COMPLETE:
          keys = method.getAnnotation(Upload.onTaskComplete.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_CANCEL:
          keys = method.getAnnotation(Upload.onTaskCancel.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_FAIL:
          keys = method.getAnnotation(Upload.onTaskFail.class).value();
          break;
        case ProxyConstance.UPLOAD_TASK_NO_SUPPORT_BREAKPOINT:
          keys = method.getAnnotation(Upload.onNoSupportBreakPoint.class).value();
          break;
      }
    }

    return keys == null ? null : convertSet(keys);
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

  /**
   * 字符串数组转set
   *
   * @param keys 注解中查到的key
   */
  private Set<String> convertSet(final String[] keys) {
    if (keys == null || keys.length == 0) {
      return null;
    }
    if (keys[0].isEmpty()) return null;
    Set<String> set = new HashSet<>();
    Collections.addAll(set, keys);
    return set;
  }

  private String getCheckParams(boolean isDownload) {
    return isDownload ? "com.arialyy.aria.core.download.DownloadTask"
        : "com.arialyy.aria.core.upload.UploadTask";
  }
}
