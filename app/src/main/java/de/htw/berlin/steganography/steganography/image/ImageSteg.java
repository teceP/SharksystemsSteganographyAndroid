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

import de.htw.berlin.steganography.steganography.Steganography;
import de.htw.berlin.steganography.steganography.exceptions.UnknownStegFormatException;
import de.htw.berlin.steganography.steganography.image.encoders.BitmapEncoder;
import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageCapacityException;
import de.htw.berlin.steganography.steganography.image.exceptions.ImageWritingException;
import de.htw.berlin.steganography.steganography.image.exceptions.NoImageException;
import de.htw.berlin.steganography.steganography.image.exceptions.UnsupportedImageTypeException;
import de.htw.berlin.steganography.steganography.util.ImageStegIO;
import de.htw.berlin.steganography.steganography.util.ImageStegIOAndroid;

/**
 * Uses steganography to encode hidden messages ("payload") into images
 */
public class ImageSteg implements Steganography {

    public static final long DEFAULT_SEED = 1732341558;
    private static final int HEADER_SIGNATURE = 1349075561;
    private final boolean useTransparent;
    private final boolean useDefaultHeader;

    /**
     * <p>Creates a new ImageSteg with settings:</p>
     * <ul>
     *     <li>useDefaultHeader = true</li>
     *     <li>useTransparent = false</li>
     * </ul>
     *
     * <p>This means, a default header will be encoded in the image to simplify decoding and
     * fully transparent pixels will not be used for encoding or decoding.</p>
     *
     * <p>This is equivalent to ImageSteg(true, false).</p>
     * @see #ImageSteg(boolean, boolean)
     */
    public ImageSteg() {
        this.useDefaultHeader = true;
        this.useTransparent = false;
    }

