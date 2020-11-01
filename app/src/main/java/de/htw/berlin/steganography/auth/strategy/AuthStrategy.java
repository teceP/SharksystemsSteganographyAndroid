package de.htw.berlin.steganography.auth.strategy;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import de.htw.berlin.steganography.auth.models.Information;

public interface AuthStrategy {

    /**
     * Represents the oauth2 authorization flow
     */
    View.OnClickListener authorize(Context context, TextView infoText, View retrieveAuthTokenBtn, View retrieveAccessTokenBtn);

    /**
     * Represents the oauth2 access token & refresh token retrieving flow
     */
    View.OnClickListener token(Context context, TextView infoText);

    /**
     * Represents the oauth2 refresh token flow
     */
    View.OnClickListener refresh(Context context);

    public Information getInformation();

    public void setInformation(Information information);
}
