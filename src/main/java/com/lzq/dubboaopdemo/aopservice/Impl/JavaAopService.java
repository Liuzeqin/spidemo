package com.lzq.dubboaopdemo.aopservice.Impl;

import com.lzq.dubboaopdemo.aopservice.AopService;

public class JavaAopService implements AopService {
    @Override
    public void service() {
        System.out.println("this is java aop service");
    }
}
