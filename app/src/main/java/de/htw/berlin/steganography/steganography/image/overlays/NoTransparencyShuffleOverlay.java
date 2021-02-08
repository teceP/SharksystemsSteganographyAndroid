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

import java.util.ArrayList;

import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;

/**
 * This class returns Pixels of the underlying Bitmap in a random order determined by the seed
 * that is given to its constructor. It will only return Pixels with an alpha value of 255.
 */
public class NoTransparencyShuffleOverlay extends ShuffleOverlay {

    /**
     * <p>Creates an Overlay that returns Pixels of the underlying Bitmap in a random order determined by the seed
     * that is given to its constructor.</p>
     * <p>It will only return Pixels with an alpha value of 255.</p>
     * @param bitmap Bitmap to represent the pixels of
     * @param seed Long to be used to affect the randomization of pixelorder.
     * @throws UnsupportedImageTypeException if the images type is not supported by this overlay
     */
    public NoTransparencyShuffleOverlay(Bitmap bitmap, long seed) throws UnsupportedImageTypeException {
        super(bitmap, seed);
    }

    @Override
    protected void initOverlay() {
        this.pixelOrder = new ArrayList<>();
        for(int y = 0; y < this.bitmap.getHeight(); y++) {
            for (int x = 0; x < this.bitmap.getWidth(); x++) {
                int pixel = this.bitmap.getPixel(x, y);
                if(((pixel >> 24) & 0xff) == 0xff)
                    this.pixelOrder.add(x + (y * this.bitmap.getWidth()));
            }
        }
    }
}
