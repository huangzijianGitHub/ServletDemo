package com.service.impl;

/*
 * @author <a>huangzijian</a>
 * @version 1.0, 2019-12-31
 * @description 
 */

import com.annotation.MyRepository;
import com.service.UserDao;

@MyRepository("userDaoImpl")
public class UserDaoImpl implements UserDao {

    @Override
    public void insert() {
        System.out.println("execute UserDaoImpl insert()");
    }
}
