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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.htw.berlin.steganography.steganography.image.encoders.BitmapEncoder;
import de.htw.berlin.steganography.steganography.image.encoders.PixelBit;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;
import de.htw.berlin.steganography.steganography.image.overlays.NoTransparencyShuffleOverlay;
import de.htw.berlin.steganography.steganography.image.overlays.PixelCoordinateOverlay;
import de.htw.berlin.steganography.steganography.image.overlays.ShuffleOverlay;

public class ImageStegIOAndroid implements ImageStegIO{

    protected final byte[] input;
    protected final boolean useTransparent;

    private Bitmap bitmap;

    private String format;

    private static final String TAG = "ImageStegIOAndroid";

    /**
     * List of supported input image formats.
     */
    protected static final Set<String> SUPPORTED_FORMATS = new HashSet<>(
            // Accepting only PNG is a design decision. You could accept a multitude of formats,
            // but Bitmap can only output PNG and WEBP_LOSSLESS as lossless formats.
            Arrays.asList(/*"image/bmp", "image/gif",*/ "image/png")
    );

    /**
     * <p>Creates an object that exists to handle reading and writing of Bitmaps to and from byte arrays
     * as well as choosing the appropriate encoders (and their overlays) for the given image.
     * It holds on to the image during its en- or decoding.</p>
     * <p>The image will only be processed if the methods getFormat() or getEncoder() are called.</p>
     * @param image the image to handle In- and Output of
     * @param useTransparent ignored (always false) due to more frequent Bitmap inaccuracies
     *                       whenever transparent pixels are involved
     */
    public ImageStegIOAndroid(byte[] image, boolean useTransparent) {
        this.input = image;
        this.useTransparent = false;
    }

    private void processImage(byte[] carrier)
            throws UnsupportedImageTypeException, NoImageException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        this.bitmap = BitmapFactory.decodeByteArray(carrier, 0, carrier.length, options);

        if (this.bitmap == null)
            throw new NoImageException("No Image could be read from carrier.");

        this.format = options.outMimeType;
        Log.i(TAG, "image format: " + this.format);

        if (this.format == null) {
            throw new UnsupportedImageTypeException(
                    "The Image format is unknown and cannot be supported."
            );
        }

        if (!isFormatSupported(this.format)) {
            throw new UnsupportedImageTypeException(
                    "The Image format (" +
                            this.format +
                            ") is not supported."
            );
        }

        // logging and possible changing of ColorSpace to sRGB to make algorithm work
        if (!this.bitmap.getColorSpace().isSrgb()) {
            Log.i(TAG, "setColorSpace: the Images ColorSpace is: " +
                    this.bitmap.getColorSpace() +
                    ". Trying to set to sRGB.");
            setColorSpace();
        }
    }

    private void setColorSpace() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.bitmap.setColorSpace(ColorSpace.get(ColorSpace.Named.SRGB));
        } else {
            Log.i(TAG, "processImage: AndroidVersion too low to set ColorSpace:" +
                    " is: " + Build.VERSION.SDK_INT +
                    " // " +
                    " needed: " + Build.VERSION_CODES.Q);
            Log.i(TAG, "setColorSpace: Setting ColorSpace to sRGB failed, " +
                    "hiding or getting secret message will probably not work.");
        }
    }

    private boolean isFormatSupported(String formatName) {
        return SUPPORTED_FORMATS.contains(formatName);
    }

    /**
     * <p>Returns the image in its current state (Output-Image) as a byte Array.</p>
     * <p>If the image was not yet processed, return == input</p>
     * @return the image in its current state as a byte array
     * @throws ImageWritingException if the image was not written to a byte array for unknown reasons
     */
    @Override
    public byte[] getImageAsByteArray() throws ImageWritingException {
        if (this.bitmap == null)
            return input;

        ByteArrayOutputStream resultImage = new ByteArrayOutputStream();

        if (!this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, resultImage)) {
            throw new ImageWritingException("Could not write image. Unknown, internal error");
        }

        return resultImage.toByteArray();
    }

    /**
     * <p>Returns the images format.</p>
     * <p>Processes the image if necessary.</p>
     * @return the images format (png, bmp, ...) as a String
     * @throws UnsupportedImageTypeException if the image type read from input is not supported
     * @throws NoImageException if no image could be read from input
     */
    @Override
    public String getFormat() throws NoImageException, UnsupportedImageTypeException {
        if (this.bitmap == null)
            processImage(this.input);

        return this.format;
    }

    /**
     * <p>Determines and returns the suitable encoder (and overlay) for the image according to its type.</p>
     * <p>In the current state, this will always be the PixelBit encoder with an overlay that only
     * uses pixels with an alpha value of 255.</p>
     * <p>Processes the image if it was not processed already.</p>
     * @param seed to hand to the overlay
     * @return BuffImgEncoder with set PixelCoordinateOverlay, chosen accordingly to the images type
     * @throws UnsupportedImageTypeException if the images type is not supported by any known encoder / overlay
     * @throws NoImageException if no image could be read from input
     */
    @Override
    public BitmapEncoder getEncoder(long seed)
            throws UnsupportedImageTypeException, NoImageException {
        if (this.bitmap == null)
            processImage(this.input);

        Bitmap.Config type = bitmap.getConfig();

        if (type != Bitmap.Config.ARGB_8888)
            throw new UnsupportedImageTypeException(
                    "Image type (BufferedImage.TYPE = " + type + ") is not supported"
            );
        else
            return new PixelBit(getOverlay(this.bitmap, seed));
    }

    /**
     * Returns an overlay according to global variable useTransparent.
     * @param bitmap Bitmap to hand to overlay
     * @param seed Seed to hand to overlay
     * @return ShuffleOverlay or NoTransparencyShuffleOverlay
     * @throws UnsupportedImageTypeException if the image type is not supported by the overlay
     */
    protected PixelCoordinateOverlay getOverlay(Bitmap bitmap, long seed)
            throws UnsupportedImageTypeException {

        return this.useTransparent ?
                new ShuffleOverlay(bitmap, seed) :
                new NoTransparencyShuffleOverlay(bitmap, seed);
    }
}
