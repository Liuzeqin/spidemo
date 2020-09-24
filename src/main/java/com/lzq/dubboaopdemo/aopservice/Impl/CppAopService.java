package com.lzq.dubboaopdemo.aopservice.Impl;

import com.lzq.dubboaopdemo.aopservice.AopService;

public class CppAopService implements AopService {
    @Override
    public void service() {
        System.out.println("this is c++ aop service");
    }
}
