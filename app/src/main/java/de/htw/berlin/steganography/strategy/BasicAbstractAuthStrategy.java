package de.htw.berlin.steganography.strategy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import de.htw.berlin.steganography.Constants;
import de.htw.berlin.steganography.MainActivity;
import de.htw.berlin.steganography.auth.Information;

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
