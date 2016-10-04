package com.arialyy.downloadutil.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lyy on 2015/11/2.
 * 忽略某个字段
 */
@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME) public @interface Ignore {
    boolean value() default true;
}
