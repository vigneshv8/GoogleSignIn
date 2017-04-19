package com.example.vvign.googlesignin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener{
    private LinearLayout Prof_Section,Upload_prom,Upload_sec,Uploaded_file;
    private Button SignOut,Upload,Choose;
    private SignInButton SignIn;
    private TextView Name,Email;
    private ImageView Prof_Pic,Image;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE=9001,IMG_PICK=9002;
    private Uri filePath;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storageReference= FirebaseStorage.getInstance().getReference();
        Prof_Section=(LinearLayout) findViewById(R.id.prof_section);
        Upload_prom=(LinearLayout) findViewById(R.id.upload_prompt);
        Upload_sec=(LinearLayout) findViewById(R.id.upload_section);
        Uploaded_file=(LinearLayout) findViewById(R.id.uploaded_file);
        Upload_prom.setVisibility(View.GONE);
        Upload_sec.setVisibility(View.GONE);
        Uploaded_file.setVisibility(View.GONE);
        SignOut=(Button) findViewById(R.id.bn_logout);
        SignIn=(SignInButton) findViewById(R.id.bn_login);
        Upload=(Button) findViewById(R.id.bn_upload);
        Choose=(Button) findViewById(R.id.bn_choose);
        Name=(TextView) findViewById(R.id.name);
        Email=(TextView) findViewById(R.id.email);
        Prof_Pic=(ImageView) findViewById(R.id.prof_pic);
        Image=(ImageView) findViewById(R.id.image);
        SignIn.setOnClickListener(this);
        SignOut.setOnClickListener(this);
        Upload.setOnClickListener(this);
        Choose.setOnClickListener(this);
        Prof_Section.setVisibility(View.GONE);
        GoogleSignInOptions signInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bn_login:
                signIn();
                //Toast.makeText(MainActivity.this,"signing-in",Toast.LENGTH_SHORT).show();
                break;
            case R.id.bn_logout:
                signOut();
                break;
            case R.id.bn_choose:
                fileChooser();
                break;
            case R.id.bn_upload:
                fileUpload();
                break;


        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    private void signIn()
    {
        Intent intent=Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);
        //Toast.makeText(MainActivity.this,"sign in function",Toast.LENGTH_SHORT).show();
    }
    private void fileChooser()
    {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"select an image"),IMG_PICK);

    }
    private void fileUpload()
    {
        if(filePath != null) {
            final ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference riversRef = storageReference.child("images/profile.jpg");


            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"file uploaded",Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress=(100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage(((int) progress) + "% uploaded...");
                        }
                    });
            ;
        }
        else {
            Toast.makeText(getApplicationContext(),"no file selected",Toast.LENGTH_LONG).show();
        }
    }

    private void signOut()
    {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(false);
            }
        });}
    private void handleResult(GoogleSignInResult result)
    {
        if(result.isSuccess())
        {
            //Toast.makeText(MainActivity.this,"result success",Toast.LENGTH_SHORT).show();
            GoogleSignInAccount account=result.getSignInAccount();
            String name=account.getDisplayName();
            String email=account.getEmail();
            String img_url= account.getPhotoUrl().toString();
            Name.setText(name);
            Email.setText(email);
            Glide.with(this).load(img_url).into(Prof_Pic);
            updateUI(true);
    }
        else {
            //Toast.makeText(MainActivity.this,"result not success",Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
        }
    private void updateUI(boolean isLogin)
    {
        if(isLogin)
        {
            Prof_Section.setVisibility(View.VISIBLE);
            Upload_prom.setVisibility(View.VISIBLE);
            Upload_sec.setVisibility(View.VISIBLE);
            Uploaded_file.setVisibility(View.VISIBLE);
            SignIn.setVisibility(View.GONE);
        }
        else
        {
            Upload_prom.setVisibility(View.GONE);
            Upload_sec.setVisibility(View.GONE);
            Uploaded_file.setVisibility(View.GONE);
            Prof_Section.setVisibility(View.GONE);
            SignIn.setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_CODE)
        {
            //Toast.makeText(MainActivity.this,"sign in function",Toast.LENGTH_SHORT).show();
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
        if(requestCode==IMG_PICK && resultCode == RESULT_OK && data != null && data.getData() !=null)
        {
            filePath=data.getData();
            try {
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                Image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //else
        //{
            //Toast.makeText(MainActivity.this,"error",Toast.LENGTH_SHORT).show();
        //}
    }
}
