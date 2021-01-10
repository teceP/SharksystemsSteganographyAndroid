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

package de.htw.berlin.steganography.steganography.image.encoders;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.htw.berlin.steganography.steganography.image.exceptions.ImageCapacityException;
import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;
import de.htw.berlin.steganography.steganography.image.overlays.BufferedImageCoordinateOverlay;


public class PixelBit extends BuffImgEncoder {
    private int numOfChannels = 3;

    public PixelBit(BufferedImageCoordinateOverlay overlay) throws IllegalArgumentException {
        super(overlay);
    }

    /**
     * Returns the number of color channels this algorithm is currently choosing from
     * to encode data. Cannot be greater than 4 or smaller than 1.
     * @return the number of color channels currently used
     */
    public int getNumberOfChannels() {
        return this.numOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        if (numberOfChannels > 4 || numberOfChannels < 1)
            throw new IllegalArgumentException("Number of channels can only be a number between " +
                    "1 (inclusive) and 4 (inclusive)");
        this.numOfChannels = numberOfChannels;
    }

    @Override
    public void encode(byte[] payload) throws ImageCapacityException, BitmapInaccuracyException {
        if ((payload.length * 8) > overlay.available()) {
            StringBuilder sb = new StringBuilder("More Bits of payload (")
                .append(payload.length * 8)
                .append(") than pixels available (")
                .append(this.overlay.available()).append(")");
            throw new ImageCapacityException(sb.toString());
        }

        // TODO: this must be determined by some algorithm
        for (byte bite : payload) {
            for (int bitNo = 7; bitNo >= 0; bitNo--) {
                // turn bit to boolean (0 or 1) -> true if 1
                boolean bitIsOne = (bite & (1 << bitNo)) > 0;
                // get current pixel
                int pixelARGB = this.overlay.next();
                // true if pixel represents 1
                boolean pixelIsOne = pixelIsOne(pixelARGB);
                // if payload bit != pixelBit -> flip pixelBit
                if (bitIsOne != pixelIsOne)
                    this.overlay.setPixel(changePixelValue(pixelARGB));
            }
        }
    }

    @Override
    public byte[] decode(int bLength) {

        if (bLength > this.overlay.available() / 8)
            throw new IndexOutOfBoundsException("bLength cannot be greater than the images capacity of " +
                    this.overlay.available() / 8 + " bytes");

        // true = 1; false = 0;
        List<Boolean> pixelBitList = new ArrayList<>();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (int i = 0; i < bLength; i++) {
            for (int j = 0; j < 8; j++) {
                pixelBitList.add(pixelIsOne(this.overlay.next()));
            }
            result.write(
                    bits2Byte(
                            pixelBitList.toArray(new Boolean[0])
                    )
            );
            pixelBitList.clear();
        }
        return result.toByteArray();
    }

    /**
     * Turns an array of 8 booleans into a byte
     * @param pixelByte
     * @return
     */
    private byte bits2Byte(Boolean[] pixelByte) {
        if (pixelByte.length != 8)
            throw new ArrayIndexOutOfBoundsException("bits2byte: Array must have length exactly 8");

        int result = 0;
        for (Boolean pixelBit : pixelByte) {
            result = (result << 1);
            if (pixelBit) // short statement?
                result = (result | 1);
        }
        // for safety, byte-cast should be enough
        return (byte) (result & 0xff);
    }

    /**
     * In this algorithm, if the return of this function is true, the given pixel represents a bit-value of 1.
     * If it is false, the pixel represents a bit-value of 0.<br/><br/>
     * Returns true, if the sum of the individual bytes of pixelARGB is an uneven number.<br/>
     * Differently put: It determines whether the amount of 1's in the least significant bits
     * of each individual byte of pixelARGB is uneven.
     * @param pixelARGB pixel that represents a bit.
     * @return true if the given pixel represents a 1 bit.
     */
    public static boolean pixelIsOne(int pixelARGB) {
        return (
                (pixelARGB & 1) ^
                (pixelARGB >> 8 & 1) ^
                (pixelARGB >> 16 & 1) ^
                (pixelARGB >> 24 & 1)
        ) > 0;
    }

    /**
     * Changes the value of a random color channel (ARGB) of the given pixel
     * by +1 or -1 (randomly, but w/o overflow).<br/><br/>
     * Since a pixel represents a bit, this method "flips" it.
     * (By changing the outcome of (A+R+G+B) & 1 == 0)
     * @param pixelARGB the pixelValue to change
     */
    protected int changePixelValue(int pixelARGB) {
        Random rng = new Random();

        // pick random channel
        int channelPick = rng.nextInt(this.numOfChannels) * 8;
        // extract the byte of picked channel
        int channel = ((pixelARGB >> channelPick) & 0xff);

        // check if addition or subtraction would cause overflow
        // and prevent it
        int addition;
            // if all bits are 1, subtract 1
        if ((channel & 0xff) == 0xff) {
            addition = -1;
            // if all bits are 0, add 1
        } else if (channel == 0) {
            addition = 1;
        } else {
            // if there is no overflow add or subtract 1 at random
            addition = (rng.nextBoolean() ? 1 : -1);
        }
        channel += addition;

        // put modified byte back to its place in the int
        return (pixelARGB | (0xff << channelPick)) & ~((~channel & 0xff) << channelPick);
        // overwrite previous picked byte in original int (pxInt) with 1111 1111
        // invert channel, position it in another int and invert again -> 11..channel..11
        // bitwise AND replaces old byte with channel and keeps the rest of pxInt
    }
}
