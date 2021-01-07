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

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public SerialOverlay(Bitmap bitmap, long seed) throws UnsupportedImageTypeException {
        this(bitmap);
        createOverlay();
    }

    protected boolean typeAccepted(Bitmap.Config type) {
        return type == Bitmap.Config.ARGB_8888;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
    public void setPixel(int value) {
        if (currentPosition < 0 || this.currentPosition >= this.pixelOrder.size())
            throw new IndexOutOfBoundsException("No pixel at current position.");
        int getColor = (this.bitmap.getPixel(this.currentX, this.currentY));
        this.bitmap.setPixel(this.currentX, this.currentY, (value));
    }

    @Override
    public int available() {
        return this.pixelOrder.size() - this.currentPosition -1;
    }
}

