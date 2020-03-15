package com.github.rudder.client;


import com.github.rudder.shared.http.InvocationClient;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CoordinatorClient extends InvocationClient {

    /**
     * Initiate communication between client and server
     *
     * @param port client's HTTP app port
     * @return application object id
     */
    @POST("/hello")
    Call<String> hello(@Query("port") int port);

}
