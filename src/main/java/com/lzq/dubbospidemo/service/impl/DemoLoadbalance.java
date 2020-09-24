package com.lzq.dubbospidemo.service.impl;

import com.lzq.dubbospidemo.service.LoadBalance;


public class DemoLoadbalance implements LoadBalance {

    @Override
    public void Hello() {
        System.out.println("this is demo balance");
    }
}
