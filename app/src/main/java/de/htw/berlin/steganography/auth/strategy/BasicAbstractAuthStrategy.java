package de.htw.berlin.steganography.auth.strategy;

import androidx.appcompat.app.AppCompatActivity;

import de.htw.berlin.steganography.auth.models.Information;

public abstract class BasicAbstractAuthStrategy extends AppCompatActivity implements AuthStrategy {

    private Information information;

    public BasicAbstractAuthStrategy(Information information){
        this.information = information;
    }

    @Override
    public Information getInformation() {
        return information;
    }

    @Override
    public void setInformation(Information information) {
        this.information = information;
    }
}
