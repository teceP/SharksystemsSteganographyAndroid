package de.htw.berlin.steganography.auth.strategy;


import android.view.View;

import de.htw.berlin.steganography.OAuthMainActivity;
import de.htw.berlin.steganography.auth.models.AuthInformation;

public class TwitterAuthStrategy  extends BasicAbstractAuthStrategy{
    public TwitterAuthStrategy(OAuthMainActivity context, AuthInformation authInformation) {
        super(context, authInformation);
    }

    /*
     PLACEHOLDER for own individual implementation for a new OAuth2 Provider.

     Info: im authInformation Objekt sind alle Daten enhalten, die ihr für euren OAuth Vorgang
     benötigt und vorher in der MainActivity eingesetzt habt. Falls ihr noch weitere Attribute braucht,
     bearbeitet einfach die AuthInformation-Klasse. Aber bitte keine Attribute entfernen.
   */

    /*public TwitterAuthStrategy(OAuthMainActivity context, AuthInformation authInformation) {
        super(context, authInformation);
    }*/

    @Override
    public View.OnClickListener authorize() {
        return null;
    }

    @Override
    public View.OnClickListener token() {
        return null;
    }

    @Override
    public View.OnClickListener refresh() {
        return null;
    }
}
