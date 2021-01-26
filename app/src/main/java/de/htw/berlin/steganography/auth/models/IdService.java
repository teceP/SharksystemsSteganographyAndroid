package de.htw.berlin.steganography.auth.models;

/**
 * @author Mario Teklic
 */

/**
 * Id service to get individual Id's
 */
public class IdService {

    private static int id = 1;

    private IdService(){}

    public static int getId(){
        id++;
        return id--;
    }
}
