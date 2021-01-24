package de.htw.berlin.steganography.steganography.exceptions;

import de.htw.berlin.steganography.socialmediasteganography.SocialMediaSteganographyException;

/**
 * Thrown if an operation was attempted on a media it doesn't support.
 */
public class UnsupportedMediaTypeException extends SocialMediaSteganographyException {

    public UnsupportedMediaTypeException() {
        super();
    }

    public UnsupportedMediaTypeException(String message) {
        super(message);
    }
}
