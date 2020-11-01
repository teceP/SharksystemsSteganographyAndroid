package de.htw.berlin.steganography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import apis.SocialMedia;
import apis.Token;
import apis.imgur.Imgur;
import apis.reddit.Reddit;
import de.htw.berlin.steganography.auth.constants.ImgurConstants;
import de.htw.berlin.steganography.auth.constants.RedditConstants;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.InformationHolder;
import de.htw.berlin.steganography.auth.models.TokenInformation;
import de.htw.berlin.steganography.auth.strategy.AuthStrategy;
import de.htw.berlin.steganography.auth.strategy.AuthStrategyFactory;
import de.htw.berlin.steganography.auth.tasks.RefreshTokenAsyncTask;
import de.htw.berlin.steganography.auth.tasks.TaskParcel;

public class MainActivity extends AppCompatActivity {

    /**
     * A list of all networks, which has tokens and a valid timestamp.
     * Note: Token could be expired.
     */
    private List<SocialMedia> networks;

    private HashMap<String, TokenInformation> authInformationPerNetwork;
    private Spinner spinner;
    private AuthStrategy authStrategy;
    private InformationHolder informationHolder;
    int authStatus;
    AuthInformation authInformation;
    Button retrieveAuthTokenBtn, retrieveAccessTokenBtn, refreshTokenBtn, updateStateBtn;
    ListView activeNetworksList;
    SharedPreferences pref;
    TextView infoText;
    String chosenNetwork;

    private List<RefreshTokenAsyncTask> refreshTokenAsyncTasks;
    private ScheduledExecutorService scheduledExecutorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.networks = new ArrayList<>();
        this.authInformationPerNetwork = new HashMap();
        this.refreshTokenAsyncTasks = new ArrayList<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(5);

