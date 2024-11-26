package com.ziio.twitterdemo.cos;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferManager;
import com.ziio.twitterdemo.cosntant.CosConstant;

public class CosManager {

    /**
     * 上传二进制数组
     * @param bytes
     * @param cosPath
     * @param cosClient
     */
    public static void uploadBinary(byte[] bytes , String cosPath , CosClient cosClient){

    }
}
