package com.github.rudder.shared.http;

import com.github.rudder.shared.http.api.MethodArguments;
import com.github.rudder.shared.http.api.MethodCallResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface InvocationClient {

    /**
     * Invoke method on the other side
     *
     * @param objectId   id of object whose method will be called
     * @param methodName name of the method
     * @return method call result
     */
    @POST("/invoke")
    Call<MethodCallResult> invoke(@Query("objectId") String objectId, @Query("methodName") String methodName);


    /**
     * Invoke method on the other side with arguments
     *
     * @param objectId   id of object whose method will be called
     * @param methodName name of the method
     * @param arguments  method call arguments
     * @return method call result
     */
    @POST("/invoke")
    Call<MethodCallResult> invoke(@Query("objectId") String objectId,
                                  @Query("methodName") String methodName,
                                  @Body MethodArguments arguments);
}