        this.pref = getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);

        /**
         * UI Elements
         */
        this.infoText = findViewById(R.id.infoText);
        this.infoText.setOnClickListener(this.infoTextOnClickListener);

        this.setSpinner();
        this.informationHolder = new InformationHolder();
        this.loadSpinnerInformations();

        //Update token state for chosen network
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
        this.restoreSocialMedias();
        this.authStatus = Constants.STATUS_UNCHECKED;
        this.setButtonStates();

        this.activeNetworksList = findViewById(R.id.activeNetworksList);
        this.activeNetworksList.setVisibility(View.GONE);
    }

    public void restoreSocialMedias(){
        List<String> networks = new ArrayList<>();
        networks.add(Constants.REDDIT_TOKEN_OBJ);
        networks.add(Constants.IMGUR_TOKEN_OBJ);
        networks.add(Constants.TWITTER_TOKEN_OBJ);
        networks.add(Constants.YOUTUBE_TOKEN_OBJ);
        networks.add(Constants.INSTAGRAM_TOKEN_OBJ);

        for(String network : networks){
            TokenInformation tokenInformation = getAuthInformation(network);

            if(tokenInformation != null && hasValidToken(tokenInformation)){
                switch(tokenInformation.getNetwork()){
                    case "reddit":
                        this.authInformationPerNetwork.put(tokenInformation.getNetwork(), tokenInformation);
                        SocialMedia reddit = new Reddit();
                        reddit.setToken(new Token(tokenInformation.getAccessToken(), tokenInformation.getAccessTokenTimestamp()));
                        this.networks.add(reddit);
                        break;
                    case "imgur":
                        this.authInformationPerNetwork.put(tokenInformation.getNetwork(), tokenInformation);
                        SocialMedia imgur = new Imgur();
                        imgur.setToken(new Token(tokenInformation.getAccessToken(), tokenInformation.getAccessTokenTimestamp()));
                        this.networks.add(imgur);
                        break;
                    case "instagram":
                        //TODO
                        break;
                    case "twitter":
                        //TODO
                        break;
                    case "youtube":
                        //TODO
                        break;
                }
            }
        }
    }

    /**
     * Checks if there is a token in the TokenInformation object and if
     * @param tokenInformation
     * @return
     */
    private boolean hasValidToken(TokenInformation tokenInformation){
        if(tokenInformation.getAccessToken() != null && tokenInformation.getAccessToken().equals(Constants.NO_RESULT) || tokenInformation.getAccessTokenTimestamp() == -1){
            return false;
        }
        return true;
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
     *
     * @return AuthInformation for current (on spinner) selected item.
     */
    public TokenInformation getCurrentSelectedAuthInformation(){
        String selectedOnSpinner = spinner.getSelectedItem().toString().trim().toLowerCase();
        TokenInformation tokenInformation = getAuthInformation(selectedOnSpinner + Constants.TOKEN_OBJ_SUFFIX);

        if(tokenInformation != null){
            return tokenInformation;
        }

        return new TokenInformation(selectedOnSpinner);
    }

    /**
     *
     * @param network: MUST be an Constant like Constants.REDDIT_TOKEN_OBJ
     * @return null, if there are no information stored for this network.
     * @return AuthInformation object
     */
    public TokenInformation getAuthInformation(String network){
        String json = pref.getString(network, Constants.NO_RESULT);

        if(!json.equals(Constants.NO_RESULT)){
            return new Gson().fromJson(json, TokenInformation.class);
        }

        return null;
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
        TokenInformation tokenInformation = getCurrentSelectedAuthInformation();
        long token = tokenInformation.getTokenTimestamp();
        long accessToken = tokenInformation.getAccessTokenTimestamp();
        String accessTokenString = tokenInformation.getAccessToken();

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

        double minsLeft = Constants.ONE_HOUR_IN_MINS - (this.getTimeDifferent(tokenInformation.getAccessTokenTimestamp()));
        this.infoText.setText("Access token is valid.\nTime till retrieval: " + minsLeft + " minutes.");
        Log.i("MYY", "Access Token: " + accessTokenString);

        //this.refreshTokenAutomatically(tokenInformation.getNetwork());

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

    public void refreshTokenAutomatically(String network){
        TokenInformation tokenInformation = getCurrentSelectedAuthInformation();
        long tillExpiration = (long) this.getTimeDifferent(tokenInformation.getAccessTokenTimestamp());
        TaskParcel taskParcel = new TaskParcel(this, infoText, authStrategy, tokenInformation);

        RefreshTokenAsyncTask refreshTokenAsyncTask = new RefreshTokenAsyncTask();
        this.refreshTokenAsyncTasks.add(refreshTokenAsyncTask);

        //scheduledExecutorService.scheduleAtFixedRate(refreshTokenAsyncTask, 0, tillExpiration, TimeUnit.MILLISECONDS);
    }

    public void setInformations(){
        String chosenNetwork = spinner.getSelectedItem().toString().toLowerCase();
        authInformation = informationHolder.get(chosenNetwork);
        Log.i("MYY", "Choosen Network: " + chosenNetwork);
    }

    /**
     * Other Click Events
     */
    private View.OnClickListener updateStateOnClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            setInformations();
            authStrategy = AuthStrategyFactory.getAuthStrategy(authInformation);
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

    private AdapterView.OnItemSelectedListener spinnerOnItemClick() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenNetwork = parent.getSelectedItem().toString();
                Log.i("MYY", "Selected spinner item: " + chosenNetwork);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        };
    }

    /**
     * Adapters
     */


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
        spinner.setOnItemSelectedListener(spinnerOnItemClick());
    }

    private void loadSpinnerInformations() {
        AuthInformation redditInfo = new AuthInformation.Builder()
                .withPlatform("reddit")
                .withAuthUrl(RedditConstants.AUTH_URI)
                .withTokenUrl(RedditConstants.TOKEN_URI)
                .withClientId(RedditConstants.CLIENT_ID)
                .withClientSecret(RedditConstants.CLIENT_SECRET)
                .withDuration(RedditConstants.DURATION_PERM)
                .withRedirectUri(RedditConstants.REDIRECT)
                .withScope(RedditConstants.SCOPE)
                .withGrantType(RedditConstants.GRANT_TYPE_AUTH_CODE)
                .withResponseType(RedditConstants.RESPONSE_TYPE)
                .withState(UUID.randomUUID().toString())
                .build();
        this.informationHolder.put("reddit", redditInfo);

        AuthInformation imgurInfo = new AuthInformation().Builder()
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

        AuthInformation twitterInfo = new AuthInformation();
        //TODO
        this.informationHolder.put("twitter", twitterInfo);

        AuthInformation youtubeInfo = new AuthInformation();
        //TODO
        this.informationHolder.put("youtube", youtubeInfo);

        AuthInformation instagramInfo = new AuthInformation();
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
            resetTokensForSelectedNetwork();
            infoText.setText("Tokens for network " + spinner.getSelectedItem().toString() + " were reset.");
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetTokensForSelectedNetwork(){
        TokenInformation tokenInformation = getCurrentSelectedAuthInformation();
        tokenInformation = new TokenInformation(tokenInformation.getNetwork());

        Gson gson = new Gson();
        String json = gson.toJson(tokenInformation);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(spinner.getSelectedItem().toString().trim().toLowerCase() + Constants.TOKEN_OBJ_SUFFIX, json);
        editor.apply();
    }

    /*
    **********************************************************************************
    ** Provide SocialMediaSteganography Objects here: ********************************
    **********************************************************************************
     */

    public List<SocialMedia> provideActiveSocial(){
        return this.networks;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.scheduledExecutorService.shutdown();
    }
}