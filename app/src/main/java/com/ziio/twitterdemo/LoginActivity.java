package com.ziio.twitterdemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private ImageView ivImagePerson;

    private FirebaseAuth mAuth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        ivImagePerson = findViewById(R.id.ivImagePerson);

        ivImagePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo : select image from phone
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

    private void loginToFireBase(String email ,String password){
        if(mAuth!=null){
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"Successfully Login" , Toast.LENGTH_LONG).show();
                                if(mAuth!=null){
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),"Fail to login" , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

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
        // todo:load image
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    void buLogin(View view){
        loginToFireBase();
    }
}