package de.htw.berlin.steganography.steganography.image.exceptions;

import de.htw.berlin.steganography.socialmediasteganography.SocialMediaSteganographyException;

/**
 * <p>Thrown if inaccuracies are detected while trying to set a pixels value.</p>
 * <p>Under some conditions, Androids Bitmap will not set pixels exactly to the desired value.
 * Because this would make decoding very unreliable, this exception is thrown whenever detecting
 * inaccuracies while encoding.</p>
 */
public class BitmapInaccuracyException extends SocialMediaSteganographyException {

    public BitmapInaccuracyException() {
        super();
    }

    public BitmapInaccuracyException(String message) {
        super(message);
    }
}
