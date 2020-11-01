package de.htw.berlin.steganography.auth.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;

import de.htw.berlin.steganography.Constants;
import de.htw.berlin.steganography.auth.models.TokenInformation;

public class RefreshTokenAsyncTask extends AsyncTask<TaskParcel, Void, Boolean> {

    @Override
    protected Boolean doInBackground(TaskParcel... taskParcels) {
        TaskParcel parcel = taskParcels[0];

        String tempAccess = parcel.getTokenInformation().getAccessToken();

        parcel.getAuthStrategy().refresh(parcel.getContextRef().get());

        SharedPreferences pref = parcel.getContextRef().get().getSharedPreferences(Constants.SHARKSYS_PREF, Context.MODE_PRIVATE);
        String json = pref.getString(parcel.getTokenInformation().getNetwork() + Constants.TOKEN_OBJ_SUFFIX, Constants.NO_RESULT);

        if(json.equals(Constants.NO_RESULT)){
            TokenInformation newTokenInfo = new Gson().fromJson(json, TokenInformation.class);
            if(tempAccess != null && newTokenInfo != null && !tempAccess.equals(newTokenInfo.getAccessToken()) && !newTokenInfo.getAccessToken().equals(Constants.NO_RESULT)){
                parcel.getInfoTextRef().get().setText("Token for network " + parcel.getTokenInformation().getNetwork() + " has been refreshed.");
                return true;
            }
        }

        parcel.getInfoTextRef().get().setText("Token for network " + parcel.getTokenInformation().getNetwork() + " has not been refreshed. Token has expired...");
        return false;
    }
}
