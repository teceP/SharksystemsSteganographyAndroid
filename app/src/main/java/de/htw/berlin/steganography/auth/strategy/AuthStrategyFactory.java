package de.htw.berlin.steganography.auth.strategy;

import android.util.Log;

import de.htw.berlin.steganography.auth.models.Information;

public class AuthStrategyFactory {

    public static AuthStrategy getAuthStrategy(Information information){
        Log.i("MYYY", "-----");
        Log.i("MYYY", information.getPlatform());
        if(information.getPlatform() == null || information.getPlatform().equals("")){
            return null;
        }

        switch(information.getPlatform().toLowerCase()){
            case "reddit":
                return new RedditAuthStrategy(information);
            case "imgur":
                return new ImgurAuthStrategy(information);
            case "instagram":
                return new InstagramAuthStrategy(information);
            case "twitter":
                return new TwitterAuthStrategy(information);
            default:
                return null;
        }
    }
}
