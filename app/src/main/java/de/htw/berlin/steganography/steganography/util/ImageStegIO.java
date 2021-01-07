package de.htw.berlin.steganography.steganography.util;

import java.io.IOException;

import de.htw.berlin.steganography.steganography.image.encoders.BuffImgEncoder;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;

public interface ImageStegIO {

    /**
     * Returns the image in its current state (Output-Image) as a Byte Array
     */
    byte[] getImageAsByteArray() throws IOException, ImageWritingException;

    /**
     * Returns the format of the image
     */
    String getFormat() throws UnsupportedImageTypeException, IOException, NoImageException;

    /**
     * Returns an appropriate encoder to encode the image with
     */
    BuffImgEncoder getEncoder(long seed) throws UnsupportedImageTypeException;

}
