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
import android.util.Log;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;

/**
 * This class returns Pixels of the underlying Bitmap in order from top left (x=0, y=0)
 * to bottom right (x=bitmap.getWidth(), y=bitmap.getHeight()).
 */
public class SequenceOverlay implements PixelCoordinateOverlay {

    protected final Bitmap bitmap;
    protected List<Integer> pixelOrder;
    protected int currentPosition = -1;
    protected int currentX = 0;
    protected int currentY = 0;

    /**
     * Creates a SequenceOverlay that returns Pixels of the underlying Bitmap in order from top left (x=0, y=0)
     * to bottom right (x=bitmap.getWidth(), y=bitmap.getHeight()).
     * @param bitmap the Bitmap to represent the pixels of.
     * @throws UnsupportedImageTypeException if the type of image is not supported by this overlay
     */
    public SequenceOverlay(Bitmap bitmap) throws UnsupportedImageTypeException {
        this.bitmap = bitmap;

        Bitmap.Config type = this.bitmap.getConfig();
        if (!this.typeAccepted(type))
            throw new UnsupportedImageTypeException("This overlay doesn't support images of type " + type);
    }

    /**
     * <p>Checks whether the type of the given image is accepted by this overlay.</p>
     * <p>Overwritten by subclasses to apply their own rules for acceptance.</p>
     * @param type representation of an image type as an int of Bitmap.Config
     * @return true if the images type is accepted by this overlay
     */
    protected boolean typeAccepted(Bitmap.Config type) {
        return type == Bitmap.Config.ARGB_8888;
    }

    /**
     * <p>Creates the overlay as an independent method to address pixels without using
     * Bitmaps coordinates.</p>
     * <p>Subclasses overwrite this method to use their own logic of creating the overlay.</p>
     */
    protected void createOverlay() {
        this.pixelOrder =
                IntStream.range(0, bitmap.getHeight() * bitmap.getWidth())
                        .boxed()
                        .collect(Collectors.toList());
    }

    @Override
    public int next() throws NoSuchElementException {
        if (this.pixelOrder == null)
            createOverlay();

        if (++currentPosition >= this.pixelOrder.size())
            throw new NoSuchElementException("No pixels left.");

        this.currentX = this.pixelOrder.get(this.currentPosition) % this.bitmap.getWidth();
        this.currentY = this.pixelOrder.get(this.currentPosition) / this.bitmap.getWidth();
        return this.bitmap.getPixel(this.currentX, this.currentY);
    }

    @Override
    public void setPixel(int value) throws BitmapInaccuracyException {
        if (this.pixelOrder == null)
            createOverlay();

        if (currentPosition < 0 || this.currentPosition >= this.pixelOrder.size())
            throw new IndexOutOfBoundsException("No pixel at current position.");

        int before = this.bitmap.getPixel(this.currentX, this.currentY);

        this.bitmap.setPixel(this.currentX, this.currentY, value);
        int changedTo = this.bitmap.getPixel(this.currentX, this.currentY);

        if (value != changedTo) {
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
                    " Alpha=" + ((changedTo >> 24) & 0xff) +
                    " Red=" + ((changedTo >> 16) & 0xff) +
                    " Green=" + ((changedTo >> 8) & 0xff) +
                    " Blue=" + (changedTo & 0xff));
            Log.i("Overlay", "-------------------------------------------------");

            throw new BitmapInaccuracyException("This image cannot be used for encoding, " +
                    "because Androids Bitmap will not change its colors correctly.");
        }
    }

    @Override
    public int available() {
        if (this.pixelOrder == null)
            createOverlay();

        return this.pixelOrder.size() - this.currentPosition -1;
    }
}

