package de.htw.berlin.steganography.auth;

import java.util.HashMap;

import de.htw.berlin.steganography.auth.models.AuthInformation;

/**
 * @author Mario Teklic
 */

public class InformationHolder {

    private HashMap<String, AuthInformation> map;

    public InformationHolder(){
        this.map = new HashMap<>();
    }

    public void put(String service, AuthInformation authInformation){
        this.map.put(service, authInformation);
    }

    public AuthInformation get(String service){
        return this.map.get(service);
    }

}
