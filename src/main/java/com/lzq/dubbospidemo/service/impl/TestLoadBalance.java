package com.lzq.dubbospidemo.service.impl;

import com.lzq.dubbospidemo.service.LoadBalance;

public class TestLoadBalance implements LoadBalance {
    @Override
    public void Hello() {
        System.out.println("this is test balance");
    }
}
