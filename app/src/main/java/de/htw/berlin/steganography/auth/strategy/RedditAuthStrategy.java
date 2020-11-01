package de.htw.berlin.steganography.auth.strategy;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.htw.berlin.steganography.Constants;
import de.htw.berlin.steganography.R;
import de.htw.berlin.steganography.auth.BasicAuthInterceptor;
import de.htw.berlin.steganography.auth.constants.RedditConstants;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.models.Information;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RedditAuthStrategy extends BasicAbstractAuthStrategy {

    public RedditAuthStrategy(Information information) {
        super(information);
    }

    /**
     * Restores AuthInformation objects from shared preferences.
     * @param context
     *
     * @return the restored objects or in case the JSON was empty, an fresh objects.
     */
    public AuthInformation getAuthInformation(Context context){
        String json = context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE)
                .getString(Constants.REDDIT_TOKEN_OBJ, Constants.NO_RESULT);

        if(!json.equals(Constants.NO_RESULT)){
            return new Gson().fromJson(json, AuthInformation.class);
        }

        return new AuthInformation("reddit");
    }

    public void applyAuthInformation(Context context, AuthInformation authInformation){
        Gson gson = new Gson();
        String json = gson.toJson(authInformation);
        context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE)
                .edit().putString(Constants.REDDIT_TOKEN_OBJ, json)
                .apply();
    }

    @Override
    public View.OnClickListener authorize(Context context, TextView infoText, View retrieveAuthTokenBtn, View retrieveAccessTokenBtn) {
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                retrieveAuthTokenBtn.setClickable(false);
                retrieveAuthTokenBtn.setAlpha(.2f);

                Dialog authDialog = new Dialog(context);
                authDialog.setContentView(R.layout.auth_dialog);

                WebView web = authDialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);

                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();

                String url = getInformation().getAuthUrl()
                        + "client_id=" + getInformation().getClientId()
                        + "&response_type=" + getInformation().getResponseType()
                        + "&duration=" + getInformation().getDuration()
                        + "&state=" + getInformation().getState()
                        + "&scope=" + getInformation().getScope()
                        + "&redirect_uri=" + getInformation().getRedirectUri();

                Log.i("MYY", url);

                web.loadUrl(url);
                web.setWebViewClient(new WebViewClient() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        AuthInformation authInformation = getAuthInformation(context);

                        if (url.contains("?code=") || url.contains("&code=")) {
                            Uri uri = Uri.parse(url);
                            String authCode = uri.getQueryParameter("code");

                            authInformation.setToken(authCode);
                            authInformation.setTokenTimestamp(System.currentTimeMillis());

                            Log.i("MYY", "New Token: " + authInformation.getToken());

                            authDialog.dismiss();
                            infoText.setText("Auth token granted. Get your access token now.");

                            retrieveAccessTokenBtn.setClickable(true);
                            retrieveAccessTokenBtn.setAlpha(1.0f);
                        } else if (url.contains("error=access_denied")) {
                            Log.i("MYY", "Error, access denied.");

                            authInformation.setToken(Constants.NO_RESULT);
                            authInformation.setTokenTimestamp(-1);

                            authDialog.dismiss();
                            retrieveAuthTokenBtn.setClickable(true);
                            retrieveAuthTokenBtn.setAlpha(1.0f);

                            infoText.setText("Auth token was not granted. Check your credentials or try later again.");
                        }
                        applyAuthInformation(context, authInformation);
                    }
                });

                authDialog.show();
                authDialog.setTitle("Authorize");
                authDialog.setCancelable(true);
            }
        };
    }

    @Override
    public View.OnClickListener token(Context context, TextView infoText) {
        return v -> {
            v.setAlpha(0.2f);
            v.setClickable(false);

            AuthInformation authInformation = getAuthInformation(context);

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(RedditConstants.CLIENT_ID, RedditConstants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", RedditConstants.GRANT_TYPE_AUTH_CODE)
                    .add("code", authInformation.getToken())
                    .add("redirect_uri", getInformation().getRedirectUri())
                    .build();

            Request request = new Request.Builder()
                    .url(RedditConstants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(RedditConstants.CLIENT_ID, RedditConstants.CLIENT_SECRET))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.i("MYY", "failure ");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response r) throws IOException {
                    String resp = r.body().string();
                    Log.i("MYY", "Return Code: " + r.code() + " - body: " + resp + "\nheaders: " + r.headers());

                    if (resp.contains("error")) {
                        Log.i("MYY", "Error in response: " + resp);
                        authInformation.setRefreshToken(Constants.NO_RESULT);
                        authInformation.setTokenTimestamp(-1);
                        authInformation.setAccessToken(Constants.NO_RESULT);
                        authInformation.setAccessTokenTimestamp(-1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            Log.i("MYY","json resp: " + json);
                            long ts = System.currentTimeMillis();
                            authInformation.setRefreshToken( (String)json.get("refresh_token"));
                            authInformation.setTokenTimestamp(ts);
                            authInformation.setAccessToken((String)json.get("access_token"));
                            authInformation.setAccessTokenTimestamp(ts);
                            infoText.setText("Access grandet.");
                            v.setAlpha(.2f);
                            v.setClickable(false);
                        } catch (JSONException e) {
                            //Set auth token to null/empty because it can only be used one time.
                            //If any error occures, it must be deleted.
                            authInformation.setToken(Constants.NO_RESULT);
                            authInformation.setTokenTimestamp(System.currentTimeMillis());
                            e.printStackTrace();
                            infoText.setText("Access denied.");
                            v.setAlpha(1.0f);
                            v.setClickable(true);
                        }
                    }
                    applyAuthInformation(context, authInformation);
                }
            });
        };
    }

    @Override
    public View.OnClickListener refresh(Context context) {
        return v -> {
            AuthInformation authInformation = getAuthInformation(context);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(RedditConstants.CLIENT_ID, RedditConstants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", RedditConstants.GRANT_TYPE_REFRESH)
                    .add("refresh_token", authInformation.getRefreshToken())
                    .build();

            Request request = new Request.Builder()
                    .url(RedditConstants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(RedditConstants.CLIENT_ID, RedditConstants.CLIENT_SECRET))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.i("MYY", "failure ");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response r) throws IOException {
                    String resp = r.body().string();
                    Log.i("MYY", "Return Code: " + r.code() + " - body: " + resp + "\nheaders: " + r.headers());

                    if (resp.contains("error")) {
                        Log.i("MYY", "Error in response: " + resp);
                        authInformation.setAccessToken(Constants.NO_RESULT);
                        authInformation.setAccessTokenTimestamp(-1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            authInformation.setAccessToken((String)json.get("access_token"));
                            authInformation.setAccessTokenTimestamp(System.currentTimeMillis());
                        } catch (JSONException e) {
                            //Set auth token to null/empty because it can only be used one time.
                            //If any error occures, it must be deleted.
                            authInformation.setToken(Constants.NO_RESULT);
                            authInformation.setTokenTimestamp(-1);
                            e.printStackTrace();
                        }
                    }
                    applyAuthInformation(context, authInformation);
                }
            });
        };
    }
}
