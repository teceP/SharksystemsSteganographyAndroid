package de.htw.berlin.steganography.steganography.exceptions;


import de.htw.berlin.steganography.socialmediasteganography.SocialMediaSteganographyException;

/**
 * Thrown if the attempt to read a certain type of media failed.
 */
public class MediaNotFoundException extends SocialMediaSteganographyException {

    public MediaNotFoundException() {
        super();
    }

    public MediaNotFoundException(String message) {
        super(message);
    }
}
