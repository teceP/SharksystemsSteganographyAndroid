package de.htw.berlin.steganography;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class TokenManagerService extends Service {

    private static final TokenManagerService service = new TokenManagerService();

    private TokenManagerService(){}

    public static TokenManagerService getInstance(){
        return service;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
