package de.htw.berlin.steganography;


import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import de.htw.berlin.steganography.MainActivity;

public class UpdateStateAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.getMainActivityInstance().progressPnl.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        MainActivity ma = MainActivity.getMainActivityInstance();
        ma.setInformations();
        ma.restoreSocialMedias();
        ma.selectedAuthStrategy = ma.authStrategys.get(ma.getCurrentSelectedTokenInformation().getNetwork());
        ma.authStatus = ma.checkTokenExpiration();
        Log.i("MYY", (ma.authStrategys.size() + ma.selectedAuthStrategy.getAuthInformation().getPlatform()) + "--c----" );
        ma.oauthBtn.setOnClickListener(ma.selectedAuthStrategy.authorize());
        ma.refreshTokenBtn.setOnClickListener(ma.selectedAuthStrategy.refresh());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MainActivity ma = MainActivity.getMainActivityInstance();
        ma.setButtonStates();
        ma.networkRecyclerAdapter.notifyDataSetChanged();
        ma.progressPnl.setVisibility(View.GONE);
    }
}
