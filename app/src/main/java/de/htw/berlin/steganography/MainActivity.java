package de.htw.berlin.steganography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
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
import de.htw.berlin.steganography.strategy.AuthStrategy;
import de.htw.berlin.steganography.strategy.AuthStrategyFactory;
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
    private AuthStrategy authStrategy;
    private InformationHolder informationHolder;
    int authStatus;
    Information information;
    WebView web;
    Button retrieveAuthTokenBtn, retrieveAccessTokenBtn, refreshTokenBtn, updateStateBtn;
    SharedPreferences pref;
    Dialog auth_dialog;
    String authCode;
    TextView infoText;

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
        this.loadSpinnerInformations();

        this.updateStateBtn = findViewById(R.id.updateStateBtn);
        this.updateStateBtn.setOnClickListener(this.updateStateOnClick);

        //Auth token
        this.retrieveAuthTokenBtn = findViewById(R.id.auth);

        //Access token
        this.retrieveAccessTokenBtn = findViewById(R.id.retrieveAccessTokenBtn);

        //Refresh token
        this.refreshTokenBtn = findViewById(R.id.retrieveRefreshTokenBtn);

        /**
         * Initial token state call
         */
        this.authStatus = Constants.STATUS_UNCHECKED;
        this.setButtonStates();
    }

    private void setButtonStates(){
        if (this.authStatus == Constants.T_AT_NOT_EXPIRED || this.authStatus == Constants.STATUS_UNCHECKED) {
            //No button clickable
            retrieveAuthTokenBtn.setClickable(false);
            retrieveAuthTokenBtn.setAlpha(.2f);

            retrieveAccessTokenBtn.setClickable(false);
            retrieveAccessTokenBtn.setAlpha(.2f);

            refreshTokenBtn.setClickable(false);
            refreshTokenBtn.setAlpha(.2f);
        } else if (this.authStatus == Constants.T_EXPIRED) {
            //Only retrieve new auth token button is clickable
            //When finished, onClick-Event will make retrieve new access token clickable
            retrieveAuthTokenBtn.setClickable(true);
            retrieveAuthTokenBtn.setAlpha(1.0f);

            retrieveAccessTokenBtn.setClickable(false);
            retrieveAccessTokenBtn.setAlpha(.2f);

            refreshTokenBtn.setClickable(false);
            refreshTokenBtn.setAlpha(.2f);
        } else if (this.authStatus == Constants.AT_NEVER_RETRIEVED) {
            //Only retrieve new access token is clickable
            retrieveAuthTokenBtn.setClickable(false);
            retrieveAuthTokenBtn.setAlpha(.2f);

            retrieveAccessTokenBtn.setClickable(true);
            retrieveAccessTokenBtn.setAlpha(1.0f);

            refreshTokenBtn.setClickable(false);
            refreshTokenBtn.setAlpha(.2f);
        } else if (this.authStatus == Constants.AT_NEEDS_REFRESH) {
            //All buttons are not clickable. Tokens are valid
            retrieveAuthTokenBtn.setClickable(false);
            retrieveAuthTokenBtn.setAlpha(.2f);

            retrieveAccessTokenBtn.setClickable(false);
            retrieveAccessTokenBtn.setAlpha(.2f);

            refreshTokenBtn.setClickable(true);
            refreshTokenBtn.setAlpha(1.0f);
        }
    }

    /**
     * Returns different to now in minutes.
     * @param l
     * @return
     */
    public double getTimeDifferent(long l) {
        return (double) (((System.currentTimeMillis() - l) / 1000) / 60);
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
        SharedPreferences pref = MainActivity.this.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);
        long token = pref.getLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
        long accessToken = pref.getLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, -1);
        String accessTokenString = pref.getString(Constants.ACCESS_TOKEN_STORAGE, "");

        //Check if Access Token is expired.
        if (this.tokenExpired(accessToken)) {
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
        Log.i("MYY", "Access Token: " + accessTokenString);
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
        Log.i("MYY", "Choosen Network: " + chosenNetwork);
    }

    /**
     * Other Click Events
     */
    private View.OnClickListener updateStateOnClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            setInformations();
            authStrategy = AuthStrategyFactory.getAuthStrategy(information);
            authStatus = checkTokenExpiration();
            retrieveAuthTokenBtn.setOnClickListener(authStrategy.authorize(MainActivity.this, infoText, retrieveAuthTokenBtn, retrieveAccessTokenBtn));
            retrieveAccessTokenBtn.setOnClickListener(authStrategy.token(MainActivity.this, infoText));
            refreshTokenBtn.setOnClickListener(authStrategy.refresh(MainActivity.this));
            setButtonStates();
        }
    };

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

    private void loadSpinnerInformations() {
        Information redditInfo = new Information.Builder()
                .withPlatform("reddit")
                .withAuthUrl(Constants.AUTH_URI)
                .withTokenUrl(Constants.TOKEN_URI)
                .withClientId(Constants.CLIENT_ID)
                .withClientSecret(Constants.CLIENT_SECRET)
                .withDuration(Constants.DURATION_PERM)
                .withRedirectUri(Constants.REDIRECT)
                .withScope(Constants.SCOPE)
                .withGrantType(Constants.GRANT_TYPE_AUTH_CODE)
                .withResponseType(Constants.RESPONSE_TYPE)
                .withState(UUID.randomUUID().toString())
                .build();
        this.informationHolder.put("reddit", redditInfo);

        Information imgurInfo = new Information().Builder()
                .withPlatform("imgur")
                .withAuthUrl(ImgurConstants.AUTH_URI)
                .withTokenUrl(ImgurConstants.TOKEN_URI)
                .withClientId(ImgurConstants.CLIENT_ID)
                .withClientSecret(ImgurConstants.CLIENT_SECRET)
                .withDuration(ImgurConstants.DURATION_PERM)
                .withRedirectUri(ImgurConstants.REDIRECT)
                .withScope(ImgurConstants.SCOPE)
                .withGrantType(ImgurConstants.GRANT_TYPE_AUTH_CODE)
                .withResponseType(ImgurConstants.RESPONSE_TYPE)
                .withState(UUID.randomUUID().toString())
                .build();
        this.informationHolder.put("imgur", imgurInfo);

        Information twitterInfo = new Information();
        //TODO
        this.informationHolder.put("twitter", twitterInfo);

        Information youtubeInfo = new Information();
        //TODO
        this.informationHolder.put("youtube", youtubeInfo);

        Information instagramInfo = new Information();
        //TODO
        this.informationHolder.put("instagram", instagramInfo);
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
        } else if(id == R.id.action_reset){
            resetAllTokens();
            infoText.setText("All tokens were reset.");
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetAllTokens(){
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(Constants.TOKEN_STORAGE_TIMESTAMP, -1);
        editor.putString(Constants.TOKEN_STORAGE, "").apply();
        editor.putLong(Constants.ACCESS_TOKEN_STORAGE_TIMESTAMP, -1);
        editor.putString(Constants.ACCESS_TOKEN_STORAGE, "");
        editor.putLong(Constants.REFRESH_TOKEN_STORAGE_TIMESTAMP, -1);
        editor.putString(Constants.REFRESH_TOKEN_STORAGE, "");
        editor.apply();
    }
}