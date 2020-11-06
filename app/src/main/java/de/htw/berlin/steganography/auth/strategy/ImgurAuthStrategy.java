package de.htw.berlin.steganography.auth.strategy;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.htw.berlin.steganography.MainActivity;
import de.htw.berlin.steganography.auth.constants.Constants;
import de.htw.berlin.steganography.R;
import de.htw.berlin.steganography.auth.BasicAuthInterceptor;
import de.htw.berlin.steganography.auth.constants.ImgurConstants;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.models.NetworkName;
import de.htw.berlin.steganography.auth.models.TokenInformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgurAuthStrategy extends BasicAbstractAuthStrategy {

    public ImgurAuthStrategy(AuthInformation authInformation){
        super(authInformation);
    }

    @Override
    public View.OnClickListener authorize() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView infoText = MainActivity.getMainActivityInstance().findViewById(R.id.infoText);
                Button oauthBtn = MainActivity.getMainActivityInstance().findViewById(R.id.auth);

                oauthBtn.setClickable(false);
                oauthBtn.setAlpha(.2f);

                Dialog authDialog = new Dialog(MainActivity.getMainActivityInstance());
                authDialog.setContentView(R.layout.auth_dialog);

                WebView web = authDialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);

                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();

                String url = getAuthInformation().getAuthUrl()
                        + "client_id=" + getAuthInformation().getClientId()
                        + "&response_type=" + getAuthInformation().getResponseType()
                        + "&state=" + getAuthInformation().getState();

                Log.i("MYY", url);

                web.loadUrl(url);
                web.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        TokenInformation tokenInformation = getTokenInformation(MainActivity.getMainActivityInstance(), "imgur");

                        Log.i("MYY", "URL: " + url);
                        boolean granted = false;

                        if (url.contains("?code=") || url.contains("&code=")) {
                            Uri uri = Uri.parse(url);
                            String authCode = uri.getQueryParameter("code");

                            tokenInformation.setToken(authCode);
                            tokenInformation.setTokenTimestamp(System.currentTimeMillis());

                            Log.i("MYY", "New Token: " + tokenInformation.getToken());

                            authDialog.dismiss();
                            infoText.setText("Auth token granted. Get your access token now.");
                            granted = true;
                        } else if (url.contains("error=access_denied")) {
                            Log.i("MYY", "Error, access denied.");

                            tokenInformation.setToken(Constants.NO_RESULT);
                            tokenInformation.setTokenTimestamp(-1);

                            authDialog.dismiss();
                            infoText.setText("Auth token was not granted. Check your credentials or try later again.");
                        }
                        applyTokenInformation(MainActivity.getMainActivityInstance(), tokenInformation);
                        if(granted){
                            Button b = MainActivity.getMainActivityInstance().findViewById(R.id.dummyBtn);
                            b.setOnClickListener(token());
                            b.callOnClick();
                        }else{
                            oauthBtn.setClickable(true);
                            oauthBtn.setAlpha(1.0f);
                        }
                    }
                });

                MainActivity.getMainActivityInstance().updateState();
                authDialog.show();
                authDialog.setTitle("Authorize");
                authDialog.setCancelable(true);
            }
        };
    }

    @Override
    public View.OnClickListener token() {
        return v -> {
            TextView infoText = MainActivity.getMainActivityInstance().findViewById(R.id.infoText);
            v.setAlpha(0.2f);
            v.setClickable(false);

            TokenInformation tokenInformation = getTokenInformation(MainActivity.getMainActivityInstance(), "imgur");

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(ImgurConstants.CLIENT_ID, ImgurConstants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("client_id", ImgurConstants.CLIENT_ID)
                    .add("client_secret", ImgurConstants.CLIENT_SECRET)
                    .add("grant_type", ImgurConstants.GRANT_TYPE_AUTH_CODE)
                    .add("code", tokenInformation.getToken())
                    .add("redirect_uri", getAuthInformation().getRedirectUri())
                    .build();

            Request request = new Request.Builder()
                    .url(ImgurConstants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(ImgurConstants.CLIENT_ID, ImgurConstants.CLIENT_SECRET))
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
                        tokenInformation.setRefreshToken(Constants.NO_RESULT);
                        tokenInformation.setTokenTimestamp(-1);
                        tokenInformation.setAccessToken(Constants.NO_RESULT);
                        tokenInformation.setAccessTokenTimestamp(-1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            Log.i("MYY","json resp: " + json);
                            long ts = System.currentTimeMillis();
                            tokenInformation.setRefreshToken( (String)json.get("refresh_token"));
                            tokenInformation.setTokenTimestamp(ts);
                            tokenInformation.setAccessToken((String)json.get("access_token"));
                            tokenInformation.setAccessTokenTimestamp(ts);
                            infoText.setText("Access grandet.");
                        } catch (JSONException e) {
                            //Set auth token to null/empty because it can only be used one time.
                            //If any error occures, it must be deleted.
                            tokenInformation.setToken(Constants.NO_RESULT);
                            tokenInformation.setTokenTimestamp(System.currentTimeMillis());
                            e.printStackTrace();
                            infoText.setText("Access denied.");
                            Button oauthBtn = MainActivity.getMainActivityInstance().findViewById(R.id.auth);
                            oauthBtn.setClickable(true);
                            oauthBtn.setAlpha(1.0f);
                        }
                    }
                    applyTokenInformation(MainActivity.getMainActivityInstance(), tokenInformation);
                }
            });
            Log.i("MYY", "Update UI now");
            MainActivity.getMainActivityInstance().updateUI();
            MainActivity.getMainActivityInstance().addAutoRefreshTimer(NetworkName.IMGUR, Constants.ONE_HOUR_IN_MS);
        };
    }

    @Override
    public View.OnClickListener refresh() {
        return v -> {
            TokenInformation tokenInformation = getTokenInformation(MainActivity.getMainActivityInstance(), "imgur");
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(ImgurConstants.CLIENT_ID, ImgurConstants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("client_id", ImgurConstants.CLIENT_ID)
                    .add("client_secret", ImgurConstants.CLIENT_SECRET)
                    .add("grant_type", ImgurConstants.GRANT_TYPE_REFRESH)
                    .add("refresh_token", tokenInformation.getRefreshToken())
                    .build();

            Request request = new Request.Builder()
                    .url(ImgurConstants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(ImgurConstants.CLIENT_ID, ImgurConstants.CLIENT_SECRET))
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
                        tokenInformation.setAccessToken(Constants.NO_RESULT);
                        tokenInformation.setAccessTokenTimestamp(-1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            Log.i("MYY", "new access token: " + json.getString("access_token"));
                            tokenInformation.setAccessToken((String)json.get("access_token"));
                            tokenInformation.setAccessTokenTimestamp(System.currentTimeMillis());
                            tokenInformation.setRefreshToken((String)json.get("refresh_token"));
                            tokenInformation.setRefreshTokenTimestamp(System.currentTimeMillis());
                        } catch (JSONException e) {
                            //Set auth token to null/empty because it can only be used one time.
                            //If any error occures, it must be deleted.
                            tokenInformation.setToken(Constants.NO_RESULT);
                            tokenInformation.setTokenTimestamp(-1);
                            TextView infoText = MainActivity.getMainActivityInstance().findViewById(R.id.infoText);
                            infoText.setText("Refreshing failed.");
                            Log.i("MYY", "Error during refreshing: " + e.getMessage());
                            Button oauthBtn = MainActivity.getMainActivityInstance().findViewById(R.id.auth);
                            oauthBtn.setClickable(true);
                            oauthBtn.setAlpha(1.0f);
                        }
                    }
                    applyTokenInformation(MainActivity.getMainActivityInstance(), tokenInformation);
                }
            });
            MainActivity.getMainActivityInstance().updateUI();
            MainActivity.getMainActivityInstance().addAutoRefreshTimer(NetworkName.IMGUR, Constants.ONE_HOUR_IN_MS);
        };
    }
}
