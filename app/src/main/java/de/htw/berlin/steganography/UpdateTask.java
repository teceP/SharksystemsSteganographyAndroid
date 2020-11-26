package de.htw.berlin.steganography;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.Callable;

public class UpdateTask implements Callable<MainActivity> {
    private MainActivity ma;

    @Override
    public MainActivity call() {
        Log.i("MYY", "#################################################################");
        Log.i("MYY", "Update data...");

        ma.setAuthStatus(ma.checkTokenExpiration());
        ma.oauthBtn.setOnClickListener(ma.getCurrentSelectedNetwork().getAuthStrategy().authorize());
        ma.refreshTokenBtn.setOnClickListener(ma.getCurrentSelectedNetwork().getAuthStrategy().refresh());
        ma.updateTokenInformationForRecyclerView();
        ma.setButtonStates();

        Log.i("MYY", "Update data finished.");
        Log.i("MYY", "################################################################");
        return ma;
    }
}
