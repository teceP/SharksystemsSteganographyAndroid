package de.htw.berlin.steganography;


import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

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
        ma.setAuthStatus(ma.checkTokenExpiration());

        ma.oauthBtn.setOnClickListener(ma.getCurrentSelectedNetwork().getAuthStrategy().authorize());
        ma.refreshTokenBtn.setOnClickListener(ma.getCurrentSelectedNetwork().getAuthStrategy().refresh());

        ma.updateTokenInformationForRecyclerView();
        Log.i("MYY", "Update data finished.");
        Log.i("MYY", "################################################################");

        return ma;
    }

    @Override
    protected synchronized void onPostExecute(MainActivity mainActivity) {
        super.onPostExecute(mainActivity);
        Log.i("MYY", "-----------------------------------------------------------------");
        Log.i("MYY", "Update UI...");

        MainActivity.getMainActivityInstance().setButtonStates();
        mainActivity.progressPnl.setVisibility(View.GONE);

        Log.i("MYY", "Update UI finished.");
        Log.i("MYY", "-------------------------------------------------------------------");

    }
}
