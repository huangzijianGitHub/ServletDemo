package com.servlet;



import com.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description 
 */
public class MyDispatcherServlet  extends HttpServlet {
    private Logger logger = Logger.getLogger("init");

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    //handlerMapping的类型可以自定义为Handler

    private Map<String, Object> handlerMapping = new HashMap<String, Object>();

    private Map<String, Object> controllerMap = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init();
        logger.info("初始化MyDispatcherServlet");
        //1.加载配置文件，填充properties字段
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.根据properties，初始化所有相关联的类，扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));

        //3.拿到扫描到的类，通过反射的机制，实例化，并且放到ioc容器中（k-v  beanName  bean）
        doInstance();

        //4.自动化注入
        doAutowired();

        //5.初始化HandlerMapping（将url和method对应上）
        initHandlerMapping();

        doAutowired2();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        //注释掉父类的实现，不然会报错：405 Http method  POST  is not supported by ...
        //super.doPost(req, resp);
        logger.info("执行MyDispatcherServlet的doPost()方法");
        try {
            //处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                resp.getWriter().write("500!!! Server Exception");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        //注释掉父类的实现，不然会报错：405 Http method  GET  is not supported by ...
        //super.doGet(req, resp);
        logger.info("在执行MyDispatcherServlet的doGet()方法");
        try {
            //处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                resp.getWriter().write("500!!! Server Exception");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        if (handlerMapping.isEmpty()){
            return;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        //去掉url前面的斜杆“/”，所有的@Myrequestmapping可以不用写斜杆“/”
        if (url.lastIndexOf('/') != 0) {
            url = url.substring(1);
        }
        if (!this.handlerMapping.containsKey(url)) {
            try {
                resp.getWriter().write("404 NOT FOUND");
                logger.info("404 NOT FOUND!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Method method = (Method) this.handlerMapping.get(url);
        //获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //保存的参数值
        Object[] paramValues = new Object[parameterTypes.length];
        //方法的参数列表
        for (int i =0, len = parameterTypes.length; i < len; i ++) {
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();
            if ("HttpServletRequest".equals(requestParam)) {
                //参数类型已经明确，这边强转类型
                paramValues[i] = req;
                continue;
            }
            if ("HttpServletResponse".equals(requestParam)) {
                paramValues[i] = resp;
                continue;

            }
            if ("String".equals(requestParam)) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                    paramValues[i] = value;

                }
            }
        }
        //利用反射机制来调用
        try {
            //第一个参数是method所对应的实例  在ioc容器中
            method.invoke(this.controllerMap.get(url),paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据配置文件位置，读取配置文件中的配置信息，将其填充到properties字段
     * @param location  配置文件的位置
     */
    private void doLoadConfig(String location) {

        //将web.xml中的contextConfigLocation对应value值得文件加载到流里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            //用Properties文件加载文件里的内容
            logger.info("读取"+location+"里面的文件");
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关流
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将指定包下扫描得到的类，添加到classNames字段中；
     * @param packageName 需要扫描的包名
     *
     */
    private void doScanner(String packageName) {
        URL url  =this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //递归取包
                doScanner(packageName+"."+file.getName());
            } else {
                String className =packageName +"." +file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 将classNames中的类实例化，经key-value：类名（小写）-类对象放入ioc字段中
     * Params:
     */
    private void doInstance() {

        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                //把类搞出来，反射来实例化（只有加@Controller需要实例化）
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyService.class)){
                    MyService myService=clazz.getAnnotation(MyService.class);
                    String beanName=myService.value();
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstWord(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    Class[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), instance);
                    }
                } else if(clazz.isAnnotationPresent(MyRepository.class)) {

                    MyRepository myRepository=clazz.getAnnotation(MyRepository.class);
                    String beanName=myRepository.value();
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstWord(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    Class[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 自动化的依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //包括私有的方法，在spring中没有隐私，@MyAutowired可以注入public、private字段
            Field[] fields=entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields){
                if (!field.isAnnotationPresent(MyAutowired.class)){
                    continue;
                }
                MyAutowired autowired= field.getAnnotation(MyAutowired.class);
                String beanName=autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void doAutowired2() {
        if (controllerMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String,Object> entry : controllerMap.entrySet()) {
            //包括私有的方法，在spring中没有隐私，@MyAutowired可以注入public、private字段
            Field[] fields=entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields){
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    continue;
                }
                MyAutowired autowired= field.getAnnotation(MyAutowired.class);
                String beanName=autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * \初始化HandlerMapping(将url和method对应上)
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<? extends Object> clazz = entry.getValue().getClass();
                if(!clazz.isAnnotationPresent(MyController.class)){
                    continue;
                }
                //拼接url时，是Controller头的url拼上方法的url
                String baseUrl = "";
                if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = annotation.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                        continue;
                    }
                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = annotation.value();

                    url = (baseUrl + "/" +url).replaceAll("/+", "/");
                    handlerMapping.put(url, method);
                    controllerMap.put(url, clazz.newInstance());
                    System.out.println(url + "," + method);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * 将字符串中的首字母小写
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

}
