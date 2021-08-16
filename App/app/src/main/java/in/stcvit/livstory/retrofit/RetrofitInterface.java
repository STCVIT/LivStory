package in.stcvit.livstory.retrofit;

import in.stcvit.livstory.modal.ListResponseModel;
import in.stcvit.livstory.modal.RequestModel;
import in.stcvit.livstory.modal.SoundResponseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @POST("/")
    Call<SoundResponseModel> getSound(@Body RequestModel requestModel);

    @POST("/text")
    Call<ListResponseModel> getList(@Body RequestModel requestModel);

    @POST("/report")
    Call<Object> postWord(@Body RequestModel requestModel);
}
