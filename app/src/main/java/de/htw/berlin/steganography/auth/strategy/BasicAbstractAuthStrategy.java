package de.htw.berlin.steganography.auth.strategy;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import de.htw.berlin.steganography.OAuthMainActivity;
import de.htw.berlin.steganography.R;
import de.htw.berlin.steganography.auth.constants.Constants;
import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.models.TokenInformation;

/**
 * @author Mario Teklic
 */

/**
 * Abstract implementation of an AuthStrategy implementation, which contains several
 * methods and the auth information, which are used in each implementation.
 *
 * Each Auth Strategy should implement a OAuth2 flow which is specified in the RFC6749:
 * https://tools.ietf.org/html/rfc6749
 */
public abstract class BasicAbstractAuthStrategy extends AppCompatActivity implements AuthStrategy, Runnable {

    /**
     * Holds any Auth Information which are needed in a OAuth2 Flow, or which are generated in this flow.
     */
    private AuthInformation authInformation;

    /**
     * Context
     */
    protected OAuthMainActivity contextActivity;

    public BasicAbstractAuthStrategy(OAuthMainActivity context, AuthInformation authInformation){
        this.authInformation = authInformation;
        contextActivity = context;
    }

    @Override
    public AuthInformation getAuthInformation() {
        return authInformation;
    }

    @Override
    public void setAuthInformation(AuthInformation authInformation){
        this.authInformation = authInformation;
    }

    /**
     * 1. action in the flow, which authorizes the user with the individual provider (e.g. Reddit, Imgur)
     *
     * @return Authorization Token
     */
    @Override
    public abstract View.OnClickListener authorize();

    /**
     * 2. action in the flow, which triggers the provider to send a "Access Token" and a "Refresh Token" in exchange for the
     *  Authorization token from step 1.
     *
     */
    @Override
    public abstract View.OnClickListener token();

    /**
     * (3.) Action in the flow. Trigger the provider to send a fresh access token.
     */
    @Override
    public abstract View.OnClickListener refresh();

    /**
     * Triggers the refresh-Method to execute.
     */
    @Override
    public void run() {
        View v = OAuthMainActivity.getMainActivityInstance().findViewById(R.id.dummyBtn);
        v.setOnClickListener(this.refresh());
        v.callOnClick();
    }

    /**
     * Restores AuthInformation objects from shared preferences.
     * @param context
     *
     * @return the restored objects or in case the JSON was empty, an fresh objects.
     */
    public TokenInformation getTokenInformation(Context context, String network){
        String json = context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE)
                .getString(network + Constants.TOKEN_OBJ_SUFFIX, Constants.NO_RESULT);

        if(!json.equals(Constants.NO_RESULT)){
            return new Gson().fromJson(json, TokenInformation.class);
        }

        return new TokenInformation(network);
    }

    /**
     * Stores a TokenInformation object.
     * @param context
     * @param tokenInformation
     */
    public void applyTokenInformation(Context context, TokenInformation tokenInformation){
        Gson gson = new Gson();
        String json = gson.toJson(tokenInformation);
        context.getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE)
                .edit().putString(tokenInformation.getNetwork() + Constants.TOKEN_OBJ_SUFFIX, json)
                .apply();
        Log.i("MYY", "Alt: " + OAuthMainActivity.getMainActivityInstance().getParcelMap().get(tokenInformation.getNetwork()).getTokenInformation().toString());
        Log.i("MYY", "Neu: " + tokenInformation.toString());

        OAuthMainActivity.getMainActivityInstance().getParcelMap().get(tokenInformation.getNetwork()).setTokenInformation(tokenInformation);

        if(OAuthMainActivity.getMainActivityInstance().getCurrentSelectedNetwork().getNetworkName() == tokenInformation.getNetwork()){
            OAuthMainActivity.getMainActivityInstance().updateCurrentSelectedNetworkTokenInformation(tokenInformation);
        }
    }

    /**
     * Clears all stored tokens. Use with care.
     */
    public void clearTokens(){
        OAuthMainActivity.getMainActivityInstance().getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
