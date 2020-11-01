package de.htw.berlin.steganography.auth.tasks;

import android.content.Context;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.htw.berlin.steganography.auth.models.AuthInformation;
import de.htw.berlin.steganography.auth.models.TokenInformation;
import de.htw.berlin.steganography.auth.strategy.AuthStrategy;

public class TaskParcel {

    private WeakReference<Context> contextRef;
    private WeakReference<TextView> infoTextRef;
    private AuthStrategy authStrategy;
    private TokenInformation tokenInformation;

    public TaskParcel(Context context, TextView infoTextRef, AuthStrategy authStrategy, TokenInformation tokenInformation){
        this.contextRef = new WeakReference<>(context);
        this.infoTextRef = new WeakReference<>(infoTextRef);
        this.authStrategy = authStrategy;
        this.tokenInformation = tokenInformation;
    }

    public AuthStrategy getAuthStrategy() {
        return authStrategy;
    }

    public TokenInformation getTokenInformation() {
        return tokenInformation;
    }

    public WeakReference<Context> getContextRef() {
        return contextRef;
    }

    public WeakReference<TextView> getInfoTextRef() {
        return infoTextRef;
    }
}

