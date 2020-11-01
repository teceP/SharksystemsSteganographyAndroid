package de.htw.berlin.steganography.auth.strategy;

import androidx.appcompat.app.AppCompatActivity;

import de.htw.berlin.steganography.auth.models.AuthInformation;

public abstract class BasicAbstractAuthStrategy extends AppCompatActivity implements AuthStrategy {

    private AuthInformation authInformation;

    public BasicAbstractAuthStrategy(AuthInformation authInformation){
        this.authInformation = authInformation;
    }

    @Override
    public AuthInformation getAuthInformation() {
        return authInformation;
    }

    @Override
    public void setAuthInformation(AuthInformation authInformation) {
        this.authInformation = authInformation;
    }
}
