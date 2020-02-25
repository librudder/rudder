package com.github.rudder.shared;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface InvocationClient {

    @POST("/invoke")
    Call<MethodCallResult> invoke(@Query("objectId") String objectId, @Query("methodName") String methodName);


    @POST("/invoke")
    Call<MethodCallResult> invoke(@Query("objectId") String objectId,
                                  @Query("methodName") String methodName,
                                  @Body MethodArguments arguments);
}
