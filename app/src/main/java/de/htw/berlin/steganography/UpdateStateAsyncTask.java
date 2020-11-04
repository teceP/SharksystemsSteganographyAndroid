package de.htw.berlin.steganography;


import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import de.htw.berlin.steganography.auth.constants.Constants;

public class UpdateStateAsyncTask extends AsyncTask<MainActivity, Void, MainActivity> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.getMainActivityInstance().progressPnl.setVisibility(View.VISIBLE);
    }

    @Override
    protected synchronized MainActivity doInBackground(MainActivity... activities) {
        Log.i("MYY", "#################################################################");
        Log.i("MYY", "Update data...");
        MainActivity ma = activities[0];
        ma.setCurrentAuthInformations();
        if(!ma.latestRefreshedNetwork.equals(Constants.NO_RESULT)){
            ma.updateSocialMediaToken(ma.latestRefreshedNetwork);
        }
        ma.selectedAuthStrategy = ma.authStrategys.get(ma.getCurrentSelectedTokenInformation().getNetwork());
        ma.setAuthStatus(ma.checkTokenExpiration());
        ma.oauthBtn.setOnClickListener(ma.selectedAuthStrategy.authorize());
        ma.refreshTokenBtn.setOnClickListener(ma.selectedAuthStrategy.refresh());
        Log.i("MYY", "Update data finished.");
        Log.i("MYY", "################################################################");

        return ma;
    }

    @Override
    protected synchronized void onPostExecute(MainActivity mainActivity) {
        Log.i("MYY", "-----------------------------------------------------------------");
        super.onPostExecute(mainActivity);
        Log.i("MYY", "Update UI...");
        MainActivity.getMainActivityInstance().setButtonStates();
        mainActivity.progressPnl.setVisibility(View.GONE);
        Log.i("MYY", "Update UI finished.");
        Log.i("MYY", "-------------------------------------------------------------------");

    }
}
