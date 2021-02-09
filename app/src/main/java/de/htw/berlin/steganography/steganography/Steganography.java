/*
 * Copyright (c) 2020
 * Contributed by NAME HERE
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

package de.htw.berlin.steganography.steganography;

import de.htw.berlin.steganography.steganography.exceptions.MediaCapacityException;
import de.htw.berlin.steganography.steganography.exceptions.MediaNotFoundException;
import de.htw.berlin.steganography.steganography.exceptions.MediaReassemblingException;
import de.htw.berlin.steganography.steganography.exceptions.UnknownStegFormatException;
import de.htw.berlin.steganography.steganography.exceptions.UnsupportedMediaTypeException;
import de.htw.berlin.steganography.steganography.image.exceptions.BitmapInaccuracyException;


public interface Steganography {
    /**
     * Encodes the given payload in the given carrier (image, mp3, ...) and returns the result.
     * The format of the returned media will be the same as carrier. Carrier needs to be an exact media representation
     * as it would be read from a file by an InputStream.
     * @param carrier media used to hide the payload
     * @param payload data to hide
     * @return steganographic data - exact media representation. Can be stored as it is to a file to open externally
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from carrier
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     * @throws MediaReassemblingException if a problem occurred during writing of the result media
     * @throws MediaCapacityException if the payload doesn't fit in the carrier
     * @throws BitmapInaccuracyException if bitmap sets an intended pixel inaccurately, thereby
     * not encoding the payload properly
     */
    byte[] encode(byte[] carrier, byte[] payload)
            throws MediaNotFoundException, UnsupportedMediaTypeException,
            MediaReassemblingException, MediaCapacityException, BitmapInaccuracyException;

    /**
     * <p>Encodes the given payload in the given carrier (image, mp3, ...) and returns the result.
     * The format of the returned media will be the same as carrier. Carrier needs to be an exact media representation
     * as it would be read from a file by an InputStream.</p>
     * <p>The Seed changes the way the payload is encoded. When decoding the result, the exact same Seed needs to be
     * given to decode()</p>
     * @param carrier media used to hide the payload
     * @param payload data to hide
     * @param seed affects the resulting steganographic data (similar to a password)
     * @return steganographic data - exact media representation. Can be stored as it is to a file to open externally
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from carrier
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     * @throws MediaReassemblingException if a problem occurred during writing of the result media
     * @throws MediaCapacityException if the payload doesn't fit in the carrier
     * @throws BitmapInaccuracyException if bitmap sets an intended pixel inaccurately, thereby
     * not encoding the payload properly
     */
    byte[] encode(byte[] carrier, byte[] payload, long seed)
            throws MediaNotFoundException, UnsupportedMediaTypeException,
            MediaReassemblingException, MediaCapacityException, BitmapInaccuracyException;

    /**
     * <p>Decodes a hidden message in the given steganographicData and returns it as a byte array.</p>
     * <p>steganographicData needs to be an exact media representation as it would be read from a file by an
     * InputStream.</p>
     * <p>This method only works if the message was encoded using no Seed or the respective default Seed.
     * Otherwise it will throw an UnknownStegFormat as if no message was found.</p>
     * @param steganographicData Media containing the hidden message to decode
     * @return the hidden message as a byte array
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from steganographicData
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     * @throws UnknownStegFormatException if no hidden message was found
     */
    byte[] decode(byte[] steganographicData)
            throws MediaNotFoundException, UnsupportedMediaTypeException, UnknownStegFormatException;

    /**
     * <p>Decodes a hidden message in the given steganographicData and returns it as a byte array.</p>
     * <p>steganographicData needs to be an exact media representation as it would be read from a file by an
     * InputStream</p>
     * <p>This method only works if the message was encoded using the given Seed. Otherwise it will throw an
     * UnknownStegFormatException as if no message was found.</p>
     * @param steganographicData Media containing the hidden message to decode
     * @param seed seed that was used to encode the given stenographicData
     * @return the hidden message as a byte array
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from
     * steganographicData
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     * @throws UnknownStegFormatException if no hidden message was found
     */
    byte[] decode(byte[] steganographicData, long seed)
            throws MediaNotFoundException, UnsupportedMediaTypeException, UnknownStegFormatException;

    /**
     * <p>Tests whether the given data has a hidden message encoded in it. This method only works if the message was encoded
     * using the given Seed or the respective default Seed. Otherwise it will always return false.</p>
     * <p>The use of this method is discouraged. It saves very little resources compared to decode(...). So unless
     * you need to test a lot of possible steganographicData, just use decode(...) and catch the UnknownStegFormatException</p>
     * @param data data to test
     * @return true if the given data has a hidden message encoded in it
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from data
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     */
    boolean isSteganographicData(byte[] data)
            throws MediaNotFoundException, UnsupportedMediaTypeException;

    /**
     * <p>Tests whether the given data has a hidden message encoded in it. This method only works if the message was encoded
     * using the given Seed. Otherwise it will always return false.</p>
     * <p>The use of this method is discouraged. It saves very little resources compared to decode(...). So unless
     * you need to test a lot of possible steganographicData, just use decode(...) and catch the UnknownStegFormatException</p>
     * @param data data to test
     * @param seed seed the hidden message was encoded with
     * @return true if the given data has a hidden message encoded in it
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from data
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     */
    boolean isSteganographicData(byte[] data, long seed)
            throws MediaNotFoundException, UnsupportedMediaTypeException;
}
