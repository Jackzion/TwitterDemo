package com.ziio.twitterdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferManager;
import com.ziio.twitterdemo.cos.CosClient;
import com.ziio.twitterdemo.cosntant.CosConstant;
import com.ziio.twitterdemo.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private CosClient cosClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.view01);
        Button button = findViewById(R.id.button2);
        cosClient = new CosClient(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImageInCos();
            }
        });
    }

    // 存储选中照片到 cos
    private void saveImageInCos() {
        // 生成目标路径
        String emailId = "ziio";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyHHmmss");
        String rootPath = "twitter";
        Date date = new Date();
        String imagePath =simpleDateFormat.format(date) + ".jpg";
        String cosPath = rootPath + File.separator + emailId + File.separator + imagePath;

        // 获取 ivImagePerson 图片字节流
        imageView.isDrawingCacheEnabled();
        imageView.buildDrawingCache();
        BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        // 上传 cos
        TransferManager transferManager = cosClient.getTransferManager();
        Context context = cosClient.getContext();
        // 上传字节数组
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(CosConstant.BUCKET, cosPath, data);
        //设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult uploadResult =
                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
            }
            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });

    }

}