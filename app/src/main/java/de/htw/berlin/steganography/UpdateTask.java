package de.htw.berlin.steganography;

import android.util.Log;

import java.util.concurrent.Callable;

/**
 * @author Mario Teklic
 */

public class UpdateTask implements Callable<OAuthMainActivity> {
    private OAuthMainActivity ma;

    public UpdateTask(OAuthMainActivity ma){
        this.ma = ma;
    }

    @Override
    public OAuthMainActivity call() {
        Log.i("MYY", "Update data...");
        ma.setAuthStatus(ma.checkTokenExpiration());
        ma.oauthBtn.setOnClickListener(ma.getCurrentSelectedNetwork().getAuthStrategy().authorize());
        ma.refreshTokenBtn.setOnClickListener(ma.getCurrentSelectedNetwork().getAuthStrategy().refresh());
        ma.updateSocialMediaTokens();
        ma.updateUI();
        Log.i("MYY", "Update data finished.");
        return ma;
    }
}
