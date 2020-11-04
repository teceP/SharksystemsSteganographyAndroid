package de.htw.berlin.steganography.auth.models;

public enum Networks {
    REDDIT("reddit"),
    IMGUR("imgur"),
    TWITTER("imgur"),
    YOUTUBE("youtube"),
    INSTAGRAM("instagram");

    private final String identifier;

    Networks(String id){
        this.identifier = id;
    }
}
