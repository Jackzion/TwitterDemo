package com.ziio.twitterdemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferManager;
import com.ziio.twitterdemo.config.CosConfig;
import com.ziio.twitterdemo.cos.CosClient;
import com.ziio.twitterdemo.model.Ticket;
import com.ziio.twitterdemo.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IndexActivity extends AppCompatActivity {

    private List<Ticket> ticketList;
    private ListView lvTweets;
    private MyTweetAdapter adapter;
    private String myEmail;

    private CosClient cosClient;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        lvTweets = findViewById(R.id.lvTweets);
        ticketList = new ArrayList<Ticket>();
        // bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        myEmail = bundle.getString("email");
        // init cosClient
        cosClient = new CosClient(this);
        // dummy data
        ticketList.add(new Ticket("0" , "him" , "url" , "add"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ziio"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ziio"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ziio"));
        // set listView Adapter
        adapter = new MyTweetAdapter(ticketList,this);
        lvTweets.setAdapter(adapter);
        // selectImage ResultCallBack
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
                    // upload the attach image
                    uploadImage(BitmapFactory.decodeFile(picturePath));
                }
            }
        });

    }

    class MyTweetAdapter extends BaseAdapter {

        private List<Ticket> listNotesAdapter = new ArrayList<Ticket>();

        private Context context;

        public MyTweetAdapter(List listNotesAdpater, Context context) {
            this.listNotesAdapter = listNotesAdpater;
            this.context = context;
        }

        @Override
        public int getCount() {
            return listNotesAdapter.size();
        }

        @Override
        public Object getItem(int i) {
            return listNotesAdapter.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Ticket myTweet = listNotesAdapter.get(i);
            if(myTweet.getTweetPersonUID().equals("add")){
                View myView = LayoutInflater.from(context).inflate(R.layout.add_ticket, null);
                // load add ticket
                ImageView ivAttach = findViewById(R.id.iv_attach);
                ivAttach.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // load the image
                        loadImage();
                    }
                });
                ImageView ivPost = findViewById(R.id.iv_post);
                ivPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // upload the postInfo

                    }
                });
                return myView;
            }else{
                View myView = LayoutInflater.from(context).inflate(R.layout.tweets_ticket, null);
                // todo : work
                // load tweet ticket
                return myView;
            }
        }
    }
    private void loadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    private String pictureURL;
    private void uploadImage(Bitmap bitmap) {
        // 生成目标路径
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyHHmmss");
        String rootPath = "imagePost";
        String emailId = StringUtil.splitEmail(myEmail);
        Date date = new Date();
        String imagePath =simpleDateFormat.format(date) + ".jpg";
        String cosPath = rootPath + File.separator + emailId + File.separator + imagePath;

        // 获取 bitmap 图片字节流
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
                // public the pictureURL
                pictureURL = result.accessUrl;
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
}