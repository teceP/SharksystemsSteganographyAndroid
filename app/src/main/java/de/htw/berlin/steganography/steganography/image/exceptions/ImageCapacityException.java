package de.htw.berlin.steganography.steganography.image.exceptions;

import de.htw.berlin.steganography.steganography.exceptions.MediaCapacityException;

/**
 * <p>Thrown if the images usable pixels determined by an overlay are too few to support the attempted operation.</p>
 * <p>Usually thrown if the payload has more bits than the image has usable pixels.</p>
 */
public class ImageCapacityException extends MediaCapacityException {

    public ImageCapacityException() {
        super();
    }

    public ImageCapacityException(String message) {
        super(message);
    }
}
