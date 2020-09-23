package com.lzq.dubbospidemo.service.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.lzq.dubbospidemo.service.LoadBalance;

import java.util.List;

public class DemoLoadbalance implements LoadBalance {

    @Override
    public void Hello() {
        System.out.println("this is demo balance");
    }
}
