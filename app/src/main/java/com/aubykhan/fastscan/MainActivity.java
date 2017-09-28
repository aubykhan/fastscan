package com.aubykhan.fastscan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private ImageView idImage;
    private ImageView selfieImage;

    private String mIdFilePath;
    private String mSelfieFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selfieImage = (ImageView) findViewById(R.id.selfieImage);
        idImage = (ImageView) findViewById(R.id.idImage);

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.scanButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImages();
            }
        });

        selfieImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(REQUEST_SELFIE_CAPTURE);
            }
        });

        idImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(REQUEST_ID_CAPTURE);
            }
        });
    }

    private void uploadImages() {
        String API_BASE_URL = "http://faceoffdemoapi.azurewebsites.net/api/";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit =
                builder
                        .client(
                                httpClient.build()
                        )
                        .build();

        ApiClient client = retrofit.create(ApiClient.class);

        Log.d("Info", "Uploading files...");
        Call<ApiResponse> call =
                client.uploadForRecognition(Arrays.asList(createPart(mIdFilePath), createPart(mSelfieFilePath)));

        // Execute the call asynchronously. Get a positive or negative callback.
        call.enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                if (response.isSuccessful()) {
                    if (response.body().getIsIdentical()) {
                        alertBuilder.setMessage("The profile is matched with confidence " + response.body().getConfidence());
                    } else {
                        alertBuilder.setMessage("The profile is not matched. " + response.message());
                    }
                } else {
                    alertBuilder.setMessage("Unable to process the request. " + response.message());
                    Log.d("Request", call.request().toString());
                    Log.d("Response", response.toString());
                }
                alertBuilder.show();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage("Request failed with error. " + t.getMessage());
                alertBuilder.show();
            }
        });
    }

    private MultipartBody.Part createPart(String path) {
        File file = new File(path);

        Bitmap bmp = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray());
        return MultipartBody.Part.createFormData("files", file.getName(), body);
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
            selfieImage.setImageBitmap(getBitmap(data));
        } else if (requestCode == REQUEST_ID_CAPTURE && resultCode == RESULT_OK) {
            idImage.setImageBitmap(getBitmap(data));
        }
    }

    private Bitmap getBitmap(Intent data) {
        Bundle extras = data.getExtras();
        return (Bitmap) extras.get("data");
    }
}
