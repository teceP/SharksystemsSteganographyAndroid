package de.htw.berlin.steganography.auth.strategy;

import android.app.Activity;
import android.util.Log;

import de.htw.berlin.steganography.OAuthMainActivity;
import de.htw.berlin.steganography.auth.models.AuthInformation;

public class AuthStrategyFactory {



    public static AuthStrategy getAuthStrategy(OAuthMainActivity context, AuthInformation authInformation){
        if(authInformation.getPlatform() == null || authInformation.getPlatform().equals("")){
            return null;
        }

        switch(authInformation.getPlatform().toLowerCase()){
            case "reddit":
                return new RedditAuthStrategy(context, authInformation);
            case "imgur":
                return new ImgurAuthStrategy(context, authInformation);
            case "twitter":
                return new TwitterAuthStrategy(context, authInformation);
            default:
                return null;
        }
    }
}
