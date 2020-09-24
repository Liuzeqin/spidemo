package com.lzq.dubboaopdemo.aopservice.Impl;

import com.lzq.dubboaopdemo.aopservice.AopService;

public class AopServiceWapper2 implements AopService {
    private AopService aopService;

    //必须有这个构造方法才能被判断为wapper类
    public AopServiceWapper2(AopService service) {
        this.aopService = service;
    }

    @Override
    public void service() {
        System.out.println("before wapper2");
        aopService.service();
        System.out.println("after wapper2");
    }
}
