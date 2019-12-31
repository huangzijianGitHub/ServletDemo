package com.annotation;

import java.lang.annotation.*;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description  自动注入的注解
 */
@Documented
@Target(ElementType.FIELD)//(字段)标志此注解可以修饰在哪些地方，类，成员变量，方法.
@Retention(RetentionPolicy.RUNTIME)//Annotation的生命周期，一般情况下，我们自定义注解的话，显然需要在运行期获取注解的一些信息。
public @interface MyAutowired {
    public String value() default "";

}
