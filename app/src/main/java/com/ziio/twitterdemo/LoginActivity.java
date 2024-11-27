package com.ziio.twitterdemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferManager;
import com.ziio.twitterdemo.config.GoogleConfig;
import com.ziio.twitterdemo.cos.CosClient;
import com.ziio.twitterdemo.config.CosConfig;
import com.ziio.twitterdemo.model.Ticket;
import com.ziio.twitterdemo.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements OnCompleteListener<AuthResult> {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private ImageView ivImagePerson;

    private EditText edEmail;

    private EditText edPassword;

    private FirebaseAuth mAuth = null;

    private FirebaseDatabase firebaseDatabase;

    private DatabaseReference myRef;

    private CosClient cosClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // init fireBase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(GoogleConfig.REALTIME_DATABASE_URL);
        myRef = firebaseDatabase.getReference();
        // component init
        ivImagePerson = findViewById(R.id.ivImagePerson);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        cosClient = new CosClient(this);
        View buLogin = findViewById(R.id.buLogin);

        buLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginToFireBase(edEmail.getText().toString(),edPassword.getText().toString());
            }
        });

        ivImagePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // select image from phone
                checkPermission();
            }
        });

        activityResultLauncher  = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK && result.getData()!=null){
                    // get imagePath from data
                    Uri selectedImage = result.getData().getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    // set ivImagePerson
                    ivImagePerson.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
            }
        });
    }

    // login to fireBase by fireAuth
    private void loginToFireBase(String email ,String password){
        if(mAuth!=null){
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this);
        }
    }

    // 存储选中照片到 cos
    private void saveImageInCos(FirebaseUser currentUser) {
        // 生成目标路径
        String email = currentUser.getEmail();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyHHmmss");
        String rootPath = "twitter";
        String emailId = StringUtil.splitEmail(email);
        Date date = new Date();
        String imagePath =simpleDateFormat.format(date) + ".jpg";
        String cosPath = rootPath + File.separator + emailId + File.separator + imagePath;

        // 获取 ivImagePerson 图片字节流
        ivImagePerson.isDrawingCacheEnabled();
        ivImagePerson.buildDrawingCache();
        BitmapDrawable drawable = (BitmapDrawable)ivImagePerson.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        // 上传 cos
        TransferManager transferManager = cosClient.getTransferManager();
        Context context = cosClient.getContext();
        // 上传字节数组
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(CosConfig.BUCKET, cosPath, data);
        //设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult uploadResult =
                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                System.out.println("ziio"+ result.accessUrl);
                // 保存到 fireDatabase
                myRef.child("Users").child(currentUser.getUid()).child("email").setValue(currentUser.getEmail());
                myRef.child("Users").child(currentUser.getUid()).child("profileImage").setValue(result.accessUrl);
                // Activity 跳转
                loadTweets();
            }
            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    Toast.makeText(context,"fail to upload" , Toast.LENGTH_LONG).show();
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        loadTweets();
//    }

    // 跳转页面
    private void loadTweets(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            Intent intent = new Intent(this, IndexActivity.class);
            intent.putExtra("email",currentUser.getEmail());
            intent.putExtra("uid",currentUser.getUid());

            startActivity(intent);
        }
    }

    // 动态授权（读取图片）
    final int READIMAGE = 253;
    private void checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            // 授权
            if(ActivityCompat.checkSelfPermission(this ,
                    Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READIMAGE);
                return;
            }
        }
        loadImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         switch (requestCode){
             case READIMAGE:
                 if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                     loadImage();
                 }else{
                     Toast.makeText(this,"Cannot access your images" , Toast.LENGTH_LONG).show();
                 }
                 break;
             default:
                 super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                 break;
         }
    }

    private void loadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if(task.isSuccessful()){
            Toast.makeText(this,"Successfully Login" , Toast.LENGTH_LONG).show();
            if(mAuth!=null){
                FirebaseUser currentUser = mAuth.getCurrentUser();
                saveImageInCos(currentUser);
            }
        }else{
            Toast.makeText(this,"Fail to login" , Toast.LENGTH_LONG).show();
        }
    }
}