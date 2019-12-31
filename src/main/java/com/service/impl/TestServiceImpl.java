package com.service.impl;

import com.annotation.MyAutowired;
import com.annotation.MyService;
import com.service.TestService;
import com.service.UserDao;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description 
 */
@MyService
public class TestServiceImpl implements TestService {

    @MyAutowired
    UserDao userDao;


    @Override
    public void printParam(String param) {
        System.out.println("接收到的参数为：" + param);
        userDao.insert();
    }

}
