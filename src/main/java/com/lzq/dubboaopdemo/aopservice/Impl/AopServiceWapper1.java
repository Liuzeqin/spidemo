package com.lzq.dubboaopdemo.aopservice.Impl;

import com.lzq.dubboaopdemo.aopservice.AopService;

public class AopServiceWapper1 implements AopService {
    private AopService aopService;

    //必须有这个构造方法才能被判断为wapper类
    public AopServiceWapper1(AopService service) {
        this.aopService = service;
    }

    @Override
    public void service() {
        System.out.println("before wapper1");
        aopService.service();
        System.out.println("after wapper1");
    }
}
