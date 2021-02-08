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

package de.htw.berlin.steganography.steganography.image.overlays;

import java.util.NoSuchElementException;

import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;

/**
 * <p>Classes that implement this interface should be able to return pixels of a
 * given Bitmap or similar image representation in an order independent from the
 * coordinate system of the pixels (to act as an overlay).</p>
 * <p>They are meant to be used by encoding algorithms (BitmapEncoder), so
 * arranging and en- / decoding pixels are separate procedures.</p>
 */
public interface PixelCoordinateOverlay {

    /**
     * Returns the next pixel value as an int (representing ARGB as its bytes),
     * meaning the next pixel determined by the Overlay.
     * @return int representing the next pixel by the Overlay.
     * @throws NoSuchElementException if there is no next pixel
     */
    int next() throws NoSuchElementException;

    /**
     * Sets the current pixel to the given value and checks if the correct value was actually
     * set by the Bitmap. This is due to inaccuracies of Bitmap while trying to set a pixel to a
     * specific value.
     * @param value the value to set the current pixel to
     * @throws NoSuchElementException if setPixel() is called before the first call to next().
     * Or if setPixel() is called after the last call to next() produced a NoSuchElementException
     * @throws BitmapInaccuracyException if the desired value was not set correctly by bitmap
     */
    void setPixel(int value) throws NoSuchElementException, BitmapInaccuracyException;

    /**
     * Returns the number of remaining pixels (not yet returned by next())
     * @return number of remaining pixels
     */
    int available();
}
