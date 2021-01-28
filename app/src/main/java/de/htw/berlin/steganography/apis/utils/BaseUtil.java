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

package de.htw.berlin.steganography.apis.utils;

import android.util.Log;

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.steganography.Steganography;
import de.htw.berlin.steganography.steganography.exceptions.MediaNotFoundException;
import de.htw.berlin.steganography.steganography.exceptions.UnknownStegFormatException;
import de.htw.berlin.steganography.steganography.exceptions.UnsupportedMediaTypeException;
import de.htw.berlin.steganography.steganography.image.ImageSteg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Mario Teklic
 */

/**
 * Base utilities which both, Reddit & Imgur are using
 */
public class BaseUtil {
    private static final Logger logger = Logger.getLogger(BaseUtil.class.getName());

    public BaseUtil(){
    }

    /**
     * Updates all listeners
     * @param socialMedia
     * @param msgList
     */
    public void updateListeners(SocialMedia socialMedia, List<String> msgList) {
        class AndroidDownloadTask implements Runnable{
            SocialMedia socialMediaTask;
            List<String> msgListTask;
            AndroidDownloadTask(SocialMedia socialMedia, List<String> msgList) { this.socialMediaTask = socialMedia; this.msgListTask = msgList;}
            @Override
            public void run() {
                Log.i("BaseUtil updateListeners", "called");
                Steganography steganography = new ImageSteg();
                List<String> decodedMessageString = new ArrayList<>();
                List<byte[]> encodedByteArray = new ArrayList<>();
                Log.i("BaseUtil updateListeners", "starting to download...");
                for (String link : msgList) {
                    Log.i("BaseUtil updateListeners", "begin download of new file... Link:" + link);
                    encodedByteArray.add(BlobConverterImpl.downloadToByte(link));
                    Log.i("BaseUtil updateListeners", "finished downloading file...");
                }
                Log.i("BaseUtil updateListeners", "finished downloading all files");
                Log.i("BaseUtil updateListeners", "starting to decode files");
                Log.i("BaseUtil updateListeners encodedByteArray size", String.valueOf(encodedByteArray.size()));
                for (byte[] encodedByte : encodedByteArray) {
                    try {
                        Log.i("BaseUtil updateListeners", "started decoding file...");
                        decodedMessageString.add(new String(steganography.decode(encodedByte)));
                        Log.i("BaseUtil updateListeners", "decoded file...");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (MediaNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedMediaTypeException e) {
                        e.printStackTrace();
                    } catch (UnknownStegFormatException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("BaseUtil updateListeners", "finished decoding all files");
                this.socialMediaTask.addMessages(decodedMessageString);
            }
        };
        Thread t = new Thread(new AndroidDownloadTask(socialMedia, msgList));
        t.start();

    }

    /**
     * Sorts a list of post entries, based on their timestamp
     * @param postEntries
     */
    public static void sortPostEntries(List<PostEntry> postEntries){
        Collections.sort(postEntries);
    }

    /**

     * - 600.000 (Ten minutes) to avoid post-time-upload-problem
     * @param latestPostTimestamp
     */
    public void setLatestPostTimestamp(SocialMedia socialMedia, String keyword, MyDate latestPostTimestamp) {
        //if(latestPostTimestamp.getTime() != 0){
            logger.info("Set timestamp in ms: " + latestPostTimestamp.getTime());
           // socialMedia.setLastTimeCheckedForKeyword(keyword, (latestPostTimestamp.getTime() - 600000));
        socialMedia.setLastTimeCheckedForKeyword(keyword, latestPostTimestamp.getTime());

        Log.i("called social media setLatest", "called social media setLatest");
        //}else{
          //  logger.info("Latest timestamp will not be set because the timestamp parameter was zero.");
        //}
    }

    /**
     * Restores the latest stored timestamp with the JSONPersistentManager.
     * @return MyDate(0) if an exception was thrown. Happens if there is no stored timestamp found,
     *         or the timestamp was stored wrong e.g. with an character for an example 'k' within the stored value like
     *         '1231k512'.
     */
    public MyDate getLatestStoredTimestamp(SocialMedia socialMedia, String keyword) {
        Log.i("15. BaseUtil getLatestStoredTimestamp called for keyword:", keyword);
        MyDate oldPostTimestamp = null;
        Log.i("15.2 BaseUtil getLatestStoredTimestamp SocialMedia eintrag for keyword: "+keyword+" lastTimeChecked",String.valueOf( socialMedia.getLastTimeCheckedForKeyword(keyword)));

        try {
            String oldPostTimestampString = String.valueOf(socialMedia.getLastTimeCheckedForKeyword(keyword));
            Log.i("16. BaseUtil getLatestStoredTimestamp keyword: " + keyword+" oldPostTimestampString:", oldPostTimestampString);
            oldPostTimestamp = new MyDate(new Date(Long.valueOf(oldPostTimestampString)));
        } catch (Exception e) {
            logger.info("Exception was thrown, while retrieving latest stored timestamp. Default value for latest timestamp is 'new Date(0)'.");
            oldPostTimestamp = new MyDate(new Date(0));
        }
        return oldPostTimestamp;
    }

    /**
     * Generates the keyword list, which has to be processed by the subscription deamons.

     * @return the list of keywords, or if no keywords were found, an empty list.
     */
    public Map<String, Long> getKeywordAndLastTimeCheckedMap(SocialMedia socialMedia){
        Log.i("4. BaseUtil called getKeywordAndLastTimeCheckedMap for socialMedia", socialMedia.getApiName() );
        Map<String, Long> keywords = new HashMap<>();
        keywords = socialMedia.getAllSubscribedKeywordsAndLastTimeChecked();
        Log.i("5. BaseUtil getKeywordAndLastTimeCheckedMap keywords Map size", String.valueOf(keywords.size()));
        return keywords;
    }

    /**
     * Returns a timestamp for a String of info which represents the time as ms.
     * @return Timestamp in seconds
     */
    public MyDate getTimestamp(String info){
        return new MyDate(new Date(Long.valueOf(info.substring(0, info.length()-2))));
    }

    /**
     * Proofs if a HTTP Code is has an error or not.
     * Uses range between 199 and 299 for "good" codes.
     * @param responseCode
     * @return true if has error code and param is not in range of "good" codes.
     */
    public static boolean hasErrorCode(int responseCode) {
        if (199 <= responseCode && responseCode <= 299) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Eliminated old posts according to a timatestamp (should be latest stored, but can be used for other
     * purposes with any timestamp)
     * @param latestStoredTimestamp
     * @param postEntries
     * @return the filtered list, which only has items with timestamps wich are newer than the param latestStoredTimestamp
     */
    public List<PostEntry> elimateOldPostEntries(MyDate latestStoredTimestamp, List<PostEntry> postEntries){
        //If current postEntry's timestamp is not newer than latestStored, filter it.
        Log.i("17. BaseUtil eliminateOldPostEntries called with latestStoredTimestamp"+ String.valueOf(latestStoredTimestamp.getTime())+"and postEntries List size:", String.valueOf(postEntries.size()));
        return postEntries
                .stream()
                .filter(postEntry -> latestStoredTimestamp.compareTo(postEntry.getDate()) < 0)
                .collect(Collectors.toList());
    }

    /**
     * 'Decoded' an URL: 'amp;' will be replaced with an empty String.
     *  This is the only encoding which is used in the URL String for this application.
     * @param url
     * @return
     */
    public static String decodeUrl(String url){
        return url.replace("amp;", "");
    }
}
