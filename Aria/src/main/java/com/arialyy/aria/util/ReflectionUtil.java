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

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by “AriaLyy@outlook.com” on 2015/7/30.
 * 反射工具类
 */
public class ReflectionUtil {
  private static final String TAG = "ReflectionUtil";

  /**
   * 获取类里面的所在字段
   */
  public static Field[] getFields(Class clazz) {
    Field[] fields = null;
    fields = clazz.getDeclaredFields();
    if (fields == null || fields.length == 0) {
      Class superClazz = clazz.getSuperclass();
      if (superClazz != null) {
        fields = getFields(superClazz);
      }
    }
    return fields;
  }

  /**
   * 获取所有字段，包括父类的字段
   */
  public static List<Field> getAllFields(Class clazz) {
    List<Field> fields = new ArrayList<>();
    Class personClazz = clazz.getSuperclass();
    if (personClazz != null) {
      Collections.addAll(fields, personClazz.getDeclaredFields());
    }
    Collections.addAll(fields, clazz.getDeclaredFields());
    return fields;
  }

  /**
   * 获取类里面的指定对象，如果该类没有则从父类查询
   */
  public static Field getField(Class clazz, String name) {
    Field field = null;
    try {
      field = clazz.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      try {
        field = clazz.getField(name);
      } catch (NoSuchFieldException e1) {
        if (clazz.getSuperclass() == null) {
          return field;
        } else {
          field = getField(clazz.getSuperclass(), name);
        }
      }
    }
    if (field != null) {
      field.setAccessible(true);
    }
    return field;
  }

  /**
   * 利用递归找一个类的指定方法，如果找不到，去父亲里面找直到最上层Object对象为止。
   *
   * @param clazz 目标类
   * @param methodName 方法名
   * @param params 方法参数类型数组
   * @return 方法对象
   */
  public static Method getMethod(Class clazz, String methodName, final Class<?>... params) {
    Method method = null;
    try {
      method = clazz.getDeclaredMethod(methodName, params);
    } catch (NoSuchMethodException e) {
      try {
        method = clazz.getMethod(methodName, params);
      } catch (NoSuchMethodException ex) {
        if (clazz.getSuperclass() == null) {
          Log.e(TAG, "无法找到" + methodName + "方法");
          return method;
        } else {
          method = getMethod(clazz.getSuperclass(), methodName, params);
        }
      }
    }
    if (method != null) {
      method.setAccessible(true);
    }
    return method;
  }
}
