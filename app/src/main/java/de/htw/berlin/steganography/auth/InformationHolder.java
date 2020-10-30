package de.htw.berlin.steganography.auth;

import java.util.HashMap;

public class InformationHolder {

    private HashMap<String, Information> map;

    public InformationHolder(){
        this.map = new HashMap<>();
    }

    public void put(String service, Information information){
        this.map.put(service, information);
    }

    public Information get(String service){
        return this.map.get(service);
    }

}
