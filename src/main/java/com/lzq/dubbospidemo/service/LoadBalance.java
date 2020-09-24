package com.lzq.dubbospidemo.service;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;


@SPI("demo")
public interface LoadBalance {

    @Adaptive
    void Hello();
}
