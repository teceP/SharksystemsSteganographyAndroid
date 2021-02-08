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

import android.graphics.Bitmap;

import java.util.Collections;
import java.util.Random;

import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;

/**
 * This class returns Pixels of the underlying Bitmap in a random order determined by the seed
 * that is given to its constructor
 */
public class ShuffleOverlay extends SequenceOverlay {

    protected Random random;

    /**
     * Creates a ShuffleOverlay that returns Pixels of the underlying Bitmap in a random order
     * determined by the seed that is given to its constructor.
     * @param bitmap Bitmap to represent the pixels of
     * @param seed Long to be used to affect the randomization of pixelorder.
     * @throws UnsupportedImageTypeException if the images type is not supported by this overlay
     */
    public ShuffleOverlay(Bitmap bitmap, long seed) throws UnsupportedImageTypeException {
        super(bitmap);
        this.random = new Random(seed);
    }

    /**
     * <p>Creates the overlay as an independent method to address pixels without using
     * Bitmaps coordinates. Uses two protected methods to separate the creation
     * of the overlay from its randomization.</p>
     * <p>Subclasses should only overwrite this method to alter this separation.</p>
     */
    @Override
    protected void createOverlay() {
        initOverlay();
        shufflePixelOrder();
    }

    /**
     * <p>Initialization of the overlay. This process is separate from the randomization
     * of the overlay.</p>
     */
    protected void initOverlay() {
        super.createOverlay();
    }

    /**
     * <p>Randomization of the already created overlay.</p>
     */
    protected void shufflePixelOrder() {
        Collections.shuffle(this.pixelOrder, this.random);
    }
}
