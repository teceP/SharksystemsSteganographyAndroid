package de.htw.berlin.steganography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import apis.SocialMedia;
import apis.Token;
import apis.imgur.Imgur;
import apis.reddit.Reddit;
import de.htw.berlin.steganography.adapters.NetworkListAdapter;
import de.htw.berlin.steganography.adapters.SimpleDividerItemDecoration;
import de.htw.berlin.steganography.auth.constants.Constants;
import de.htw.berlin.steganography.auth.constants.ImgurConstants;
import de.htw.berlin.steganography.auth.constants.RedditConstants;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.InformationHolder;
import de.htw.berlin.steganography.auth.models.NetworkParcel;
import de.htw.berlin.steganography.auth.models.Networks;
import de.htw.berlin.steganography.auth.models.TokenInformation;
import de.htw.berlin.steganography.auth.strategy.AuthStrategy;
import de.htw.berlin.steganography.auth.strategy.AuthStrategyFactory;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;

    /**
     * A list of all networks, which has tokens and a valid timestamp.
     * Note: Token could be expired.
     */
    private Map<Networks, NetworkParcel> parcelMap;
    private Map<String, SocialMedia> networks;
    private List<TokenInformation> tokenInformationPerNetwork;
    private Spinner spinner;
    AuthStrategy selectedAuthStrategy;
    Map<String, AuthStrategy> authStrategys;
    private InformationHolder authInformationHolder;
    Integer authStatus;
    AuthInformation authInformation;
    Button oauthBtn;
    Button refreshTokenBtn;
    ProgressBar progressPnl;

    RecyclerView networkRecyclerView;
    RecyclerView.Adapter networkRecyclerAdapter;
    RecyclerView.LayoutManager networkRecyclerLayoutManager;

    SharedPreferences pref;
    TextView infoText;
    String chosenNetwork;
    String latestRefreshedNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        this.networks = new HashMap<>();
        this.tokenInformationPerNetwork = new ArrayList<>();
        this.authStrategys = new HashMap<>();
        this.parcelMap = new HashMap<>();
        this.latestRefreshedNetwork = Constants.NO_RESULT;

        this.pref = getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);

        /**
         * UI Elements
         */
        this.infoText = findViewById(R.id.infoText);
        this.progressPnl = findViewById(R.id.progressPnl);

        this.setSpinner();
        this.authInformationHolder = new InformationHolder();
        this.loadSpinnerInformations();

        //OAuth2 Button
        this.oauthBtn = findViewById(R.id.auth);
        this.refreshTokenBtn = findViewById(R.id.refreshTokenBtn);
        this.refreshTokenBtn.setVisibility(View.GONE);

        /**
         * Initial token state
         */
        this.authStatus = new Integer(Constants.STATUS_UNCHECKED);

        /**
         * Recycler View
         */
        this.networkRecyclerView = findViewById(R.id.activeNetworksList);
        this.networkRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        this.networkRecyclerLayoutManager = new LinearLayoutManager(this);
        this.networkRecyclerAdapter = new NetworkListAdapter(this.tokenInformationPerNetwork);
        this.networkRecyclerView.setLayoutManager(networkRecyclerLayoutManager);
        this.networkRecyclerView.setAdapter(networkRecyclerAdapter);

        /**
         * Update state & UI
         */
        //this.updateState();
        this.restoreSocialMedias();
        this.updateUI();
    }

    public void updateSocialMediaToken(String network) {
        TokenInformation ti = getTokenInformation(network);
        this.networks.get(network).setToken(new Token(ti.getAccessToken(), ti.getAccessTokenTimestamp()));
    }

    /**
     * Restores AuthInformation objects from shared preferences.
     *
     * @return the restored objects or in case the JSON was empty, an fresh objects.
     */
    public TokenInformation getTokenInformation(String network) {
        String json = pref.getString(network + Constants.TOKEN_OBJ_SUFFIX, Constants.NO_RESULT);

        if (!json.equals(Constants.NO_RESULT)) {
            return new Gson().fromJson(json, TokenInformation.class);
        }

        return new TokenInformation(network);
    }

    public void restoreSocialMedias() {
        List<String> networks = new ArrayList<>();
        networks.add(Constants.REDDIT_TOKEN_OBJ);
        networks.add(Constants.IMGUR_TOKEN_OBJ);
        networks.add(Constants.TWITTER_TOKEN_OBJ);
        networks.add(Constants.YOUTUBE_TOKEN_OBJ);
        networks.add(Constants.INSTAGRAM_TOKEN_OBJ);

        for (String network : networks) {
            TokenInformation tokenInformation = getAuthInformation(network);

            if (tokenInformation == null) {
                tokenInformation = new TokenInformation(network.replace(Constants.TOKEN_OBJ_SUFFIX, ""));
            }

            switch (tokenInformation.getNetwork()) {
                case "reddit":
                    this.tokenInformationPerNetwork.add(tokenInformation);
                    SocialMedia reddit = new Reddit();
                    reddit.setToken(new Token(tokenInformation.getAccessToken(), tokenInformation.getAccessTokenTimestamp()));
                    this.networks.put(tokenInformation.getNetwork(), reddit);
                    break;
                case "imgur":
                    this.tokenInformationPerNetwork.add(tokenInformation);
                    SocialMedia imgur = new Imgur();
                    imgur.setToken(new Token(tokenInformation.getAccessToken(), tokenInformation.getAccessTokenTimestamp()));
                    this.networks.put(tokenInformation.getNetwork(), imgur);
                    break;
                case "instagram":
                    this.tokenInformationPerNetwork.add(tokenInformation);
                    /**
                     * TODO dein social media objekt initialisieren, token setzen und in die networks liste adden
                     */
                    break;
                case "twitter":
                    this.tokenInformationPerNetwork.add(tokenInformation);
                    /**
                     * TODO dein social media objekt initialisieren, token setzen und in die networks liste adden
                     */break;
                case "youtube":
                    this.tokenInformationPerNetwork.add(tokenInformation);
                    /**
                     * TODO dein social media objekt initialisieren, token setzen und in die networks liste adden
                     */break;
            }
        }
    }

    /**
     * Checks if there is a token in the TokenInformation object and if
     *
     * @param tokenInformation
     * @return
     */
    private boolean hasValidToken(TokenInformation tokenInformation) {
        if (tokenInformation.getAccessToken() != null && tokenInformation.getAccessToken().equals(Constants.NO_RESULT) || tokenInformation.getAccessTokenTimestamp() == -1) {
            return false;
        }
        return true;
    }

    public void setButtonStates() {
        if (this.authStatus == Constants.T_AT_NOT_EXPIRED || this.authStatus == Constants.STATUS_UNCHECKED) {
            //Button not clickable, refresh possible
            oauthBtn.setClickable(false);
            oauthBtn.setAlpha(.2f);

            refreshTokenBtn.setClickable(true);
            refreshTokenBtn.setAlpha(1.0f);
        } else if (this.authStatus == Constants.T_EXPIRED) {
            //Only retrieve new auth token button is clickable
            //When finished, onClick-Event will make retrieve new access token clickable
            oauthBtn.setClickable(true);
            oauthBtn.setAlpha(1.0f);

            refreshTokenBtn.setClickable(false);
            refreshTokenBtn.setAlpha(.2f);
        } else if (this.authStatus == Constants.AT_NEVER_RETRIEVED) {
            //Access token not retrieved. Call token()-method and change back to authorize
            oauthBtn.setClickable(false);
            oauthBtn.setAlpha(.2f);
            oauthBtn.setOnClickListener(selectedAuthStrategy.token());
            oauthBtn.callOnClick();
            this.authStatus = checkTokenExpiration();
            oauthBtn.setOnClickListener(selectedAuthStrategy.authorize());

            refreshTokenBtn.setClickable(false);
            refreshTokenBtn.setAlpha(.2f);
        } else if (this.authStatus == Constants.AT_NEEDS_REFRESH) {
            //Only refresh possible. Will be called automatically
            oauthBtn.setClickable(false);
            oauthBtn.setAlpha(.2f);
            refreshTokenBtn.callOnClick();
            this.authStatus = checkTokenExpiration();
        }
    }

    /**
     * Returns different to now in minutes.
     *
     * @param l
     * @return
     */
    public double getTimeDifferentInMin(long l) {
        return (double) (((System.currentTimeMillis() - l) / 1000) / 60);
    }

    /**
     * Computes how many ms left till expiration of access token.
     *
     * @param l
     * @return
     */
    public long timeLeftInMs(long l) {
        return Constants.ONE_HOUR_IN_MS - (System.currentTimeMillis() - l);
    }

    /**
     * @return AuthInformation for current (on spinner) selected item.
     */
    public TokenInformation getCurrentSelectedTokenInformation() {
        String selectedOnSpinner = spinner.getSelectedItem().toString().trim().toLowerCase();
        TokenInformation tokenInformation = getAuthInformation(selectedOnSpinner + Constants.TOKEN_OBJ_SUFFIX);

        if (tokenInformation != null) {
            return tokenInformation;
        }

        return new TokenInformation(selectedOnSpinner);
    }

    /**
     * @param network: MUST be an Constant like Constants.REDDIT_TOKEN_OBJ
     * @return AuthInformation object
     */
    public TokenInformation getAuthInformation(String network) {
        String json = pref.getString(network, Constants.NO_RESULT);

        if (!json.equals(Constants.NO_RESULT)) {
            return new Gson().fromJson(json, TokenInformation.class);
        }
        return null;
    }

    /**
     * Tokens are normally valid for one Hour.
     * If a access token is expired, they can be refreshed.
     *
     * @return 3 == access token is expired [AT_NEEDS_REFRESH]
     */
    public int checkTokenExpiration() {
        TokenInformation tokenInformation = getCurrentSelectedTokenInformation();
        long token = tokenInformation.getTokenTimestamp();
        long accessToken = tokenInformation.getAccessTokenTimestamp();
        String accessTokenString = tokenInformation.getAccessToken();

        //Check if Access Token is expired.
        if (this.tokenExpired(accessToken)) {
            //Check if access Token was ever retrieved
            if (accessTokenString.isEmpty()) {
                //Check if Auth Token is expired. -> Clear cookies and get new token + timestamp
                if (this.tokenExpired(token)) {
                    this.infoText.setText("Auth token is expired.\nRetrieve new auth token.");
                    return Constants.T_EXPIRED;
                } else {
                    //Auth Token is not expired but access token was never retrieved.
                    //Get access token with grant_type with authorization_code
                    this.infoText.setText("Access token was never retrieved.\nRetrieve first access token.");
                    return Constants.AT_NEVER_RETRIEVED;
                }
            } else if (!accessTokenString.isEmpty() && this.tokenExpired(accessToken)) {
                //Access token is expired. Get new with grant_type refresh_token
                Log.i("MYY", "token >>" + accessTokenString);
                this.infoText.setText("Access token is expired.\nRefresh access token.");
                return Constants.AT_NEEDS_REFRESH;
            }
        }

        double minsLeft = Constants.ONE_HOUR_IN_MINS - (this.getTimeDifferentInMin(tokenInformation.getAccessTokenTimestamp()));
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
        if ((this.getTimeDifferentInMin(l) > Constants.ONE_HOUR_IN_MINS) || l == -1) {
            return true;
        }
        return false;
    }

    public void setCurrentAuthInformations() {
        String chosenNetwork = spinner.getSelectedItem().toString().toLowerCase();
        authInformation = authInformationHolder.get(chosenNetwork);
        Log.i("MYY", "Choosen Network: " + chosenNetwork);
    }

    public void updateState() {
        new UpdateStateAsyncTask().execute(this);
        //Second run shouldnt be 3
    }

    public void addAutoRefreshTimer(long timer) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule((Runnable) selectedAuthStrategy, timer, TimeUnit.MILLISECONDS);
        Log.i("MYY", "Auto refresh was set for " + selectedAuthStrategy.getAuthInformation().getPlatform() + " in " + timer + " ms.");
    }

    /**
     * Other Click Events
     */

    private AdapterView.OnItemSelectedListener spinnerOnItemClick() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenNetwork = parent.getSelectedItem().toString();
                updateState();
                Log.i("MYY", "Selected spinner item: " + chosenNetwork);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        };
    }

    public View.OnClickListener doRefreshOnClick(String network) {
        return v -> {
            try {
                if (!getTokenInformation(network).getToken().equals(Constants.NO_RESULT) &&
                        (!getTokenInformation(network).getAccessToken().equals(Constants.NO_RESULT))) {
                    refreshTokenBtn.setOnClickListener(authStrategys.get(network).refresh());
                    refreshTokenBtn.callOnClick();
                    refreshTokenBtn.setOnClickListener(selectedAuthStrategy.refresh());
                    this.latestRefreshedNetwork = network;
                    updateState();
                    networkRecyclerAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Authorize first.", Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException ex) {
                Log.i("MYY", "Invalid Authstrategy or AuthInformation. Refreshing not possible.");
                Toast.makeText(this, "Invalid Authstrategy or AuthInformation. Refreshing not possible.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * UI Objects
     */

    public void setAuthStatus(int authStatus) {
        Log.i("MYY", "Old state: " + this.authStatus);
        this.authStatus = authStatus;
        Log.i("MYY", "New state: " + this.authStatus);

    }

    public void updateUI() {
        setButtonStates();
        networkRecyclerAdapter.notifyDataSetChanged();
    }

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
        this.authInformationHolder.put("reddit", redditInfo);
        this.authStrategys.put("reddit", AuthStrategyFactory.getAuthStrategy(redditInfo));

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
        this.authInformationHolder.put("imgur", imgurInfo);
        this.authStrategys.put("imgur", AuthStrategyFactory.getAuthStrategy(imgurInfo));

        AuthInformation twitterInfo = new AuthInformation();
        twitterInfo.setPlatform("twitter");
        /* TODO alle für deinen OAuth-Vorgang nötigen values setzen.
         * Am besten mit einer Constantsdatei im Constantsordner (auth/constants) ums einheitlich zu machen.
         */
        this.authInformationHolder.put("twitter", twitterInfo);
        this.authStrategys.put("twitter", AuthStrategyFactory.getAuthStrategy(twitterInfo));

        AuthInformation youtubeInfo = new AuthInformation();
        /* TODO alle für deinen OAuth-Vorgang nötigen values setzen.
         * Am besten mit einer Constantsdatei im Constantsordner (auth/constants) ums einheitlich zu machen.
         */
        this.authInformationHolder.put("youtube", youtubeInfo);
        youtubeInfo.setPlatform("youtube");
        this.authStrategys.put("youtube", AuthStrategyFactory.getAuthStrategy(youtubeInfo));

        AuthInformation instagramInfo = new AuthInformation();
        /* TODO alle für deinen OAuth-Vorgang nötigen values setzen.
         * Am besten mit einer Constantsdatei im Constantsordner (auth/constants) ums einheitlich zu machen.
         */
        this.authInformationHolder.put("instagram", instagramInfo);
        instagramInfo.setPlatform("instagram");
        this.authStrategys.put("instagram", AuthStrategyFactory.getAuthStrategy(instagramInfo));
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
        } else if (id == R.id.action_reset) {
            resetTokensForSelectedNetwork();
            for(int i = 0; i < this.tokenInformationPerNetwork.size(); i++){
                if(this.tokenInformationPerNetwork.get(i).getNetwork().equals(getCurrentSelectedTokenInformation().getNetwork())){
                    this.tokenInformationPerNetwork.get(i).setAccessToken(Constants.NO_RESULT);
                    this.tokenInformationPerNetwork.get(i).setAccessTokenTimestamp(-1);
                    this.tokenInformationPerNetwork.get(i).setRefreshToken(Constants.NO_RESULT);
                    this.tokenInformationPerNetwork.get(i).setRefreshTokenTimestamp(-1);
                    this.tokenInformationPerNetwork.get(i).setToken(Constants.NO_RESULT);
                    this.tokenInformationPerNetwork.get(i).setTokenTimestamp(-1);
                }
            }
            infoText.setText("Tokens for network " + spinner.getSelectedItem().toString() + " were reset.");
            networkRecyclerAdapter.notifyDataSetChanged();
        } else if (id == R.id.action_update_ui) {
            updateState();
            updateUI();
            networkRecyclerAdapter.notifyDataSetChanged();
        } else if (id == R.id.action_refresh_token) {
            refreshTokenBtn.callOnClick();
            networkRecyclerAdapter.notifyDataSetChanged();
        }


        return super.onOptionsItemSelected(item);
    }

    private void resetTokensForSelectedNetwork() {
        TokenInformation tokenInformation = getCurrentSelectedTokenInformation();
        tokenInformation = new TokenInformation(tokenInformation.getNetwork());

        Gson gson = new Gson();
        String json = gson.toJson(tokenInformation);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(spinner.getSelectedItem().toString().trim().toLowerCase() + Constants.TOKEN_OBJ_SUFFIX, json);
        editor.apply();
    }

    public static MainActivity getMainActivityInstance() {
        return instance;
    }

    /*
     **********************************************************************************
     ** Provide SocialMediaSteganography Objects here: ********************************
     **********************************************************************************
     */

    public Map<String, SocialMedia> provideActiveSocial() {
        return this.networks;
    }

}