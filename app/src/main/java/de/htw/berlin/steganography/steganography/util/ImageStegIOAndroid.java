package de.htw.berlin.steganography.steganography.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.htw.berlin.steganography.steganography.image.encoders.BuffImgEncoder;
import de.htw.berlin.steganography.steganography.image.encoders.PixelBit;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;
import de.htw.berlin.steganography.steganography.image.overlays.BufferedImageCoordinateOverlay;
import de.htw.berlin.steganography.steganography.image.overlays.RemoveTransparentShuffleOverlay;
import de.htw.berlin.steganography.steganography.image.overlays.ShuffleOverlay;

public class ImageStegIOAndroid implements ImageStegIO{

    private final byte[] input;
    private final boolean useTransparent;

    private Bitmap bitmap;

    private String format;

    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(
            Arrays.asList("image/bmp", "image/gif", "image/png")
    );

    public ImageStegIOAndroid(byte[] image)
            throws UnsupportedImageTypeException, IOException, NoImageException {

        this.input = image;
        this.useTransparent = false;
        processImage(this.input);
    }

    public ImageStegIOAndroid(byte[] image, boolean useTransparent)
            throws UnsupportedImageTypeException, IOException, NoImageException {

        this.input = image;
        this.useTransparent = useTransparent;
        processImage(this.input);
    }

    private void processImage(byte[] carrier)
            throws IOException, UnsupportedImageTypeException {

        /* Option A
        this.bitmap = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(ByteBuffer.wrap(carrier)),
                // listener to get mime type
                (decoder, info, source) -> {
                    this.format = info.getMimeType();
                }
        );
        */

        // TODO: Probably not necessary -> would only be compressed to PNG
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inMutable = true;

        this.format = options.outMimeType;
        //////////////////////////////////////////////////////////////////

        this.bitmap = BitmapFactory.decodeByteArray(carrier, 0, carrier.length, options);

        // TODO: Probably not necessary -> would only be compressed to PNG
        if (this.format == null)
            throw new UnsupportedImageTypeException(
                    "The Image format is unknown and cannot be supported."
            );

        if (!isFormatSupported(this.format))
            throw new UnsupportedImageTypeException(
                    "The Image format (" +
                            this.format +
                            ") is not supported."
            );
        //////////////////////////////////////////////////////////////////
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
     * Returns overlay according to global variable useTransparent
     * @param bitmap Bitmap to hand to overlay
     * @param seed Seed to hand to overlay
     * @return ShuffleOverlay or RemoveTransparentShuffleOverlay
     * @throws UnsupportedImageTypeException if the image type is not supported by the overlay
     */
    private BufferedImageCoordinateOverlay getOverlay(Bitmap bitmap, long seed)
            throws UnsupportedImageTypeException {

        return this.useTransparent ?
                new ShuffleOverlay(bitmap, seed) :
                new RemoveTransparentShuffleOverlay(bitmap, seed);
    }
}
