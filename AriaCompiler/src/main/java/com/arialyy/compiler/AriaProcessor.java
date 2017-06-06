package com.arialyy.compiler;

import com.arialyy.annotations.Download;
import com.arialyy.annotations.Upload;
import com.google.auto.service.AutoService;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Created by lyy on 2017/6/6.
 * 事件注解扫描器
 */
@AutoService(Processor.class) public class AriaProcessor extends AbstractProcessor {
  ElementHandle mHandler;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    PrintLog.init(processingEnv.getMessager());
    mHandler = new ElementHandle(processingEnv.getFiler());
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotataions = new LinkedHashSet<>();
    annotataions.add(Download.onPre.class.getCanonicalName());
    annotataions.add(Download.onNoSupportBreakPoint.class.getCanonicalName());
    annotataions.add(Download.onTaskCancel.class.getCanonicalName());
    annotataions.add(Download.onTaskComplete.class.getCanonicalName());
    annotataions.add(Download.onTaskFail.class.getCanonicalName());
    annotataions.add(Download.onTaskPre.class.getCanonicalName());
    annotataions.add(Download.onTaskResume.class.getCanonicalName());
    annotataions.add(Download.onTaskRunning.class.getCanonicalName());
    annotataions.add(Download.onTaskStart.class.getCanonicalName());
    annotataions.add(Download.onTaskStop.class.getCanonicalName());
    annotataions.add(Upload.onPre.class.getCanonicalName());
    annotataions.add(Upload.onNoSupportBreakPoint.class.getCanonicalName());
    annotataions.add(Upload.onTaskCancel.class.getCanonicalName());
    annotataions.add(Upload.onTaskComplete.class.getCanonicalName());
    annotataions.add(Upload.onTaskFail.class.getCanonicalName());
    annotataions.add(Upload.onTaskPre.class.getCanonicalName());
    annotataions.add(Upload.onTaskResume.class.getCanonicalName());
    annotataions.add(Upload.onTaskRunning.class.getCanonicalName());
    annotataions.add(Upload.onTaskStart.class.getCanonicalName());
    annotataions.add(Upload.onTaskStop.class.getCanonicalName());
    return annotataions;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    PrintLog.getInstance().info("开始扫描");
    mHandler.clean();
    mHandler.handleDownload(roundEnv);
    mHandler.handleUpload(roundEnv);
    return true;
  }
}
