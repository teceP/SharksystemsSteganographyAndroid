package de.htw.berlin.steganography.auth.models;

public class IdService {

    private static int id = 1;

    private IdService(){}

    public static int getId(){
        id++;
        return id--;
    }
}
