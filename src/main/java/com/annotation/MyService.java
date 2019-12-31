package com.annotation;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description  Service层的注解
 */

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)//作用于类上
@Retention(RetentionPolicy.RUNTIME)
public @interface MyService {
    public String value() default "";
}
