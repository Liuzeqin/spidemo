package com.lzq.spidemo;

import com.lzq.spidemo.service.UploadCDN;

import java.util.ServiceLoader;

public class Demo {
    public static void main(String[] args) {
        //注意："META-INF/services/"这个目录是写死的
        ServiceLoader<UploadCDN> uploadCDN = ServiceLoader.load(UploadCDN.class);
        for (UploadCDN u : uploadCDN) {
            u.upload("filePath");
        }
    }
}
