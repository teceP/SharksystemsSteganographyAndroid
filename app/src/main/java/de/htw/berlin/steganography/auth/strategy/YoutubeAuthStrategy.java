package de.htw.berlin.steganography.auth.strategy;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import de.htw.berlin.steganography.OAuthMainActivity;
import de.htw.berlin.steganography.auth.models.AuthInformation;

public class YoutubeAuthStrategy extends BasicAbstractAuthStrategy{

    /*TODO implement methods and do following:
     Am Ende jeder Methode muss folgende Methode aufgerufen werden:
     MainActivity.getMainActivityInstace().updateState();

     Info: im authInformation Objekt sind alle Daten enhalten, die ihr für euren OAuth Vorgang
     benötigt und vorher in der MainActivity eingesetzt habt. Falls ihr noch weitere Attribute braucht,
     bearbeitet einfach die AuthInformation-Klasse. Aber bitte keine Attribute entfernen.
   */

    public YoutubeAuthStrategy(OAuthMainActivity context, AuthInformation authInformation) {
        super(context, authInformation);
    }


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
