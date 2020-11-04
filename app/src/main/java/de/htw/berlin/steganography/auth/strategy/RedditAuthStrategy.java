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

import de.htw.berlin.steganography.auth.constants.Constants;
import de.htw.berlin.steganography.MainActivity;
import de.htw.berlin.steganography.R;
import de.htw.berlin.steganography.auth.BasicAuthInterceptor;
import de.htw.berlin.steganography.auth.constants.RedditConstants;
import de.htw.berlin.steganography.auth.models.TokenInformation;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RedditAuthStrategy extends BasicAbstractAuthStrategy {

    public RedditAuthStrategy(AuthInformation authInformation) {
        super(authInformation);
    }

    @Override
    public View.OnClickListener authorize() {
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Button oauthBtn = MainActivity.getMainActivityInstance().findViewById(R.id.auth);
                TextView infoText = MainActivity.getMainActivityInstance().findViewById(R.id.infoText);

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
                        + "&duration=" + getAuthInformation().getDuration()
                        + "&state=" + getAuthInformation().getState()
                        + "&scope=" + getAuthInformation().getScope()
                        + "&redirect_uri=" + getAuthInformation().getRedirectUri();

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
                        TokenInformation tokenInformation = getTokenInformation(MainActivity.getMainActivityInstance(), "reddit");
                        boolean granted = false;
                        if (url.contains("?code=") || url.contains("&code=")) {
                            Uri uri = Uri.parse(url);
                            String authCode = uri.getQueryParameter("code");

                            tokenInformation.setToken(authCode);
                            tokenInformation.setTokenTimestamp(System.currentTimeMillis());

                            Log.i("MYY", "New Token: " + tokenInformation.getToken());

                            authDialog.dismiss();
                            infoText.setText("Auth token granted. Access token will be retrieved now.");
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
            TokenInformation tokenInformation = getTokenInformation(MainActivity.getMainActivityInstance(), "reddit");

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new BasicAuthInterceptor(RedditConstants.CLIENT_ID, RedditConstants.CLIENT_SECRET))
                    .build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", RedditConstants.GRANT_TYPE_AUTH_CODE)
                    .add("code", tokenInformation.getToken())
                    .add("redirect_uri", getAuthInformation().getRedirectUri())
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
            MainActivity.getMainActivityInstance().updateState();
            MainActivity.getMainActivityInstance().addAutoRefreshTimer(Constants.ONE_HOUR_IN_MS);

        };
    }

    @Override
    public View.OnClickListener refresh() {
        return v -> {
            TokenInformation tokenInformation = getTokenInformation(MainActivity.getMainActivityInstance(), "reddit");
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(RedditConstants.CLIENT_ID, RedditConstants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", RedditConstants.GRANT_TYPE_REFRESH)
                    .add("refresh_token", tokenInformation.getRefreshToken())
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
                        tokenInformation.setAccessToken(Constants.NO_RESULT);
                        tokenInformation.setAccessTokenTimestamp(-1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            tokenInformation.setAccessToken((String)json.get("access_token"));
                            tokenInformation.setAccessTokenTimestamp(System.currentTimeMillis());
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
            MainActivity.getMainActivityInstance().updateState();
            MainActivity.getMainActivityInstance().addAutoRefreshTimer(Constants.ONE_HOUR_IN_MS);
        };
    }
}
