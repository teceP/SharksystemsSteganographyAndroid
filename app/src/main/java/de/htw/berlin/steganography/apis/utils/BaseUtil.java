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
import de.htw.berlin.steganography.apis.models.APINames;
import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.models.PostEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private SocialMedia socialMedia;

    public BaseUtil(SocialMedia socialMedia){
        this.socialMedia = socialMedia;
    }

    public void setSocialMedia(SocialMedia socialMedia) {
        this.socialMedia = socialMedia;
    }

    public SocialMedia getSocialMedia(){
        return socialMedia;
    }

    public void updateListeners(List<String> msgList){
        this.socialMedia.setMessage(msgList);
    }

    /**
     * Sorts a list of post entries, based on their timestamp
     * @param postEntries
     */
    public static void sortPostEntries(List<PostEntry> postEntries){
        Collections.sort(postEntries);
    }

    /**
     * Calls the JSONPersistentManager singleton and stores a (the latest timestamp of the postenty-list)
     * according to a specific network.
     * @param latestPostTimestamp
     */
    public void setLatestPostTimestamp(MyDate latestPostTimestamp) {
        logger.info("Set timestamp in ms: " + latestPostTimestamp.getTime());
        socialMedia.setLastTimeChecked(latestPostTimestamp.getTime());
        Log.i("called social media setLatest", "called social media setLatest");

    }

    /**
     * Restores the latest stored timestamp with the JSONPersistentManager.
     * @return MyDate(0) if an exception was thrown. Happens if there is no stored timestamp found,
     *         or the timestamp was stored wrong e.g. with an character for an example 'k' within the stored value like
     *         '1231k512'.
     */
    public MyDate getLatestStoredTimestamp() {
        MyDate oldPostTimestamp = null;

        try {
            String oldPostTimestampString = String.valueOf(socialMedia.getLastTimeChecked());
            oldPostTimestamp = new MyDate(new Date(Long.valueOf(oldPostTimestampString)));
        } catch (Exception e) {
            logger.info("Exception was thrown, while retrieving latest stored timestamp. Default value for latest timestamp is 'new Date(0)'.");
            oldPostTimestamp = new MyDate(new Date(0));
        }

        return oldPostTimestamp;
    }

    /**
     * Generates the keyword list, which has to be processed by the subscription deamons.
     *
     * @param onceUsedKeyword If keyword is NOT null AND the length is NOT 0, a list with just and only this keyword gets returned.
     *                        If the keyword is null or the length is 0, a keywordlist will be restored by the JSONPersistentManager.
     *                        All empty occuring keywords will be removed from the list.
     * @return the list of keywords, or if no keywords were found, an empty list.
     */
    public List<String> getKeywordList(String onceUsedKeyword){
        List<String> keywords = new ArrayList<>();

        if(onceUsedKeyword != null && onceUsedKeyword.length() > 0){
            keywords.add(onceUsedKeyword);
        }else{
            try {
                keywords = socialMedia.getAllSubscribedKeywords();
                keywords.removeIf(String::isEmpty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (onceUsedKeyword == null && keywords == null || keywords.size() == 0) {
            return Collections.emptyList();
        }
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
