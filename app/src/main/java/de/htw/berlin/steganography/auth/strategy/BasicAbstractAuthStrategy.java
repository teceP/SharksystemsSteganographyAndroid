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

public abstract class BasicAbstractAuthStrategy extends AppCompatActivity implements AuthStrategy, Runnable {

    private AuthInformation authInformation;
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

    @Override
    public abstract View.OnClickListener authorize();

    @Override
    public abstract View.OnClickListener token();

    @Override
    public abstract View.OnClickListener refresh();

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

    public void clearTokens(){
        OAuthMainActivity.getMainActivityInstance().getSharedPreferences(Constants.SHARKSYS_PREF, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
