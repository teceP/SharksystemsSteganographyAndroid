package de.htw.berlin.steganography;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import apis.SocialMedia;
import de.htw.berlin.steganography.auth.BasicAuthInterceptor;
import de.htw.berlin.steganography.auth.Information;
import de.htw.berlin.steganography.auth.InformationHolder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private SocialMedia socialMedia;

    private Spinner spinner;

    private InformationHolder informationHolder;
    int authStatus;
    Information information;
    WebView web;
    Button oauthBtn;
    SharedPreferences pref;
    Dialog auth_dialog;
    String authCode;
    TextView infoText;

    /**
     * Returns different to now in minutes.
     * @param l
     * @return
     */
    public double getTimeDifferent(long l) {
        return (double) (((System.currentTimeMillis() - l) / 1000) / 60);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.pref = getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);

        /**
         * UI Elements
         */
        this.infoText = findViewById(R.id.infoText);
        this.infoText.setOnClickListener(this.infoTextOnClickListener);

        this.setSpinner();
        this.informationHolder = new InformationHolder();
        this.addSpinnerInformations(this.informationHolder);

        this.oauthBtn = findViewById(R.id.auth);

        Log.i("MYY", "test: "+ pref.getString(Constants.TOKEN_STORAGE, "no") + "\n" +pref.getLong(Constants.TOKEN_STORAGE_TIMESTAMP, -2));

        /**
         * Token Expiration
         */
        this.authStatus = -1;
        this.authStatus = this.checkTokenExpiration();

        if (this.authStatus == Constants.T_AT_NOT_EXPIRED) {
            oauthBtn.setClickable(false);
            Log.i("MYY", "Access token is valid.");
        } else if (this.authStatus == Constants.T_EXPIRED) {
            oauthBtn.setOnClickListener(this.tokenOnClick);
        } else if (this.authStatus == Constants.AT_NEVER_RETRIEVED) {
            oauthBtn.setOnClickListener(this.accessTokenOnClick);
        } else if (this.authStatus == Constants.AT_NEEDS_REFRESH) {
            oauthBtn.setOnClickListener(this.accessTokenOnClick);
        }else{
            Log.i("MYY", "Error: no click event was bind on button");
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Constants.AUTH_STATUS, this.authStatus);
        editor.apply();
    }

    /**
     * Tokens are normally valid for one Hour.
     * If a access token is expired, they can be refreshed.
     *
     * @return 0 == token and access token are not expired [T_AT_NOT_EXPIRED]
     * @return 1 == token is expired [T_EXPIRED]
     * @return 2 == access token was never retrieved [AT_NEVER_RETRIEVED]
     * @return 3 == access token is expired [AT_NEEDS_REFRESH]
     */
    private int checkTokenExpiration() {
        long token = pref.getLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
        long accessToken = pref.getLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, -1);

        //Check if Access Token is expired.
        if (this.tokenExpired(accessToken)) {

            String accessTokenString = pref.getString(Constants.ACCESS_TOKEN_STORAGE, "");
            Log.i("MYY", "accT:" + accessTokenString);
            //Check if access Token was ever retrieved
            if(accessTokenString.isEmpty()){
                //Check if Auth Token is expired. -> Clear cookies and get new token + timestamp
                if (this.tokenExpired(token)) {
                    this.infoText.setText("Auth token is expired.\nRetrieve new auth token.");
                    return Constants.T_EXPIRED;
                }else{
                    //Auth Token is not expired but access token was never retrieved.
                    //Get access token with grant_type with authorization_code
                    this.infoText.setText("Access token was never retrieved.\nRetrieve first access token.");
                    return Constants.AT_NEVER_RETRIEVED;
                }
            }else{
                //Access token is expired. Get new with grant_type refresh_token
                this.infoText.setText("Access token is expired.\nRefresh access token.");
                return Constants.AT_NEEDS_REFRESH;
            }
        }

        double minsLeft = Constants.ONE_HOUR_IN_MINS - (this.getTimeDifferent(pref.getLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, -1)));

        this.infoText.setText("Access token is valid.\nTime till retrieval: " + minsLeft + " minutes.");
        return Constants.T_AT_NOT_EXPIRED;
    }

    /**
     * Compares current millis, with timestamp of token.
     * Handles '-1' as expired.
     * If '-1' was provided, no timestamp was found.
     *
     * @param l
     * @return true if token is expired.
     */
    private boolean tokenExpired(long l) {
        if ((this.getTimeDifferent(l) > Constants.ONE_HOUR_IN_MINS) || l == -1) {
            return true;
        }
        return false;
    }

    public void setInformations(){
        String chosenNetwork = spinner.getSelectedItem().toString().toLowerCase();
        information = informationHolder.get(chosenNetwork);
        infoText.setText("Choosen Network: " + chosenNetwork);
    }


    /**
     * Token Click Events
     */

    /**
     * Will only be called, when there is no valid access token AND no token.
     */
    private View.OnClickListener tokenOnClick = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            setInformations();

            auth_dialog = new Dialog(MainActivity.this);
            auth_dialog.setContentView(R.layout.auth_dialog);

            web = auth_dialog.findViewById(R.id.webv);
            web.getSettings().setJavaScriptEnabled(true);

            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();

            String url = information.getAuthUrl()
                    + "client_id=" + information.getClientId()
                    + "&response_type=code"
                    + "&state=" + UUID.randomUUID().toString()
                    + "&redirect_uri=" + information.getRedirectUri()
                    + "&scope=" + information.getScope();

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
                        authCode = uri.getQueryParameter("code");

                        SharedPreferences pref = MainActivity.this.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(Constants.TOKEN_STORAGE, authCode);
                        editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, System.currentTimeMillis());
                        editor.apply();

                        Log.i("MYY", "New Token: " + pref.getString(Constants.TOKEN_STORAGE, ""));

                        auth_dialog.dismiss();
                        MainActivity.this.oauthBtn.setOnClickListener(MainActivity.this.accessTokenOnClick);
                        Log.i("MYY", "onclick access was set");
                    } else if (url.contains("error=access_denied")) {
                        Log.i("MYY", "Error, access denied.");
                        SharedPreferences pref = MainActivity.this.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(Constants.TOKEN_STORAGE, Constants.NO_RESULT);
                        editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
                        editor.apply();
                        auth_dialog.dismiss();
                    }
                }
            });

            auth_dialog.show();
            auth_dialog.setTitle("Authorize");
            auth_dialog.setCancelable(true);
        }
    };

    private View.OnClickListener accessTokenOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setInformations();

            if(pref.getInt(Constants.AUTH_STATUS, -1) == Constants.AT_NEEDS_REFRESH){
                information.setGrantType(Constants.GRANT_TYPE_REFRESH);
            }

            //else: grant_type == authorization_code by default

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BasicAuthInterceptor(Constants.CLIENT_ID, Constants.CLIENT_SECRET)).build();

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", information.getGrantType())
                    .add("code", pref.getString(Constants.TOKEN_STORAGE, ""))
                    .add("redirect_uri", information.getRedirectUri())
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.TOKEN_URI)
                    .addHeader("Authorization", Credentials.basic(Constants.CLIENT_ID, Constants.CLIENT_SECRET))
                    .post(body)
                    .build();

            Log.i("MMY", "Request body: ");


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.i("MYY", "failure ");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response r) throws IOException {
                    String resp = r.body().string();
                    Log.i("MYY", "Return Code: " + r.code() + " - body: " + resp);

                    SharedPreferences pref = MainActivity.this.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
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
        }
    };

    /**
     * Other Click Events
     */
    private View.OnClickListener infoTextOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("Copied", infoText.getText());
            clipboard.setPrimaryClip(data);
            Toast.makeText(getApplicationContext(), "Copied to Clipboard.", Toast.LENGTH_LONG).show();
        }
    };

    /**
     * UI Objects
     */

    private void setSpinner() {
        spinner = findViewById(R.id.spinner);
        String[] items = new String[]{"Reddit", "Imgur", "Twitter", "Instagram", "YouTube"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, items);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void addSpinnerInformations(InformationHolder informationHolder) {
        Information redditInfo = new Information.Builder()
                .withAuthUrl(Constants.AUTH_URI)
                .withTokenUrl(Constants.TOKEN_URI)
                .withClientId(Constants.CLIENT_ID)
                .withClientSecret(Constants.CLIENT_SECRET)
                .withDuration(Constants.DURATION_PERM)
                .withRedirectUri(Constants.REDIRECT)
                .withScope(Constants.SCOPE)
                .withGrantType(Constants.GRANT_TYPE_AUTH_CODE)
                .build();

        informationHolder.put("reddit", redditInfo);

        Information imgurInfo = new Information();
        informationHolder.put("imgur", imgurInfo);

        Information twitterInfo = new Information();
        informationHolder.put("twitter", twitterInfo);

        Information youtubeInfo = new Information();
        informationHolder.put("youtube", youtubeInfo);

        Information instagramInfo = new Information();
        informationHolder.put("instagram", instagramInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}