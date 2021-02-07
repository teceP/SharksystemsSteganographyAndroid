package de.htw.berlin.steganography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.imgur.Imgur;
import de.htw.berlin.steganography.apis.models.APINames;
import de.htw.berlin.steganography.apis.models.Token;
import de.htw.berlin.steganography.apis.reddit.Reddit;
import de.htw.berlin.steganography.adapters.NetworkListAdapter;
import de.htw.berlin.steganography.adapters.SimpleDividerItemDecoration;
import de.htw.berlin.steganography.auth.constants.Constants;
import de.htw.berlin.steganography.auth.constants.ImgurConstants;
import de.htw.berlin.steganography.auth.constants.RedditConstants;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.models.NetworkParcel;
import de.htw.berlin.steganography.auth.models.NetworkName;
import de.htw.berlin.steganography.auth.models.TokenInformation;
import de.htw.berlin.steganography.auth.strategy.ImgurAuthStrategy;
import de.htw.berlin.steganography.auth.strategy.RedditAuthStrategy;
import de.htw.berlin.steganography.auth.strategy.TwitterAuthStrategy;
import de.htw.berlin.steganography.persistence.JSONPersistentManager;
import de.htw.berlin.steganography.persistence.JSONPersistentWriter;

import static java.lang.Thread.sleep;

/**
 * @author Mario Teklic
 */

public class OAuthMainActivity extends AppCompatActivity {

    TextView somethingWentWrong;

    private static OAuthMainActivity instance;

    /**
     * A list of all networks, which has tokens and a valid timestamp.
     * Note: Token could be expired.
     */
    private Map<String, NetworkParcel> parcelMap;
    private List<TokenInformation> tokenInformationsRecyclerView;

    String currentSelectedNetworkString;

    Spinner spinner;
    Integer authStatus;
    Button oauthBtn, refreshTokenBtn;

    RecyclerView networkRecyclerView;
    RecyclerView.Adapter networkRecyclerAdapter;
    RecyclerView.LayoutManager networkRecyclerLayoutManager;

