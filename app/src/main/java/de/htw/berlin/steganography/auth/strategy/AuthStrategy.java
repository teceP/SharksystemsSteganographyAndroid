package de.htw.berlin.steganography.auth.strategy;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.models.TokenInformation;

/**
 * @author Mario Teklic
 */

public interface AuthStrategy {

    /**
     * Represents the oauth2 authorization flow
     */
    View.OnClickListener authorize();

    /**
     * Represents the oauth2 access token & refresh token retrieving flow
     */
    View.OnClickListener token();

    /**
     * Represents the oauth2 refresh token flow
     */
    View.OnClickListener refresh();

    /**
     * @return Authorization information
     */
    AuthInformation getAuthInformation();

    void setAuthInformation(AuthInformation authInformation);

    /**
     *
     * @param context
     * @param network Example "reddit", "imgur", ...
     * @return Returns current token information for a specific network.
     */
    TokenInformation getTokenInformation(Context context, String network);

    /**
     * Stores the token information in the shared preferences.
     * @param context
     * @param tokenInformation
     */
    void applyTokenInformation(Context context, TokenInformation tokenInformation);

}
