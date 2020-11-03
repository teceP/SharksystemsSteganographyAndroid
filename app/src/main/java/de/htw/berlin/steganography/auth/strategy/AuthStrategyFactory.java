package de.htw.berlin.steganography.auth.strategy;

import android.util.Log;

import de.htw.berlin.steganography.auth.models.AuthInformation;

public class AuthStrategyFactory {

    public static AuthStrategy getAuthStrategy(AuthInformation authInformation){
        if(authInformation.getPlatform() == null || authInformation.getPlatform().equals("")){
            return null;
        }

        switch(authInformation.getPlatform().toLowerCase()){
            case "reddit":
                return new RedditAuthStrategy(authInformation);
            case "imgur":
                return new ImgurAuthStrategy(authInformation);
            case "instagram":
                return new InstagramAuthStrategy(authInformation);
            case "twitter":
                return new TwitterAuthStrategy(authInformation);
            default:
                return null;
        }
    }
}
