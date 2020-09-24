package com.lzq.dubboaopdemo;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.lzq.dubboaopdemo.aopservice.AopService;

public class DubboAopDemo {
    public static void main(String[] args) {
        ExtensionLoader<AopService> loader = ExtensionLoader.getExtensionLoader(AopService.class);
        AopService service = loader.getExtension("java");
        service.service();
    }
}
