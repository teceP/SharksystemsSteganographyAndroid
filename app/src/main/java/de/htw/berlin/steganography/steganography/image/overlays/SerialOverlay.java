/*
 * Copyright (c) 2020
 * Contributed by Henk Lubig
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
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;

public class SerialOverlay implements BufferedImageCoordinateOverlay {

    protected final Bitmap bitmap;
    protected List<Integer> pixelOrder;
    protected int currentPosition = -1;
    protected int currentX = 0;
    protected int currentY = 0;

    protected SerialOverlay(Bitmap bitmap) throws UnsupportedImageTypeException {
        this.bitmap = bitmap;

        Bitmap.Config type = this.bitmap.getConfig();
        if (!this.typeAccepted(type))
            throw new UnsupportedImageTypeException("This overlay doesn't support images of type " + type);
    }

    public SerialOverlay(Bitmap bitmap, long seed) throws UnsupportedImageTypeException {
        this(bitmap);
        createOverlay();
    }

    protected boolean typeAccepted(Bitmap.Config type) {
        return type == Bitmap.Config.ARGB_8888;
    }

    protected void createOverlay() {
        if (this.pixelOrder == null) {
            this.pixelOrder =
                    IntStream.range(0, bitmap.getHeight() * bitmap.getWidth())
                            .boxed()
                            .collect(Collectors.toList());
        }
    }

    @Override
    public int next() throws NoSuchElementException {
        if (++currentPosition >= this.pixelOrder.size())
            throw new NoSuchElementException("No pixels left.");

        this.currentX = this.pixelOrder.get(this.currentPosition) % this.bitmap.getWidth();
        this.currentY = this.pixelOrder.get(this.currentPosition) / this.bitmap.getWidth();
        return this.bitmap.getPixel(this.currentX, this.currentY);
    }

    @Override
    public void setPixel(int value) throws BitmapInaccuracyException {
        if (currentPosition < 0 || this.currentPosition >= this.pixelOrder.size())
            throw new IndexOutOfBoundsException("No pixel at current position.");
        // save 'before' value for logging
        int before = this.bitmap.getPixel(this.currentX, this.currentY);

        // actual change
        this.bitmap.setPixel(this.currentX, this.currentY, value);

        // check 'after' value to see if change worked as intended
        int after = this.bitmap.getPixel(this.currentX, this.currentY);
        // verbose log and exception throwing if change didn't work as intended
        if (value != after) {
            Log.i("Overlay", "-------------Bitmap Pixel Error------------------");
            Log.i("Overlay", "before   :" +
                    " Alpha=" + ((before >> 24) & 0xff) +
                    " Red=" + ((before >> 16) & 0xff) +
                    " Green=" + ((before >> 8) & 0xff) +
                    " Blue=" + (before & 0xff));
            Log.i("Overlay", "should be:" +
                    " Alpha=" + ((value >> 24) & 0xff) +
                    " Red=" + ((value >> 16) & 0xff) +
                    " Green=" + ((value >> 8) & 0xff) +
                    " Blue=" + (value & 0xff));
            Log.i("Overlay", "but is   :" +
                    " Alpha=" + ((after >> 24) & 0xff) +
                    " Red=" + ((after >> 16) & 0xff) +
                    " Green=" + ((after >> 8) & 0xff) +
                    " Blue=" + (after & 0xff));
            Log.i("Overlay", "-------------------------------------------------");

            throw new BitmapInaccuracyException("This image cannot be used for encoding, " +
                    "because Androids Bitmap will not change colors correctly.");
        }
    }

    @Override
    public int available() {
        return this.pixelOrder.size() - this.currentPosition -1;
    }
}

