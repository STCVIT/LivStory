package com.yashkasera.livstory.retrofit;

import com.yashkasera.livstory.model.RequestModel;
import com.yashkasera.livstory.model.ResponseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {
    @POST("https://livstory4.azurewebsites.net/")
    Call<ResponseModel> getSound(@Body RequestModel requestModel);
}
