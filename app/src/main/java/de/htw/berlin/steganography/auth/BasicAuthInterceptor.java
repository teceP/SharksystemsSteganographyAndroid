package de.htw.berlin.steganography.auth;

import android.util.Log;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

/**
 * @author Mario Teklic
 */

/**
 * Basic Authentication Interceptor which is applied before a request goes out to a provider
 */
public class BasicAuthInterceptor implements Interceptor {

    /**
     * Username and password, combined as a Bearer token String
     */
    private String credentials;

    public BasicAuthInterceptor(String user, String password){
        this.credentials = Credentials.basic(user, password);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build();

        long t1 = System.nanoTime();
        Log.d("MYY-OkHttp", String.format("Sending request %s on %s%n%s",
                authenticatedRequest.url(), chain.connection(), authenticatedRequest.headers()));
        Buffer buffer = new Buffer();
        request.body().writeTo(buffer);

        Log.i("MYY", "Body: " + buffer.readUtf8());

        Response response = chain.proceed(authenticatedRequest);

        long t2 = System.nanoTime();
        Log.d("MYY-OkHttp", String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}
