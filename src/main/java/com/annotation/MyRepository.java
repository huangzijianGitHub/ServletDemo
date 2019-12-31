package com.annotation;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description  持久层的注解
 */

import java.lang.annotation.*;

@Documented//JavaDoc文档
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRepository {
    public String value() default "";

}
