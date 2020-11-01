package de.htw.berlin.steganography.auth.strategy;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import de.htw.berlin.steganography.auth.models.AuthInformation;

public class TwitterAuthStrategy  extends BasicAbstractAuthStrategy{
    //TODO

    public TwitterAuthStrategy(AuthInformation authInformation) {
        super(authInformation);
    }

    @Override
    public View.OnClickListener authorize(Context context, TextView infoText, View retrieveAuthTokenBtn, View retrieveAccessTokenBtn) {
        return null;
    }

    @Override
    public View.OnClickListener token(Context context, TextView infoText) {
        return null;
    }

    @Override
    public View.OnClickListener refresh(Context context) {
        return null;
    }
}
