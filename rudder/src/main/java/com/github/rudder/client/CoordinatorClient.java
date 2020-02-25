package com.github.rudder.client;


import com.github.rudder.shared.InvocationClient;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CoordinatorClient extends InvocationClient {

    @POST("/hello")
    Call<String> hello(@Query("port") int port);

}