    /**
     * <p>Creates a new ImageSteg with the given settings.</p>
     * <b>useDefaultHeader</b>
     * <ul>
     *     <li>if true, the default header will be encoded in the image. The hidden message can then be
     *         decoded using ImageSteg.decode(...).
     *     </li>
     *     <li>
     *         if false, no header will be encoded in the image. The hidden message can only be decoded
     *         using ImageSteg.decodeRaw(length, ...)
     *     </li>
     * </ul>
     *
     * <b>useTransparent</b>
     * <ul>
     *      <li>if true, fully transparent pixels will be used for encoding and decoding</li>
     *      <li>if false, fully transparent pixels will not be used for encoding and decoding</li>
     *      <li>This value must be equal while encoding and decoding to successfully decode the hidden message.</li>
     *      <li>This value can only affect PNGs that contain fully transparent pixels.</li>
     *      <li>If an image has no fully transparent pixels, this value will be ignored.</li>
     *      <li>If the image is a GIF, this value will be ignored.</li>
     *      <li>BMPs with transparent pixels are not supported by this class.</li>
     * </ul>
     * @param useDefaultHeader should the default header be used for encoding?
     * @param useTransparent should fully transparent pixels be used for encoding and decoding?
     * @see #decode(byte[])
     * @see #decode(byte[], long)
     * @see #decodeRaw(int, byte[])
     * @see #decodeRaw(int, byte[], long)
     */
    public ImageSteg(boolean useDefaultHeader, boolean useTransparent) {
        this.useDefaultHeader = useDefaultHeader;
        this.useTransparent = useTransparent;
    }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload)
            throws UnsupportedImageTypeException, NoImageException,
            ImageWritingException, ImageCapacityException, BitmapInaccuracyException {

        return encode(carrier, payload, DEFAULT_SEED);
    }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload, long seed)
            throws NoImageException, UnsupportedImageTypeException,
            ImageWritingException, ImageCapacityException, BitmapInaccuracyException {

        if (carrier == null)
            throw new NullPointerException("Parameter 'carrier' must not be null");
        if (payload == null)
            throw new NullPointerException("Parameter 'payload' must not be null");

        ImageStegIO imageStegIO = new ImageStegIOAndroid(carrier, this.useTransparent);

        BitmapEncoder encoder = imageStegIO.getEncoder(seed);

        if (this.useDefaultHeader) {
            encoder.encode(int2bytes(HEADER_SIGNATURE));
            encoder.encode(int2bytes(payload.length));
        }
        encoder.encode(payload);

        return imageStegIO.getImageAsByteArray();
    }

    /**
     * <p>Decodes a hidden message in the given steganographicData (an image) and returns it as a byte array.</p>
     * <p>This method will fail, if the message was hidden without using the default header.
     * Use ImageSteg.decodeRaw() for this purpose.</p>
     * <p>Reasons for failing with an UnknownStegFormatExceptions are:</p>
     * <ul>
     *      <li>there is no hidden message</li>
     *      <li>the message was hidden with 'useDefaultHeader = false'</li>
     *      <li>the value for 'useTransparent' was different when hiding the message</li>
     *      <li>the message was hidden using an unknown algorithm</li>
     * </ul>
     * @param steganographicData Image containing the hidden message to decode
     * @return the hidden message as a byte array
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     * @throws UnknownStegFormatException if the default header could not be found
     * @see #decodeRaw(int, byte[])
     */
    @Override
    public byte[] decode(byte[] steganographicData)
            throws UnsupportedImageTypeException, NoImageException, UnknownStegFormatException {

        return decode(steganographicData, DEFAULT_SEED);
    }

    /**
     * <p>Decodes a hidden message in the given steganographicData (an image) and returns it as a byte array.</p>
     * <p>This method will fail, if the message was hidden without using the default header.
     * Use ImageSteg.decodeRaw() for this purpose.</p>
     * <p>Reasons for failing with an UnknownStegFormatExceptions are:</p>
     * <ul>
     *      <li>there is no hidden message</li>
     *      <li>the message was hidden with 'useDefaultHeader = false'</li>
     *      <li>the value for 'useTransparent' was different when hiding the message</li>
     *      <li>the message was hidden using an unknown algorithm</li>
     * </ul>
     * @param steganographicData Image containing the hidden message to decode
     * @param seed seed that was used to encode the given stenographicData
     * @return the hidden message as a byte array
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     * @throws UnknownStegFormatException if the default header could not be found
     * @see #decodeRaw(int, byte[], long)
     */
    @Override
    public byte[] decode(byte[] steganographicData, long seed)
            throws NoImageException, UnsupportedImageTypeException, UnknownStegFormatException {

        if (steganographicData == null)
            throw new NullPointerException("Parameter 'steganographicData' must not be null");

        ImageStegIO imageStegIO = new ImageStegIOAndroid(steganographicData, this.useTransparent);

        BitmapEncoder encoder = imageStegIO.getEncoder(seed);

        // decode 4 bytes and compare them to header signature
        if (bytesToInt(encoder.decode(4)) != HEADER_SIGNATURE) {
            throw new UnknownStegFormatException("No steganographic encoding found.");
        }

        // decode the next 4 bytes to get the amount of bytes to read
        int length = bytesToInt(encoder.decode(4));

        return encoder.decode(length);
    }

    /**
     * <p>Interprets an amount of (length * 8) pixels as a hidden message and returns it as a byte array.</p>
     * <p>This method will not search for a header or validate the retrieved data in any form. If 'steganographicData'
     * contains a supported image, this method will always return a result. Whether this result is the hidden message,
     * depends on the settings used:</p>
     * <ul>
     *     <li>'useTransparent' during encoding == 'useTransparent' during decoding</li>
     *     <li>'payload.length' during encoding == 'length' during decoding</li>
     *     <li>No seed used during encoding (thereby using ImageSteg.DEFAULT_SEED)</li>
     *     <li>'useDefaultHeader' == false during encoding</li>
     * </ul>
     * @param length Length (in bytes) of the hidden message
     * @param steganographicData Data containing data to extract
     * @return a byte array of length == "length" as a result of decoding (length * 8) pixels
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     */
    public byte[] decodeRaw(int length, byte[] steganographicData)
            throws NoImageException, UnsupportedImageTypeException {

        return decodeRaw(length, steganographicData, DEFAULT_SEED);
    }

    /**
     * <p>Interprets an amount of (length * 8) pixels as a hidden message and returns it as a byte array.</p>
     * <p>This method will not search for a header or validate the retrieved data in any form. If 'steganographicData'
     * contains a supported image, this method will always return a result. Whether this result is the hidden message,
     * depends on the settings used:</p>
     * <ul>
     *     <li>'useTransparent' during encoding == 'useTransparent' during decoding</li>
     *     <li>'payload.length' during encoding == 'length' during decoding</li>
     *     <li>'seed' during encoding == 'seed' during decoding</li>
     *     <li>'useDefaultHeader' == false during encoding</li>
     * </ul>
     * @param length Length (in bytes) of the hidden message
     * @param steganographicData Data containing data to extract
     * @param seed seed that was used to encode the given stenographicData
     * @return a byte array of length == "length" as a result of decoding (length * 8) pixels
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     */
    public byte[] decodeRaw(int length, byte[] steganographicData, long seed)
            throws NoImageException, UnsupportedImageTypeException {

        if (steganographicData == null)
            throw new NullPointerException("Parameter 'steganographicData' must not be null");

        ImageStegIO imageStegIO = new ImageStegIOAndroid(steganographicData, this.useTransparent);

        BitmapEncoder encoder = imageStegIO.getEncoder(seed);

        return encoder.decode(length);
    }

    @Override
    public boolean isSteganographicData(byte[] data)
            throws NoImageException, UnsupportedImageTypeException {

        return isSteganographicData(data, DEFAULT_SEED);
    }

    @Override
    public boolean isSteganographicData(byte[] data, long seed)
            throws NoImageException, UnsupportedImageTypeException {

        if (data == null)
            throw new NullPointerException("Parameter 'data' must not be null");

        BitmapEncoder encoder = new ImageStegIOAndroid(data, this.useTransparent).getEncoder(seed);

        return bytesToInt(encoder.decode(4)) == HEADER_SIGNATURE;
    }

    /**
     * Returns the maximum number of bytes that can be encoded (as payload) in the given image.
     * This method accounts for the use of transparent pixels and default header as given to the constructor.
     * @param image image to potentially encode bytes in
     * @return the payload-capacity of image
     * @throws NoImageException if no image could be read from the image
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     * @see #ImageSteg(boolean, boolean)
     */
    public int getImageCapacity(byte[] image)
            throws NoImageException, UnsupportedImageTypeException {

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
