package de.htw.berlin.steganography.strategy;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.htw.berlin.steganography.Constants;
import de.htw.berlin.steganography.MainActivity;
import de.htw.berlin.steganography.R;
import de.htw.berlin.steganography.ViewConstants;
import de.htw.berlin.steganography.auth.BasicAuthInterceptor;
import de.htw.berlin.steganography.auth.Information;
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

    @Override
    public View.OnClickListener authorize(Context context, TextView infoText, View retrieveAuthTokenBtn, View retrieveAccessTokenBtn) {
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                retrieveAuthTokenBtn.setClickable(false);
                retrieveAuthTokenBtn.setAlpha(.2f);

                Dialog auth_dialog = new Dialog(context);
                auth_dialog.setContentView(R.layout.auth_dialog);

                WebView web = auth_dialog.findViewById(R.id.webv);
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

                        if (url.contains("?code=") || url.contains("&code=")) {
                            Uri uri = Uri.parse(url);
                            Log.i("MYY", "Code: " + uri.getQueryParameter("code"));
                            String authCode = uri.getQueryParameter("code");
                            SharedPreferences pref = context.getSharedPreferences(Constants.SHARKSYS_PREF, context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString(Constants.TOKEN_STORAGE, authCode);
                            editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, System.currentTimeMillis());
                            editor.apply();

                            Log.i("MYY", "New Token: " + pref.getString(Constants.TOKEN_STORAGE, ""));

                            auth_dialog.dismiss();


                            retrieveAuthTokenBtn.setOnClickListener(token(context, infoText));

                            //Activate accesstoken Button & deactivate authtoken button
                            retrieveAccessTokenBtn.setClickable(true);
                            retrieveAccessTokenBtn.setAlpha(1.0f);
                        } else if (url.contains("error=access_denied")) {
                            Log.i("MYY", "Error, access denied.");
                            SharedPreferences pref = context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString(Constants.TOKEN_STORAGE, Constants.NO_RESULT);
                            editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
                            editor.apply();
                            auth_dialog.dismiss();
                            retrieveAuthTokenBtn.setClickable(true);
                            retrieveAuthTokenBtn.setAlpha(1.0f);
                        }
                    }
                });

                auth_dialog.show();
                auth_dialog.setTitle("Authorize");
                auth_dialog.setCancelable(true);
            }
        };
    }

    @Override
    public View.OnClickListener token(Context context, TextView infoText) {
        return v -> {
            v.setAlpha(0.2f);
            v.setClickable(false);

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(Constants.CLIENT_ID, Constants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", Constants.GRANT_TYPE_AUTH_CODE)
                    .add("code", context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE).getString(Constants.TOKEN_STORAGE, ""))
                    .add("redirect_uri", getInformation().getRedirectUri())
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(Constants.CLIENT_ID, Constants.CLIENT_SECRET))
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

                    SharedPreferences pref = context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    if (resp.contains("error")) {
                        Log.i("MYY", "Error in response: " + resp);
                        editor.putString(Constants.REFRESH_TOKEN_STORAGE, Constants.NO_RESULT);
                        editor.putLong(Constants.REFRESH_TOKEN_STORAGE_TIMESTAMP, -1);
                        editor.putString(Constants.ACCESS_TOKEN_STORAGE, Constants.NO_RESULT);
                        editor.putLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, -1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            Log.i("MYY","json resp: " + json);
                            editor.putString(Constants.REFRESH_TOKEN_STORAGE, (String)json.get("refresh_token"));
                            editor.putLong(Constants.REFRESH_TOKEN_STORAGE_TIMESTAMP, System.currentTimeMillis());
                            editor.putString(Constants.ACCESS_TOKEN_STORAGE, (String)json.get("access_token"));
                            editor.putLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, System.currentTimeMillis());
                            infoText.setText("Access grandet.");
                        } catch (JSONException e) {
                            //Set auth token to null/empty because it can only be used one time.
                            //If any error occures, it must be deleted.
                            editor.putString(Constants.TOKEN_STORAGE, "");
                            editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
                            e.printStackTrace();
                            infoText.setText("Access denied.");
                            v.setAlpha(1.0f);
                            v.setClickable(true);
                        }
                    }
                    editor.commit();
                }
            });
        };
    }

    @Override
    public View.OnClickListener refresh(Context context) {
        return v -> {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(Constants.CLIENT_ID, Constants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", Constants.GRANT_TYPE_REFRESH)
                    .add("refresh_token", context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE).getString(Constants.REFRESH_TOKEN_STORAGE, ""))
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(Constants.CLIENT_ID, Constants.CLIENT_SECRET))
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

                    SharedPreferences pref = context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    if (resp.contains("error")) {
                        Log.i("MYY", "Error in response: " + resp);
                        editor.putString(Constants.ACCESS_TOKEN_STORAGE, Constants.NO_RESULT);
                        editor.putLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, -1);
                    } else if (resp.contains("access_token")) {
                        try {
                            JSONObject json = new JSONObject(resp);
                            editor.putString(Constants.ACCESS_TOKEN_STORAGE, (String)json.get("access_token"));
                            editor.putLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, System.currentTimeMillis());
                        } catch (JSONException e) {
                            //Set auth token to null/empty because it can only be used one time.
                            //If any error occures, it must be deleted.
                            editor.putString(Constants.TOKEN_STORAGE, "");
                            editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
                            e.printStackTrace();
                        }
                    }
                    editor.commit();
                }
            });
        };
    }
}
