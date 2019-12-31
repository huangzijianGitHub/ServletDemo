package com.annotation;

import java.lang.annotation.*;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description  提供依赖注入
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyQualifier {
    public String value();

}
