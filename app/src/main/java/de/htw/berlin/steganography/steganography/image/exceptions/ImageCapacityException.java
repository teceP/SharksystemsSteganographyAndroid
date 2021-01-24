package de.htw.berlin.steganography.steganography.image.exceptions;

import de.htw.berlin.steganography.steganography.exceptions.MediaCapacityException;

public class ImageCapacityException extends MediaCapacityException {

    public ImageCapacityException() {
        super();
    }

    public ImageCapacityException(String message) {
        super(message);
    }
}
