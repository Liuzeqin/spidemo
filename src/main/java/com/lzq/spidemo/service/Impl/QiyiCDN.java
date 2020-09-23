package com.lzq.spidemo.service.Impl;

import com.lzq.spidemo.service.UploadCDN;

public class QiyiCDN implements UploadCDN {
    @Override
    public void upload(String url) {
        System.out.println("upload to Qiyi");
    }
}
