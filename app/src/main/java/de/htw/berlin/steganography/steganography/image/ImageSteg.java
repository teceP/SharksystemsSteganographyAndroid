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

package de.htw.berlin.steganography.steganography.image;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.htw.berlin.steganography.steganography.Steganography;
import de.htw.berlin.steganography.steganography.exceptions.UnknownStegFormatException;
import de.htw.berlin.steganography.steganography.image.encoders.BuffImgEncoder;
import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageCapacityException;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;
import de.htw.berlin.steganography.steganography.util.ImageStegIO;
import de.htw.berlin.steganography.steganography.util.ImageStegIOAndroid;

public class ImageSteg implements Steganography {

    public static final long DEFAULT_SEED = 1732341558;
    private static final int HEADER_SIGNATURE = 1349075561;
    private final boolean useTransparent;
    private final boolean useDefaultHeader;

    private static final Set<String> supportedFormats = new HashSet<>(
            Arrays.asList("bmp", "BMP", "gif", "GIF", "png", "PNG")
    );

    /**
     * Creates a new ImageSteg with settings:
     * <ul>
     *     <li>useDefaultHeader = true</li>
     *     <li>useTransparent = false</li>
     * </ul>
     *
     * This means, a default header will be encoded in the image to simplify decoding and
     * fully transparent pixels will not be used for encoding or decoding.
     *
     * Is equivalent to ImageSteg(true, false).
     */
    public ImageSteg() {
        this.useDefaultHeader = true;
        this.useTransparent = false;
    }

    /**
     * Creates a new ImageSteg with the given settings.
     * <ul>
     *     <li>
     *         useDefaultHeader - <br/>
     *         if true, the default header will be encoded in the image. The hidden message can then be
     *         decoded using ImageSteg.decode(...). <br/>
     *         if false, no header will be encoded in the image. The hidden message can only be decoded
     *         using ImageSteg.decodeRaw(length, ...)
     *     </li>
     *     <li>
     *         useTransparent - <br/>
     *         if true, fully transparent pixels will be used for encoding and decoding <br/>
     *         if false, fully transparent pixels will not be used for encoding and decoding <br/>
     *         This value must be equal while encoding and decoding to successfully decode the hidden message.
     *         This value can only affect PNGs that contain fully transparent pixels.
     *         If an image has no fully transparent pixels, this value will be ignored.
     *         If the image is a GIF, this value will be ignored.
     *         BMPs with transparent pixels are not supported by this class.
     *     </li>
     * </ul>
     * @param useDefaultHeader should the default header be used for encoding?
     * @param useTransparent should fully transparent pixels be used for encoding and decoding?
     */
    public ImageSteg(boolean useDefaultHeader, boolean useTransparent) {
        this.useDefaultHeader = useDefaultHeader;
        this.useTransparent = useTransparent;
    }

    // All formats: JPG, jpg, tiff, bmp, BMP, gif, GIF, WBMP, png, PNG, JPEG, tif, TIF, TIFF, wbmp, jpeg

    // @Override
    // public void useDefaultHeader(boolean useDefaultHeader) {
    //     // TODO: Might be problematic decoding, length has to be given from user
    //     // this.useDefaultHeader = useDefaultHeader;
    // }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload)
            throws IOException, UnsupportedImageTypeException, NoImageException,
            ImageWritingException, ImageCapacityException, BitmapInaccuracyException {

        return encode(carrier, payload, DEFAULT_SEED);
    }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException,
            ImageWritingException, ImageCapacityException, BitmapInaccuracyException {

        if (carrier == null)
            throw new NullPointerException("Parameter 'carrier' must not be null");
        if (payload == null)
            throw new NullPointerException("Parameter 'payload' must not be null");

        ImageStegIO imageStegIO = new ImageStegIOAndroid(carrier, this.useTransparent);

        BuffImgEncoder encoder = imageStegIO.getEncoder(seed);

        if (this.useDefaultHeader) {
            encoder.encode(int2bytes(HEADER_SIGNATURE));
            encoder.encode(int2bytes(payload.length));
        }
        encoder.encode(payload);

        return imageStegIO.getImageAsByteArray();
    }

    @Override
    public byte[] decode(byte[] steganographicData)
            throws IOException, UnsupportedImageTypeException, NoImageException, UnknownStegFormatException {

        return decode(steganographicData, DEFAULT_SEED);
    }