    SharedPreferences pref;
    TextView infoText;
    private String passedActivityString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth_activity_main);

        //example: "reddit", "imgur", "youtube"
        currentSelectedNetworkString = getIntent().getStringExtra("selectedNetwork");

        passedActivityString = getIntent().getStringExtra("activityToCall");

        somethingWentWrong = (TextView) findViewById(R.id.somethingWentWrongId);
        somethingWentWrong.setVisibility(View.INVISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // Hide your View after 3 seconds
                somethingWentWrong.setVisibility(View.VISIBLE);
            }
        }, 3000);

        try {
            validatePassedNetwork();



        JSONPersistentWriter writer = new JSONPersistentWriter(this);
        JSONPersistentManager.getInstance().setJsonPersistentHelper(writer);

        this.initObjects();

        /**
         * Restore Data
         */
        this.restoreNetworkParcels();

        this.updateTokenInformationForRecyclerView();


        /**
         * Recycler View
         */
        this.networkRecyclerView = findViewById(R.id.activeNetworksList);
        this.networkRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        this.networkRecyclerLayoutManager = new LinearLayoutManager(this);
        this.networkRecyclerAdapter = new NetworkListAdapter(this.tokenInformationsRecyclerView);
        this.networkRecyclerView.setLayoutManager(networkRecyclerLayoutManager);
        this.networkRecyclerView.setAdapter(networkRecyclerAdapter);

        /**
         *  Update UI
         */
        this.updateUI();


        /*
        String passedActivityString = "com.example.stegaexampleapp.FileUploadedTestActivity";
        openPassedActivity(passedActivityString);*/

        oauthBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().token());
        oauthBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().authorize());
        oauthBtn.callOnClick();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public String getPassedActivityString(){
        return passedActivityString;
    }

    public void openPassedActivity(String passedActivityString, String accesToken)throws ClassNotFoundException {
        try {
            Intent intent = new Intent(this , Class.forName(passedActivityString));
            intent.putExtra("selectedNetwork", currentSelectedNetworkString);
            intent.putExtra("accesToken", accesToken);
           // intent.putExtra("tokenOAuth",toDo);
            startActivity(intent);

        } catch (ClassNotFoundException e) {

            e.printStackTrace();
            throw new ClassNotFoundException("Did not pass full package String to OAuthMainActivity or Class does not exist. Add intent.putExtra(\"activityToCall\",\"package.path.to.Activity\" .");
        }
    }

    /**
     * Initialize all variables
     */
    public void initObjects() {
        instance = this;

        this.parcelMap = new TreeMap<>();
        this.tokenInformationsRecyclerView = new ArrayList<>();

        this.pref = getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE);

        /**
         * UI Elements
         */
        this.infoText = findViewById(R.id.infoText);

        this.setSpinner();
        spinner.setEnabled(false);

        //OAuth2 Button
        this.oauthBtn = findViewById(R.id.auth);
        oauthBtn.setVisibility(View.GONE);

        this.refreshTokenBtn = findViewById(R.id.refreshTokenBtn);
        this.refreshTokenBtn.setVisibility(View.GONE);


        /**
         * Initial token state
         */
        this.authStatus = new Integer(Constants.STATUS_UNCHECKED);
    }

    /**
     * Returns all TokenInformations for each Network.
     *
     * @return
     */
    public List<TokenInformation> getAllTokenInformations() {
        List<TokenInformation> list = new ArrayList<>();
        for (NetworkParcel parcel : this.parcelMap.values()) {
            list.add(parcel.getTokenInformation());
        }
        return list;
    }

    /**
     * Updates all Network Parcels with data from SharedPreferences
     */
    public void updateSocialMediaTokens() {
        for(NetworkParcel np : this.parcelMap.values()){
            TokenInformation ti = getTokenInformationFromSharedPref(np.getNetworkName());
            this.parcelMap.get(np.getNetworkName()).getTokenInformation().setAccessToken(ti.getAccessToken());
            this.parcelMap.get(np.getNetworkName()).getTokenInformation().setAccessTokenTimestamp(ti.getAccessTokenTimestamp());
            Log.i("MYY", ti.toString());
        }
    }

    /**
     * Returns the current selected network's Network Parcel
     * @return
     */
    public NetworkParcel getCurrentSelectedNetwork() {

        Log.i("spinner string for parcelMap.get",spinner.getSelectedItem().toString().toLowerCase());
        NetworkParcel returnParcel = this.parcelMap.get(currentSelectedNetworkString);
        return returnParcel;
        //return this.parcelMap.get(spinner.getSelectedItem().toString().toLowerCase());
    }

    /**
     * Updates the token information of the current selected network
     * @param tokenInformation
     */
    public void updateCurrentSelectedNetworkTokenInformation(TokenInformation tokenInformation) {
        this.getCurrentSelectedNetwork().setTokenInformation(tokenInformation);
    }

    /**
     * Restores AuthInformation objects from shared preferences.
     *
     * @return the restored objects or in case the JSON was empty, an fresh objects.
     */
    public TokenInformation getTokenInformationFromSharedPref(String network) {
        String json = pref.getString(network + Constants.TOKEN_OBJ_SUFFIX, Constants.NO_RESULT);

        if (!json.equals(Constants.NO_RESULT)) {
            return new Gson().fromJson(json, TokenInformation.class);
        }

        return new TokenInformation(network);
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

    /**
     * Updates OAUTH2-Button. Change clickable und alpha settings.
     * Calls method for receiving Access Token if Access Token was never received.
     */
    public synchronized void setButtonStates() {
        Log.i("MYY", "Authorization Status Code before setButtonState(): " + this.authStatus);
        this.authStatus = checkTokenExpiration();

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
            oauthBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().token());
            oauthBtn.callOnClick();
            oauthBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().authorize());
            Log.i("reddit TokenInformation", getCurrentSelectedNetwork().getTokenInformation().toString());

            refreshTokenBtn.setClickable(false);
            refreshTokenBtn.setAlpha(.2f);
        } else if (this.authStatus == Constants.AT_NEEDS_REFRESH) {
            //Only refresh possible. Will be called automatically
            oauthBtn.setClickable(false);
            oauthBtn.setAlpha(.2f);

            refreshTokenBtn.callOnClick();
            this.authStatus = 0;
        }

        this.authStatus = checkTokenExpiration();
        Log.i("MYY", "Authorization Status Code after setButtonState(): " + this.authStatus);
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
    public TokenInformation getCurrentSelectedTokenInformationFromSharedPref() {
        String selectedOnSpinner = spinner.getSelectedItem().toString().trim().toLowerCase();
        TokenInformation tokenInformation = getTokenInformationByNetworkNameFromSharedPref(selectedOnSpinner + Constants.TOKEN_OBJ_SUFFIX);

        if (tokenInformation != null) {
            return tokenInformation;
        }

        return new TokenInformation(selectedOnSpinner);
    }

    /**
     * @param network: MUST be an Constant like Constants.REDDIT_TOKEN_OBJ
     * @return AuthInformation object
     */
    public TokenInformation getTokenInformationByNetworkNameFromSharedPref(String network) {
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
        TokenInformation tokenInformation = getCurrentSelectedNetwork().getTokenInformation();
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

    public void updateState(){
        runOnUiThread(() -> {
            Log.i("MYY", "Update data...");
            setAuthStatus(checkTokenExpiration());
            oauthBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().authorize());
            Log.i("reddit TokenInformation", getCurrentSelectedNetwork().getTokenInformation().toString());

            refreshTokenBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().refresh());
            updateSocialMediaTokens();
            updateUI();
            Log.i("MYY", "Update data finished.");
        });
        //taskRunner.executeAsync(new UpdateTask(this),MainActivity::nothing);
    }

    private void nothing(){
    }

    public void addAutoRefreshTimer(String network, long timer) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule((Runnable) getParcelMap().get(network).getAuthStrategy(), timer, TimeUnit.MILLISECONDS);
        Log.i("MYY", "Auto refresh was set."
                + "\nNetwork: " + network
                + "\nMS: " + timer);
    }

    /**
     * Other Click Events
     */

    /**
     * Performs "updateState()" when executed
     * @return OnClickListener
     */
    private AdapterView.OnItemSelectedListener spinnerOnItemClick() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateState();
                Log.i("MYY", "Selected spinner item: " + getCurrentSelectedNetwork().getNetworkName());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        };
    }

    /**
     * Performs a refresh-token of a network
     * @param network Name of the network
     * @return OnClickListener
     */
    public View.OnClickListener doRefreshOnClick(String network) {
        return v -> {
            try {
                if (!getTokenInformationFromSharedPref(network).getToken().equals(Constants.NO_RESULT) &&
                        (!getTokenInformationFromSharedPref(network).getAccessToken().equals(Constants.NO_RESULT))) {
                    refreshTokenBtn.setOnClickListener(this.parcelMap.get(network).getAuthStrategy().refresh());
                    refreshTokenBtn.callOnClick();
                    refreshTokenBtn.setOnClickListener(getCurrentSelectedNetwork().getAuthStrategy().refresh());
                    updateState();
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

    /**
     * Sets the authentication status
     * @param authStatus
     */
    public void setAuthStatus(int authStatus) {
        Log.i("MYY", "Old state: " + this.authStatus);
        this.authStatus = authStatus;
        Log.i("MYY", "New state: " + this.authStatus);
    }

    /**
     * Updates user interface with up-to-date data
     */
    public void updateUI() {
        updateTokenInformationForRecyclerView();
        setButtonStates();
        networkRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * RESTORE
     */

    /**
     * Updates the token information for the recyclerview
     * -> Not in use anymore after app user-interface reduction
     */
    public void updateTokenInformationForRecyclerView() {
        this.tokenInformationsRecyclerView.clear();
        for (TokenInformation ti : this.parcelMap.values().stream().map(NetworkParcel::getTokenInformation).collect(Collectors.toList())) {
            Log.i("reddit TokenInformation", ti.toString());
            this.tokenInformationsRecyclerView.add(ti);
        }
    }

    /**
     * Restores all network parcels from shared preferences, which where stored in the past
     * -> Not in use anymore after app user-interface reduction
     */
    public void restoreNetworkParcels() {
        List<String> networks = new ArrayList<>();
        networks.add(Constants.REDDIT_TOKEN_OBJ);
        networks.add(Constants.IMGUR_TOKEN_OBJ);
        networks.add(Constants.TWITTER_TOKEN_OBJ);

        for (String network : networks) {
            String networkName = null;
            AuthInformation authInformation = null;
            TokenInformation tokenInformation = getTokenInformationByNetworkNameFromSharedPref(network);
            NetworkParcel np = null;

            if (tokenInformation == null) {
                tokenInformation = new TokenInformation(network.replace(Constants.TOKEN_OBJ_SUFFIX, ""));
            }

            switch (tokenInformation.getNetwork()) {
                case NetworkName.REDDIT:
                    SocialMedia reddit = new Reddit(null);
                    reddit.setToken(new Token(tokenInformation.getAccessToken(), tokenInformation.getAccessTokenTimestamp()));

                    networkName = NetworkName.REDDIT;
                    authInformation = getAuthInformation(networkName);

                    NetworkParcel.Builder builder = new NetworkParcel.Builder();
                    builder.withNetworkName(networkName);
                    builder.withTokenInformation(tokenInformation);
                    builder.withAuthStrategy(new RedditAuthStrategy(this,authInformation));
                    builder.withSocialMedia(reddit);
                    np = builder
                            .build();

                    this.parcelMap.put(np.getNetworkName(), np);
                    break;
                case NetworkName.IMGUR:
                    SocialMedia imgur = new Imgur(null);
                    imgur.setToken(new Token(tokenInformation.getAccessToken(), tokenInformation.getAccessTokenTimestamp()));

                    networkName = NetworkName.IMGUR;
                    authInformation = getAuthInformation(networkName);

                    np = new NetworkParcel.Builder()
                            .withNetworkName(networkName)
                            .withTokenInformation(tokenInformation)
                            .withAuthStrategy(new ImgurAuthStrategy(this, authInformation))
                            .withSocialMedia(imgur)
                            .build();

                    this.parcelMap.put(np.getNetworkName(), np);
                    break;
                case NetworkName.TWITTER:
                    /**
                     * TODO dein social media objekt initialisieren, token setzen und in die networks liste adden
                     */

                    networkName = NetworkName.TWITTER;
                    authInformation = getAuthInformation(networkName);

                    np = new NetworkParcel.Builder()
                            .withNetworkName(networkName)
                            .withTokenInformation(tokenInformation)
                            .withAuthStrategy(new TwitterAuthStrategy(this, authInformation))
                            //.withSocialMedia(twitter)
                            .build();

                    this.parcelMap.put(np.getNetworkName(), np);
                    break;
            }
        }
    }

    /**
     * Returns the Auth Information about a Network such a OAuth URL and so on.
     * @param network Network Name
     * @return
     */
    private AuthInformation getAuthInformation(String network) {
        AuthInformation authInformation;
        switch (network) {
            case NetworkName.REDDIT:
                authInformation = new AuthInformation.Builder()
                        .withPlatform(NetworkName.REDDIT)
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
                return authInformation;
            case NetworkName.IMGUR:
                authInformation = new AuthInformation().Builder()
                        .withPlatform(NetworkName.IMGUR)
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
                return authInformation;
            case NetworkName.TWITTER:
                /* TODO alle für deinen OAuth-Vorgang nötigen values setzen.
                 * Am besten mit einer Constantsdatei im Constantsordner (auth/constants) ums einheitlich zu machen.
                 */
                authInformation = new AuthInformation.Builder()
                        .withPlatform(NetworkName.TWITTER)
                        .build();
                return authInformation;
            default:
                return null;
        }
    }

    /**
     * @{inheritDoc}
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * @return Parcel Map with all network parcels
     */
    public Map<String, NetworkParcel> getParcelMap() {
        return this.parcelMap;
    }

    /**
     * Top Navi Menu
     * -> Not in use anymore after app user-interface reduction
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_reset) {
            resetTokensForSelectedNetwork();
            infoText.setText("Tokens for network " + spinner.getSelectedItem().toString() + " were reset.");
            updateState();
        } else if (id == R.id.action_update_ui) {
            updateState();
        } else if (id == R.id.action_refresh_token) {
            refreshTokenBtn.callOnClick();
            updateState();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Resets the token for the current selected network
     */
    public void resetTokensForSelectedNetwork() {
        TokenInformation tokenInformation = getCurrentSelectedTokenInformationFromSharedPref();
        tokenInformation = new TokenInformation(tokenInformation.getNetwork());

        Gson gson = new Gson();
        String json = gson.toJson(tokenInformation);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(spinner.getSelectedItem().toString().trim().toLowerCase() + Constants.TOKEN_OBJ_SUFFIX, json);
        editor.apply();
    }

    /**
     * Resets the token for a specific network
     */
    public void resetTokensForSpecificNetwork(String networkName) {
        TokenInformation tokenInformation = getTokenInformationFromSharedPref(networkName);

        Gson gson = new Gson();
        String json = gson.toJson(tokenInformation);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(tokenInformation.getNetwork().toLowerCase().trim() + Constants.TOKEN_OBJ_SUFFIX, json);
        editor.apply();
    }

    public static OAuthMainActivity getMainActivityInstance() {
        return instance;
    }

    /*
     **********************************************************************************
     ** Provide SocialMediaSteganography Objects here: ********************************
     **********************************************************************************
     */

    /**
     * Returns the access token for a network
     * @param network
     * @return
     */
    public String provideToken(String network) {
        return this.parcelMap.get(network).getTokenInformation().getAccessToken();
    }


    /****************************************
     *  OTHERS: *****************************
     ***
     ***/

    /**
     * Sets all spinner options
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


    boolean validatePassedNetwork()throws ClassNotFoundException{
        if(this.currentSelectedNetworkString==null){
            throw new ClassNotFoundException("Did not pass network String to OAuthMainActivity or network does not exist. Add intent.putExtra(\"selectedNetwork\",\"networkName\" .Check Enum APINames for correct notation.");
        }
        try {
            APINames.valueOf(currentSelectedNetworkString.toUpperCase());
        }
        catch (Exception e){
            e.printStackTrace();
            throw new ClassNotFoundException("Did not pass network String to OAuthMainActivity or network does not exist. Add intent.putExtra(\"selectedNetwork\",\"networkName\" .Check Enum APINames for correct notation.");

        }
        return true;
    }

}