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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
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
    //saveMethod(false, roundEnv, Upload.onTaskResume.class);
    saveMethod(false, roundEnv, Upload.onTaskRunning.class);
    saveMethod(false, roundEnv, Upload.onTaskStart.class);
    saveMethod(false, roundEnv, Upload.onTaskStop.class);
  }

  void printMethods() {
    //Set<String> keys = mMethods.keySet();
    //for (String key : keys) {
    //  ProxyEntity entity = mMethods.get(key);
    //  for (String method : entity.methods) {
    //    PrintLog.getInstance().info(method);
    //  }
    //}
    PrintLog.getInstance().info("size ==> " + mMethods.size());
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
    return MethodSpec.methodBuilder(annotation.getSimpleName())
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(parameterSpec)
        .addAnnotation(Override.class)
        .addCode("obj."
            + methodName
            + "("
            + (isDownload ? "(DownloadTask)" : "(UploadTask)")
            + "task);\n")
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

    //添加注解方法
    for (Class<? extends Annotation> annotation : entity.methods.keySet()) {
      MethodSpec method =
          createProxyMethod(entity.isDownlaod, annotation, entity.methods.get(annotation));
      builder.addMethod(method);
    }

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
      Class<? extends Annotation> annotationClazz) {
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
          //proxyEntity.packageName = classElement.getQualifiedName().toString();
          proxyEntity.packageName = packageElement.getQualifiedName().toString();
          proxyEntity.className = classElement.getSimpleName().toString();
          mMethods.put(className, proxyEntity);
        }
        proxyEntity.methods.put(annotationClazz, methodName);
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
