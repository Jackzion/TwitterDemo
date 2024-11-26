package com.ziio.twitterdemo.cos;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.ziio.twitterdemo.config.CosConfig;

/**
 *  构建 TransferManager 单例
 */
public class CosClient {

    private TransferManager transferManager;

    private Context context;

    public CosClient(Context context) {
        this.context = context;
        String secretId = CosConfig.SECRETID; // 替换为你的SecretId
        String secretKey = CosConfig.SECRETKEY; // 替换为你的SecretKey
        // 存储桶所在地域简称，例如广州地区是 ap-guangzhou
        String region = CosConfig.REGION;
        QCloudCredentialProvider myCredentialProvider =
                new ShortTimeCredentialProvider(secretId, secretKey, 300);
        // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
                .builder();
        // 创建单例
        CosXmlService cosXmlService = new CosXmlService(this.context, serviceConfig, myCredentialProvider);
        // 默认对大于或等于2M的文件自动进行分块上传，分块大小为1M，可以通过如下代码修改分块阈值
        TransferConfig transferConfig = new TransferConfig.Builder()
                // 设置启用分块上传的最小对象大小 默认为2M
                .setDivisionForUpload(2097152)
                // 设置分块上传时的分块大小 默认为1M
                .setSliceSizeForUpload(1048576)
                // 设置是否强制使用简单上传, 禁止分块上传
                .setForceSimpleUpload(false)
                .build();
        // 初始化 TransferManager
        this.transferManager = new TransferManager(cosXmlService,transferConfig);
    }

    public Context getContext() {
        return context;
    }

    public TransferManager getTransferManager() {
        return transferManager;
    }
}
