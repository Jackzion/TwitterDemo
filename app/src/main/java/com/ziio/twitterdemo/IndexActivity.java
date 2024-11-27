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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferManager;
import com.ziio.twitterdemo.config.CosConfig;
import com.ziio.twitterdemo.config.GoogleConfig;
import com.ziio.twitterdemo.cos.CosClient;
import com.ziio.twitterdemo.model.PostInfo;
import com.ziio.twitterdemo.model.Ticket;
import com.ziio.twitterdemo.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class IndexActivity extends AppCompatActivity {

    private List<Ticket> ticketList;
    private ListView lvTweets;
    private MyTweetAdapter adapter;

    private String myEmail;
    private String userUID;

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
        userUID = bundle.getString("uid");

        // init cosClient
        cosClient = new CosClient(this);
        // init fireBase
        firebaseDatabase = FirebaseDatabase.getInstance(GoogleConfig.REALTIME_DATABASE_URL);
        myRef = firebaseDatabase.getReference();
        // dummy data
        ticketList.add(new Ticket("0" , "him" , "url" , "add"));
        ticketList.add(new Ticket("0" , "him" , "url" , "ads"));

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
        LoadPost();
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
                EditText etPost = myView.findViewById(R.id.etPost);
                ImageView ivAttach = myView.findViewById(R.id.iv_attach);
                ivAttach.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // load the image
                        loadImage();
                    }
                });
                ImageView ivPost = myView.findViewById(R.id.iv_post);
                ivPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // upload the postInfo to database
                        PostInfo postInfo = new PostInfo(userUID,etPost.getText().toString(),pictureURL);
                        myRef.child("posts").push().setValue(postInfo);
                    }
                });
                return myView;
            }
            else if(myTweet.getTweetPersonUID().equals("loading")){
                View myView = LayoutInflater.from(context).inflate(R.layout.loading_ticket, null);
                return myView;
            }
            else if(myTweet.getTweetPersonUID().equals("ads")){
                View myView = LayoutInflater.from(context).inflate(R.layout.ads_ticket, null);
                AdView mAdView = myView.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
                return myView;
            }
            else{
                View myView = LayoutInflater.from(context).inflate(R.layout.tweets_ticket, null);
                // load tweet ticket
                TextView tweetText = myView.findViewById(R.id.txt_tweet);
                ImageView tweetImageView = myView.findViewById(R.id.tweet_picture);
                tweetText.setText(myTweet.getTweetText());
                Picasso.with(context).load(myTweet.getTweetImageURL()).into(tweetImageView);
                // load userInfo
                ImageView tweet_profilePic = myView.findViewById(R.id.tweet_profilePic);
                TextView txtUserName = myView.findViewById(R.id.txtUserName);
                myRef.child("Users").child(myTweet.getTweetPersonUID())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // update the list
                                HashMap<String , Object> map = (HashMap<String , Object>)dataSnapshot.getValue();
                                for(String key : map.keySet()){
                                    String value = (String)map.get(key);
                                    if(key.equals("profileImage")){
                                        Picasso.with(context).load(value).into(tweet_profilePic);
                                    }else{
                                        txtUserName.setText(value);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
        // loading 。。
        ticketList.add(0,new Ticket("0","him","url","loading"));
        adapter.notifyDataSetChanged();
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
                ticketList.remove(0);
                // run on UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"success to upload",Toast.LENGTH_LONG);
                        // 更新 UI
                        adapter.notifyDataSetChanged();
                    }
                });
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
                ticketList.remove(0);
                // run on UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"fail to upload" , Toast.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    // load postInfos from dataBase
    private void LoadPost(){
        myRef.child("posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            // clear the list
                            ticketList.clear();
                            ticketList.add(new Ticket("0","him","url","add"));
                            ticketList.add(new Ticket("0","him","url","ads"));
                            // fetch the new postList
                            HashMap<String , Object> map = (HashMap<String , Object>)dataSnapshot.getValue();
                            for(String key : map.keySet()){
                                // get the postInfo
                                HashMap<String , String> postInfoMap = (HashMap<String , String>)map.get(key);
                                String text = postInfoMap.get("text");
                                ticketList.add(new Ticket(key,postInfoMap.get("text") , postInfoMap.get("postImage") , postInfoMap.get("userUID")));
                            }
                            // update the View
                            adapter.notifyDataSetChanged();
                        }catch (Exception exception){

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}