package com.aubykhan.fastscan;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by aubykhan on 9/27/17.
 */

// http://faceoffdemoapi.azurewebsites.net/
public interface ApiClient {
    @Multipart
    @POST("Recognition")
    Call<ApiResponse> uploadForRecognition(
            @Part List<MultipartBody.Part> files
    );
}
