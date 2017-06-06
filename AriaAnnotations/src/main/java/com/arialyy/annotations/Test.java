package com.arialyy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Aria.Lao on 2017/6/6.
 */
@Retention(RetentionPolicy.CLASS) @Target(ElementType.METHOD) public @interface Test {
}
