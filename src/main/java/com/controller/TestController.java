package com.controller;

import com.annotation.MyAutowired;
import com.annotation.MyController;
import com.annotation.MyRequestMapping;
import com.annotation.MyRequestParam;
import com.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description 
 */
@MyController
public class TestController {
    @MyAutowired
     TestService testService;

    @MyRequestMapping("test")
    public void myTest(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("param") String param) {
        try {
            resp.getWriter().write("TestController1: the param you send is :" + param);
            testService.printParam(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
