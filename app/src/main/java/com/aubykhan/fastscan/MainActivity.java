package com.aubykhan.fastscan;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_SELFIE_CAPTURE = 1;
    static final int REQUEST_ID_CAPTURE = 2;

    private String mIdFilePath;
    private String mSelfieFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.startButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCaptureStep(
                    "Step 1",
                    "You'll be redirected to camera preview. Please make sure that your CNIC is properly visible on the screen.",
                    REQUEST_ID_CAPTURE
                );
            }
        });
    }

    private void startCaptureStep(String dialogTitle, String dialogMessage, final int requestType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dispatchTakePictureIntent(requestType);
                    }
                })
                .show();
    }

    private File createImageFile(int captureType) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        if (captureType == REQUEST_ID_CAPTURE) {
            mIdFilePath = image.getAbsolutePath();
        }
        else if (captureType == REQUEST_SELFIE_CAPTURE) {
            mSelfieFilePath = image.getAbsolutePath();
        }

        return image;
    }


    private void dispatchTakePictureIntent(int captureType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(captureType);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.aubykhan.fastscan",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, captureType);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELFIE_CAPTURE && resultCode == RESULT_OK) {
            startUploadActivity();
        } else if (requestCode == REQUEST_ID_CAPTURE && resultCode == RESULT_OK) {
            startCaptureStep(
                "Step 2",
                "Great! Now, take a selfie.",
                REQUEST_SELFIE_CAPTURE
            );
        }
    }

    private void startUploadActivity() {
        Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
        intent.putExtra("id_path", mIdFilePath);
        intent.putExtra("selfie_path", mSelfieFilePath);

        startActivity(intent);
    }

    private Bitmap getBitmap(Intent data) {
        Bundle extras = data.getExtras();
        return (Bitmap) extras.get("data");
    }
}
