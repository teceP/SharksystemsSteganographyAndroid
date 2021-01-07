package de.htw.berlin.steganography.steganography.image.exceptions;

import de.htw.berlin.steganography.steganography.exceptions.MediaReassemblingException;

/**
 * Thrown if an image could not be reassembled successfully after encoding a message in it.
 */
public class ImageWritingException extends MediaReassemblingException {
    public ImageWritingException() {
        super();
    }

    public ImageWritingException(String message) {
        super(message);
    }
}
