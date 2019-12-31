package com.annotation;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description  对于映射参数的注解
 */

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestParam {
    public String value();

}
