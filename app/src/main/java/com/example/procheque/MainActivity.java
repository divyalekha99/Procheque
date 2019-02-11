package com.example.procheque;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    private StorageReference mStorageRef, StorageRef;
    Uri selectedImage;
    UploadTask uploadTask;
    private static final int CAMERA_REQUEST = 1888;
    Button upload;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 &&  resultCode == RESULT_OK && data !=null)
        {
            selectedImage = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                ImageView imageView =(ImageView) findViewById(R.id.image);
                imageView.setImageBitmap(bitmap);

                progressDialog = new ProgressDialog(this);
                progressDialog.setMax(100);
                progressDialog.setMessage("Uploading...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
                progressDialog.setCancelable(false);

                StorageReference filepath = mStorageRef.child("Photos").child(selectedImage.getLastPathSegment());

                uploadTask =filepath.putFile(selectedImage);


                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //sets and increments value of progressbar
                        progressDialog.incrementProgressBy((int) progress);

                    }
                });


                 uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                //Uri downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                Task<Uri> u = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                Toast.makeText(MainActivity.this,"Upload successful",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Toast.makeText(MainActivity.this,"Error in uploading!",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
            }
            catch (IOException e)
            {
                e.getStackTrace();
            }

        }
    }
    public void getPhoto(){
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
        //Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        FirebaseApp.initializeApp(this);
        //StorageRef = mStorageRef.child("images/"+ selectedImage.getLastPathSegment());



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==1)
        {
            if(grantResults.length >0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                getPhoto();

            }
        }

    }
    public void takeImageFromCamera(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1 setContentView(R.layout.activity_main);

        setContentView(R.layout.start);
        upload = (Button) findViewById(R.id.upload);


        mStorageRef = FirebaseStorage.getInstance().getReference();


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                else{
                    getPhoto();
                }

            }
        });
    }
}