    /**
     * Retrieves hidden message from a steganographic file. This method will fail, if the message
     * was hidden without using the default header. Use ImageSteg.decodeRaw() for this purpose.
     * Reasons for failing with an UnknownStegFormatExceptions are:
     * <ul>
     *      <li>there is no hidden message</li>
     *      <li>the message was hidden with 'useDefaultHeader = false'</li>
     *      <li>the value for 'useTransparent' was different when hiding the message</li>
     *      <li>the message was hidden using an unknown algorithm</li>
     * </ul>
     * @param steganographicData Data containing data to extract
     * @param seed seed that was used to encode the given stenographicData
     * @return
     * @throws IOException if an error occurs during reading 'steganographicData'
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     * @throws UnknownStegFormatException if the default header could not be found
     */
    @Override
    public byte[] decode(byte[] steganographicData, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException, UnknownStegFormatException {

        if (steganographicData == null)
            throw new NullPointerException("Parameter 'steganographicData' must not be null");

        ImageStegIO imageStegIO = new ImageStegIOAndroid(steganographicData, this.useTransparent);

        BuffImgEncoder encoder = imageStegIO.getEncoder(seed);

        // decode 4 bytes and compare them to header signature
        if (bytesToInt(encoder.decode(4)) != HEADER_SIGNATURE) {
            throw new UnknownStegFormatException("No steganographic encoding found.");
        }

        // decode the next 4 bytes to get the amount of bytes to read
        int length = bytesToInt(encoder.decode(4));

        return encoder.decode(length);
    }

    /**
     * Retrieves hidden message from a steganographic file. This method will not search for a header
     * or validate the retrieved data in any form. If 'steganographicData' contains a supported image,
     * this method will always return a result. Whether this result is the hidden message, depends on the
     * settings used:
     * <ul>
     *     <li>'useTransparent' during encoding == 'useTransparent' during decoding</li>
     *     <li>'payload.length' during encoding == 'length' during decoding</li>
     *     <li>No seed used during encoding (thereby using ImageSteg.DEFAULT_SEED)</li>
     *     <li>'useDefaultHeader' == false during encoding</li>
     * </ul>
     * @param length Length (in bytes) of the hidden message
     * @param steganographicData Data containing data to extract
     * @return
     * @throws IOException if an error occurs during reading 'steganographicData'
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     */
    public byte[] decodeRaw(int length, byte[] steganographicData)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        return decodeRaw(length, steganographicData, DEFAULT_SEED);
    }

    /**
     * Retrieves hidden message from a steganographic file. This method will not search for a header
     * or validate the retrieved data in any form. If 'steganographicData' contains a supported image,
     * this method will always return a result. Whether this result is the hidden message, depends on the
     * settings used:
     * <ul>
     *     <li>'useTransparent' during encoding == 'useTransparent' during decoding</li>
     *     <li>'payload.length' during encoding == 'length' during decoding</li>
     *     <li>'seed' during encoding == 'seed' during decoding</li>
     *     <li>'useDefaultHeader' == false during encoding</li>
     * </ul>
     * @param length Length (in bytes) of the hidden message
     * @param steganographicData Data containing data to extract
     * @param seed seed that was used to encode the given stenographicData
     * @return
     * @throws IOException if an error occurs during reading 'steganographicData'
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     */
    public byte[] decodeRaw(int length, byte[] steganographicData, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        if (steganographicData == null)
            throw new NullPointerException("Parameter 'steganographicData' must not be null");

        ImageStegIO imageStegIO = new ImageStegIOAndroid(steganographicData, this.useTransparent);

        BuffImgEncoder encoder = imageStegIO.getEncoder(seed);

        return encoder.decode(length);
    }

    @Override
    public boolean isSteganographicData(byte[] data)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        return isSteganographicData(data, DEFAULT_SEED);
    }

    @Override
    public boolean isSteganographicData(byte[] data, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        if (data == null)
            throw new NullPointerException("Parameter 'data' must not be null");

        BuffImgEncoder encoder = new ImageStegIOAndroid(data, this.useTransparent).getEncoder(seed);

        return bytesToInt(encoder.decode(4)) == HEADER_SIGNATURE;
    }

    /**
     * Returns the maximum number of bytes that can be encoded in the given image using the settings
     * given to the constructor of ImageSteg. The number can be negative if there is not enough
     * capacity to fit the default header.
     * @param image image to potentially encode bytes in
     * @return the payload-capacity of image
     */
    public int getImageCapacity(byte[] image)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        int capacity = new ImageStegIOAndroid(image, this.useTransparent)
                        .getEncoder(DEFAULT_SEED)
                        .getOverlay().available() / 8;

        return this.useDefaultHeader ? (capacity - 8) : capacity;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                       UTIL
    ////////////////////////////////////////////////////////////////////////////////////////////

    private byte[] int2bytes(int integer) {
        return new byte[] {
                (byte) ((integer >> 24) & 0xFF),
                (byte) ((integer >> 16) & 0xFF),
                (byte) ((integer >> 8) & 0xFF),
                (byte) (integer & 0xFF)
        };
    }

    private int bytesToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}
