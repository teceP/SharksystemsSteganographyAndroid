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


public class ShuffleOverlay extends SerialOverlay {

    protected Random random;

    protected ShuffleOverlay(Bitmap bitmap) throws UnsupportedImageTypeException {
        super(bitmap);
    }

    public ShuffleOverlay(Bitmap bitmap, long seed) throws UnsupportedImageTypeException {
        super(bitmap);

        this.random = new Random(seed);
        createOverlay();
        shufflePixelOrder();
    }

    protected void createOverlay() {
        super.createOverlay();
    }

    private void shufflePixelOrder() {
        Collections.shuffle(this.pixelOrder, this.random);
    }
}
