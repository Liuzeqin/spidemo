package com.lzq.dubbospidemo;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.lzq.dubbospidemo.service.LoadBalance;


public class DubboDemo {
    public static void main(String[] args) {
        ExtensionLoader<LoadBalance> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);
        LoadBalance demoBalance = extensionLoader.getExtension("demo");
        demoBalance.Hello();
        LoadBalance testBalance = extensionLoader.getExtension("test");
        testBalance.Hello();
    }
}
