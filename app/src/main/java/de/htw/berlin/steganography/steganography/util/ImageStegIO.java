/*
 * Copyright (c) 2020
 * Contributed by Henk-Joas Lubig
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htw.berlin.steganography.steganography.util;

import de.htw.berlin.steganography.steganography.image.encoders.BitmapEncoder;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;

public interface ImageStegIO {

    /**
     * <p>Returns the image in its current state (Output-Image) as a byte Array.</p>
     * @return the image in its current state as a byte array
     * @throws ImageWritingException if the image was not written to a byte array for unknown reasons
     */
    byte[] getImageAsByteArray() throws ImageWritingException;

    /**
     * <p>Returns the images format.</p>
     * @return the images format (png, bmp, ...) as a String
     * @throws UnsupportedImageTypeException if the image type read from input is not supported
     * @throws NoImageException if no image could be read from input
     */
    String getFormat() throws UnsupportedImageTypeException, NoImageException;

    /**
     * <p>Determines and returns the suitable encoder (and overlay) for the image according to its type.</p>
     * @param seed to hand to the overlay
     * @return BuffImgEncoder with set PixelCoordinateOverlay, chosen accordingly to the images type
     * @throws UnsupportedImageTypeException if the images type is not supported by any known encoder / overlay
     * @throws NoImageException if no image could be read from input
     */
    BitmapEncoder getEncoder(long seed) throws UnsupportedImageTypeException, NoImageException;

}
