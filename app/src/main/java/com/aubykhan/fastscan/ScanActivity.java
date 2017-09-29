package com.aubykhan.fastscan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScanActivity extends AppCompatActivity {

    private ProgressBar mProgress;
    private TextView mProgressText;
    private ConstraintLayout mResultContainer;
    private TextView mCnicText;
    private TextView mNameText;
    private TextView mConfidenceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgressText = (TextView) findViewById(R.id.progressText);
        mResultContainer = (ConstraintLayout) findViewById(R.id.scanInfoContainer);
        mCnicText = (TextView) findViewById(R.id.cnicText);
        mNameText = (TextView) findViewById(R.id.nameText);
        mConfidenceText = (TextView) findViewById(R.id.confidenceText);

        mResultContainer.setVisibility(View.INVISIBLE);

        String idFilePath = getIntent().getStringExtra("id_path");
        String selfieFilePath = getIntent().getStringExtra("selfie_path");

        ImageView idImageView = (ImageView)findViewById(R.id.cnicImageView);
        ImageView selfieImageView = (ImageView)findViewById(R.id.selfieImageView);

        idImageView.setImageBitmap(getThumbnail(idFilePath));
        selfieImageView.setImageBitmap(getThumbnail(selfieFilePath));

        uploadImages(idFilePath, selfieFilePath);
    }

    private Bitmap getThumbnail(String key) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(key), 128, 128);
    }

    private void uploadImages(String idFilePath, String selfieFilePath) {
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
                client.uploadForRecognition(Arrays.asList(createPart(idFilePath), createPart(selfieFilePath)));

        // Execute the call asynchronously. Get a positive or negative callback.
        call.enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                hideProgress();
                showSuccessResult(response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                hideProgress();
                showFailureResult(t);
            }
        });
    }

    private void hideProgress() {
        mProgress.setVisibility(View.INVISIBLE);
        mProgressText.setVisibility(View.INVISIBLE);
    }

    private void showFailureResult(@NonNull Throwable t) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Request failed with error. " + t.getMessage());
        alertBuilder.show();
    }

    private void showSuccessResult(@NonNull Response<ApiResponse> response) {
        mResultContainer.setVisibility(View.VISIBLE);

        mCnicText.setText("CNIC: " + response.body().getCnic());
        mNameText.setText("Name: " + response.body().getName());
        mConfidenceText.setText("Confidence: " + response.body().getConfidence() * 100 + "%");
    }

    private MultipartBody.Part createPart(String path) {
        File file = new File(path);

        Bitmap bmp = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray());
        return MultipartBody.Part.createFormData("files", file.getName(), body);
    }
}
