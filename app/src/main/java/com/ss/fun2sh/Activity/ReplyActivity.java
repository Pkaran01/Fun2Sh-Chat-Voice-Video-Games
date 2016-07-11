package com.ss.fun2sh.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.fun2sh.CRUD.M;
import com.ss.fun2sh.R;
import com.ss.fun2sh.oldutils.ConnectionDetector;
import com.ss.fun2sh.oldutils.Constants;
import com.ss.fun2sh.oldutils.PostTask;
import com.ss.fun2sh.oldutils.WebserviceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class ReplyActivity extends AppCompatActivity implements OnClickListener,WebserviceCallback{
    TextView arrowback,txt_reply_dept,txt_reply_sub,txt_reply_head,txt_reply_close,txt_reply_submit,txt_reply_attach;
    EditText ed_reply_msg;
    String reply_msg;
    Context con=ReplyActivity.this;
    Bundle b;
    String filePath = "";
    String img_name="";
    ProgressDialog progDialog;
//    long max_file_size=13364;
    long max_file_size=3000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        img_name="";

        arrowback = (TextView) findViewById(R.id.arrowback);
        txt_reply_dept= (TextView) findViewById(R.id.txt_reply_dept);
        txt_reply_sub= (TextView) findViewById(R.id.txt_reply_sub);
        txt_reply_head= (TextView) findViewById(R.id.txt_reply_head);
        txt_reply_submit= (TextView) findViewById(R.id.txt_reply_submit);
        txt_reply_close= (TextView) findViewById(R.id.txt_reply_close);
        txt_reply_attach= (TextView) findViewById(R.id.txt_reply_attach);


        ed_reply_msg= (EditText) findViewById(R.id.ed_reply_msg);

        arrowback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b=this.getIntent().getExtras();
        txt_reply_head.setText("Ticket # "+b.getString(Constants.TKTNO));
        txt_reply_dept.setText(b.getString(Constants.DEPTNAM));
        txt_reply_sub.setText(b.getString(Constants.SUBJECT));

        if(b.getString("sel_method").contains("open"))
        {
            txt_reply_close.setVisibility(View.VISIBLE);
        }
        else
        {
            txt_reply_close.setVisibility(View.INVISIBLE);
        }

        txt_reply_submit.setOnClickListener(this);
        txt_reply_close.setOnClickListener(this);
        txt_reply_attach.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.txt_reply_submit)
        {

            try {

                if(img_name.length()>4)
                {
                    uploadFileToServer(filePath);
                }
                else {

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TKTSLNO, b.getString(Constants.TKTSLNO));
                    jsonObject.put(Constants.MESSAGE, ed_reply_msg.getText().toString());
                    jsonObject.put(Constants.ATTNAME, "" + img_name);
                    jsonObject.put(Constants.TKTACTION, "Open");
                    jsonObject.put(Constants.SITE, "Fun2sh");

                    Log.d("ddd REPLY", b.getString(Constants.TKTSLNO));
                    Log.d("ddd", ed_reply_msg.getText().toString());
                    Log.d("ddd", "abc");
                    Log.d("ddd", "Open");
                    Log.d("ddd", "Fun2sh");

                    if (ConnectionDetector.isConnected(this)) {
                        PostTask postTask = new PostTask(ReplyActivity.this, Constants.HELP_REPLY, ReplyActivity.this);
                        postTask.execute(jsonObject);
                    } else {
                        M.T(this, Constants.NO_INTERNET);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(v.getId()==R.id.txt_reply_close)
        {
            if(img_name.length()>4)
            {
                uploadFileToServer(filePath);
            }
            else {

                try {
                    JSONObject jsonObject = new JSONObject();

                        jsonObject.put(Constants.TKTSLNO, b.getString(Constants.TKTSLNO));
                        jsonObject.put(Constants.MESSAGE, ed_reply_msg.getText().toString());
                        jsonObject.put(Constants.ATTNAME, "" + img_name);
                        jsonObject.put(Constants.TKTACTION, "Closed");
                        jsonObject.put(Constants.SITE, "Fun2sh");

                    Log.d("ddd REPLY", b.getString(Constants.TKTSLNO));
                    Log.d("ddd", ed_reply_msg.getText().toString());
                    Log.d("ddd", "abc");
                    Log.d("ddd", "Closed");
                    Log.d("ddd", "Fun2sh");

                    if (ConnectionDetector.isConnected(this)) {
                        PostTask postTask = new PostTask(ReplyActivity.this, Constants.HELP_REPLY, ReplyActivity.this);
                        postTask.execute(jsonObject);
                    } else {
                        M.T(this, Constants.NO_INTERNET);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(v.getId()==R.id.txt_reply_attach)
        {
//            M.T(con,"u clicked attach in Reply");

            String title = "Choose a picture";
            final String[] items = { "Gallery", "Camera" };
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    if (items[position].equals("Gallery")) {
                        choosePhoto();
                    } else if (items[position].equals("Camera")) {
                        takeAPhoto();
                    }
                }
            };
            showDialogWithItems(items, title, listener);
        }
    }

    @Override
    public void postResult(String postResult, String postMethod) {

        Log.d("ddd",postResult);
        try {
            JSONObject obj=new JSONObject(postResult);
            M.T(con,obj.get("MSG").toString());

            if(obj.get("MSG").toString().equalsIgnoreCase("SUCCESS"))
            {
                img_name="";
                this.finish();

            }

//            startActivity(new Intent(ReplyActivity.this,DashBoardActivity.class));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void choosePhoto() {
        Intent photoIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoIntent.setType("image/*");
        startActivityForResult(photoIntent, Constants.REQUEST_CHOOSE_PHOTO);
    }

    public void takeAPhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, Constants.REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == Constants.REQUEST_CHOOSE_PHOTO
                && resultCode == RESULT_OK && null != intent) {

            filePath = getSelectedPath(intent);
//            LLpath.setVisibility(View.VISIBLE);
//            photopath.setText(" :" + filePath);
//            uploadFileToServer(filePath);

            Log.d("ddd filepath",filePath);
//            M.T(con,"You selected Image to upload");


            File file=new File(filePath);
            long length = file.length();
            Log.d("ddd length",""+length);

            String[] array=filePath.split("/");
            img_name=array[array.length-1];

            if(length<max_file_size)
            {
                M.T(ReplyActivity.this,"You selected Image to upload");
                img_name=array[array.length-1];
            }
            else
            {
                M.T(ReplyActivity.this,"Selected file size is greater than 3MB,Please choose another file ");
                img_name="";
            }

//            long file_size= 0;
//            try {
//                FTPClient ftp=new FTPClient();
//                file_size = ftp.fileSize(filePath);
//                Log.d("file_size",""+file_size);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (FTPIllegalReplyException e) {
//                e.printStackTrace();
//            } catch (FTPException e) {
//                e.printStackTrace();
//            }




        } else if (requestCode == Constants.REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK && null != intent) {

            filePath = getCapturedImagePath(intent);
//            LLpath.setVisibility(View.VISIBLE);
//            photopath.setText(" :" + filePath);

//            uploadFileToServer(filePath);
            Log.d("ddd filepath",filePath);
            M.T(con,"You selected Image to upload");

            String[] array=filePath.split("/");
            Log.d("ddd len",""+array.length);
            for(int i=0;i<array.length;i++)
            {
                Log.d("ddd",array[i]);
            }
            img_name=array[array.length-1];
        }
    }

    private void uploadFileToServer(final String filePath) {

        showProgressDialog();

        new Thread(new Runnable() {

            @Override
            public void run() {

                uploadFile(filePath);
            }
        }).start();
    }

    public void uploadFile(String filePath) {

        FTPClient client = new FTPClient();



        try {

            client.connect(Constants.FTP_PHOTOHOST, 21);
            client.login(Constants.FTP_PHOTOUSER, Constants.FTP_PHOTOPASS);



            client.setType(FTPClient.TYPE_BINARY);

//            long file_size= 0;
//            try {
//                file_size = client.fileSize(filePath);
//                Log.d("file_size",""+file_size);
//                M.T(ReplyActivity.this,"filesize"+file_size);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (FTPIllegalReplyException e) {
//                e.printStackTrace();
//            } catch (FTPException e) {
//                e.printStackTrace();
//            }




            client.setCompressionEnabled(true);
            client.changeDirectory("/");
            client.upload(new File(filePath), new MyTransferListener(filePath));

        } catch (Exception e) {
            try {
//				Logger.log("ftp uploadFile catch1" + e);
                client.disconnect(true);
                callHandler(Constants.FILE_TRANS_FAILED, filePath);
            } catch (Exception e2) {
//				Logger.log("ftp uploadFile catch2" + e);
                e2.printStackTrace();
                callHandler(Constants.FILE_TRANS_FAILED, filePath);
            }
        }

    }// end of uploadFile



    public String getSelectedPath(Intent data) {

        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        // close cursor
        cursor.close();

        // create copy of image with timestamp
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File localFileStroageLocName = new File(
                Constants.LOCAL_FILE_STORAGE_PATH + File.separator + "IMAGE_"
                        + timeStamp + ".jpg");
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(
                    localFileStroageLocName);
            fileOutputStream.write(bytes.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localFileStroageLocName.getPath();
    }

    public String getCapturedImagePath(Intent intent) {
        Bundle bundle = intent.getExtras();
        Bitmap thumbnail = (Bitmap) bundle.get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File localFileStroageLocName = new File(
                Constants.LOCAL_FILE_STORAGE_PATH + File.separator + "IMAGE_"
                        + timeStamp + ".jpg");
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(
                    localFileStroageLocName);
            fileOutputStream.write(bytes.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localFileStroageLocName.getPath();
    }

    private void showProgressDialog() {
        progDialog = new ProgressDialog(ReplyActivity.this);
        progDialog.setMessage("Uploading...");
        progDialog.setIndeterminate(true);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setCancelable(false);
        progDialog.show();
    }

    private void dismissProgressDialog() {
        if ((progDialog != null) && progDialog.isShowing()) {
            try {
                progDialog.dismiss();
                progDialog = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void callHandler(String photoUploadResult, String filePath) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("filepath", filePath);
        bundle.putString("photoUploadResult", photoUploadResult);
        message.setData(bundle);
        ftpFileUploadHandler.sendMessage(message);
    }

    final Handler ftpFileUploadHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message handlerMsg) {

            Bundle bundle = handlerMsg.getData();
            String loacalMessagePath = bundle.getString("filepath");
            String photoUploadResult = bundle.getString("photoUploadResult");

            if (photoUploadResult.equals(Constants.FILE_TRANS_COMPLETE)) {

                // JSONObject jsonObject = new JSONObject();
                File file = new File(loacalMessagePath);

//                showToastMsg("File Uploaded");
                dismissProgressDialog();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.TKTSLNO, b.getString(Constants.TKTSLNO));
                    jsonObject.put(Constants.MESSAGE, ed_reply_msg.getText().toString());
                    jsonObject.put(Constants.ATTNAME, "" + img_name);
                    if(b.getString("sel_method").contains("open")) {
                        jsonObject.put(Constants.TKTACTION, "Open");
                    }
                    else
                    {
                        jsonObject.put(Constants.TKTACTION, "Closed");
                    }
                    jsonObject.put(Constants.SITE, "Fun2sh");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (ConnectionDetector.isConnected(ReplyActivity.this)) {
                    PostTask postTask = new PostTask(ReplyActivity.this, Constants.HELP_REPLY, ReplyActivity.this);
                    postTask.execute(jsonObject);
                } else {
                    M.T(ReplyActivity.this, Constants.NO_INTERNET);
                }
            } else {
                dismissProgressDialog();
                showToastMsg(Constants.PHOTO_UPLOAD_FAILED);

            }// end of handleMessage
        }
    };

    public void showToastMsg(String message) {
        Toast.makeText(ReplyActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private class MyTransferListener implements FTPDataTransferListener {

        String filePath;

        public MyTransferListener(String filePath) {
            this.filePath = filePath;

        }

        @Override
        public void started() {
//			Logger.log("ftp started called");
        }

        @Override
        public void transferred(int arg0) {
//			Logger.log("ftp transferred called");
        }

        @Override
        public void completed() {
//		Logger.log("ftp completed called");
            callHandler(Constants.FILE_TRANS_COMPLETE, filePath);
        }

        @Override
        public void aborted() {
//			Logger.log("ftp aborted called");
        }

        @Override
        public void failed() {
//			Logger.log("ftp failed called");
            callHandler(Constants.FILE_TRANS_FAILED, filePath);
        }// MyTransferListener
    }

    public void showDialogWithItems(String[] items, String title,
                                    DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReplyActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setItems(items, listener);
        alertDialog.show();
    }
}
