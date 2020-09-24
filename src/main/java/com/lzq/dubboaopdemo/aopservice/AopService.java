package com.lzq.dubboaopdemo.aopservice;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

@SPI
public interface AopService {

    @Adaptive
    void service();
}
