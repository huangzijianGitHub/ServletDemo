package com.annotation;

import java.lang.annotation.*;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description  方法映射的注解
 */
@Documented
@Target({ElementType.METHOD,ElementType.TYPE})//作用于类或者方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestMapping {
    public String value();

}
