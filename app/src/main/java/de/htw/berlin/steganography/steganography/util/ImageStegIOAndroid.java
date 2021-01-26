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

import de.htw.berlin.steganography.steganography.image.encoders.BuffImgEncoder;
import de.htw.berlin.steganography.steganography.image.encoders.PixelBit;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;
import de.htw.berlin.steganography.steganography.image.overlays.BufferedImageCoordinateOverlay;
import de.htw.berlin.steganography.steganography.image.overlays.NoTransparencyShuffleOverlay;
import de.htw.berlin.steganography.steganography.image.overlays.ShuffleOverlay;

public class ImageStegIOAndroid implements ImageStegIO{

    private final byte[] input;
    private final boolean useTransparent;

    private Bitmap bitmap;

    private String format;

    private static final String TAG = "ImageStegIOAndroid";

    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(
            Arrays.asList("image/bmp", "image/gif", "image/png")
    );

    public ImageStegIOAndroid(byte[] image)
            throws UnsupportedImageTypeException, NoImageException {

        this.input = image;
        this.useTransparent = false;
        processImage(this.input);
    }

    /**
     * This constructor has no use in the current state and does the same as ImageSteg(byte[] image).
     * Transparent pixels lead to inaccuracies in the Android class Bitmap and can therefore not be
     * used.
     * @param image the image tp process
     * @param useTransparent always false
     * @throws UnsupportedImageTypeException
     * @throws NoImageException
     */
    public ImageStegIOAndroid(byte[] image, boolean useTransparent)
            throws UnsupportedImageTypeException, NoImageException {

        this(image);
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

        // TODO: Probably not necessary -> would only be compressed to PNG
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
        //////////////////////////////////////////////////////////////////

        // logging and possible setting of ColorSpace to make algorithm work
        if (!this.bitmap.getColorSpace().isSrgb()) {
            Log.i(TAG, "setColorSpace:  the Images ColorSpace is: " +
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
     * Returns the image in its current state (Output-Image) as a Byte Array. This Class can
     * only return images as PNG-files.
     * @throws ImageWritingException if the image could not be written
     */
    @Override
    public byte[] getImageAsByteArray() throws ImageWritingException {

        ByteArrayOutputStream resultImage = new ByteArrayOutputStream();

        if (!this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, resultImage)) {
            throw new ImageWritingException("Could not write image. Unknown, internal error");
        }

        return resultImage.toByteArray();
    }

    @Override
    public String getFormat() {
        return this.format;
    }

    /**
     * Determines and returns the suitable encoder (and overlay) for the given bufferedImage according to its type.
     * In the current state, this will always be the PixelBit encoder with an overlay that only
     * uses pixels with an alpha value of 255.
     * @param seed to hand to the overlay
     * @return BuffImgEncoder with set BufferedImageCoordinateOverlay, chosen accordingly to the images type
     * @throws UnsupportedImageTypeException if the images type is not supported by any known encoder / overlay
     */
    @Override
    public BuffImgEncoder getEncoder(long seed)
            throws UnsupportedImageTypeException {

        Bitmap.Config type = bitmap.getConfig();

        if (type != Bitmap.Config.ARGB_8888)
            throw new UnsupportedImageTypeException(
                    "Image type (BufferedImage.TYPE = " + type + ") is not supported"
            );
        else
            return new PixelBit(getOverlay(this.bitmap, seed));
    }

    /**
     * Returns overlay according to global variable useTransparent. In the current state, this will
     * always be an overlay that only uses pixels with an alpha value of 255.
     * @param bitmap Bitmap to hand to overlay
     * @param seed Seed to hand to overlay
     * @return ShuffleOverlay or NoTransparencyShuffleOverlay
     * @throws UnsupportedImageTypeException if the image type is not supported by the overlay
     */
    private BufferedImageCoordinateOverlay getOverlay(Bitmap bitmap, long seed)
            throws UnsupportedImageTypeException {

        return this.useTransparent ?
                new ShuffleOverlay(bitmap, seed) :
                new NoTransparencyShuffleOverlay(bitmap, seed);
    }
}
