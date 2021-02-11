/*
 * Copyright (c) 2020
 * Contributed by Mario Teklic
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

package de.htw.berlin.steganography.apis.imgur;

import android.util.Log;

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.SubscriptionDeamon;
import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.reddit.RedditConstants;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.htw.berlin.steganography.apis.imgur.ImgurConstants.BASE_URI;
import static de.htw.berlin.steganography.apis.imgur.ImgurConstants.SEARCH_URI;
import static de.htw.berlin.steganography.apis.models.APINames.IMGUR;

/**
 * @author Mario Teklic
 */

/**
 * Can search for new post entries in Imgur in an interval or just once
 */
public class ImgurSubscriptionDeamon implements SubscriptionDeamon {

    private static final Logger logger = Logger.getLogger(ImgurSubscriptionDeamon.class.getName());

    /**
     * Utilities for processing the search
     */
    private ImgurUtil imgurUtil;

    /**
     * Social Media
     */
    private SocialMedia socialMedia;

    /**
     * Represents if there were post entries found in the last search
     */
    private boolean newPostAvailable;

    /**
     * Latest found post entries
     */
    private List<PostEntry> latestPostEntries;

    /**
     * Subcription for a Keyword in a Social Media
     */
    public ImgurSubscriptionDeamon(SocialMedia socialMedia, ImgurUtil imgurUtil) {
        this.imgurUtil = imgurUtil;
        this.socialMedia = socialMedia;
    }

    @Override
    public void run() {
        //bool newPostAvailable will be setted in getRecentMediaForSubscribedKeywords()
        this.latestPostEntries = this.getRecentMediaForSubscribedKeywords();
    }

    /**
     * Searches for the latest upload medias in this social media network for the given keyword.
     */
     public Map<String, List<PostEntry>> getRecentMedia() {
        Map<String,Long> keywords = imgurUtil.getKeywordAndLastTimeCheckedMap(socialMedia);

        if (keywords == null || keywords.size() == 0) {
            logger.info("No keyword(s) were set.");
            return Collections.emptyMap();
        }

        Map<String, List<PostEntry>> resultMap = new HashMap<>();

        for (String key : keywords.keySet()) {
            logger.info("Check for new post entries for keyword '" + key + "' ...");

            try {
                URL url = new URL(BASE_URI + SEARCH_URI + key);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Log.i("Hello imgur", "1");
                Response response = client.newCall(request).execute();
                Log.i("Hello imgur", "2");

                int respCode = response.code();
                Log.i("Hello imgur", "3");

                String responseString = response.body().string();
                Log.i("Hello imgur", "4");

                if (!BaseUtil.hasErrorCode(respCode)) {
                    Log.i("Hello imgur", "5");

                    logger.info("Response Code  imgur: " + respCode + ". No error.");
                    resultMap.put(key, this.imgurUtil.getPosts(responseString));
                } else {
                    Log.i("Hello imgur", "5,5");

                    logger.info("Response Code  imgur: " + respCode + ". Has error. For Keyword: " + key + ".");
                }
                logger.info(url.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //logger.info((resultList.size()) + " postentries found.");
        //resultList.stream().forEach(postEntry -> logger.info(postEntry.toString()));
        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PostEntry> getRecentMediaForSubscribedKeywords() {
        Map<String, List<PostEntry>> tmp = this.getRecentMedia();
        Log.i("13.3 ImgurSubscriptionDeamon getRecentMediaForsubscribedKeywords tmp size", String.valueOf(tmp.size()));
        List<PostEntry> latestPostEntries = new ArrayList<>();

        if (tmp != null) {
            for (Map.Entry<String, List<PostEntry>> entry : tmp.entrySet()) {
                Log.i("14.  tmp.entrySet for keyword:" + entry.getKey() + " postEntires List size:", String.valueOf(entry.getValue().size()));
                Log.i("14.2  last checked for keyword " + entry.getKey(), String.valueOf(imgurUtil.getLatestStoredTimestamp(socialMedia, entry.getKey()).getTime()));
                MyDate myDate = imgurUtil.getLatestStoredTimestamp(socialMedia, entry.getKey());
                entry.setValue(imgurUtil.elimateOldPostEntries(myDate, entry.getValue()));
                Log.i("18.  eliminated old postEntires for keyword: " + entry.getKey() + ", remaining postEntries", String.valueOf(entry.getValue().size()));

                if (entry.getValue().size() > 0) {
                    newPostAvailable = true;
                    BaseUtil.sortPostEntries(entry.getValue());
                    Log.i("19. imgurUitl.setLatestPostTiestamp for keyword:", entry.getKey() + " and postEntries List size: " + entry.getValue().size());
                    imgurUtil.setLatestPostTimestamp(socialMedia, entry.getKey(), entry.getValue().get(entry.getValue().size() - 1).getDate());

                    Log.i("new media found for", entry.getKey());
                    for (PostEntry postEntry : entry.getValue()) {
                        latestPostEntries.add(postEntry);
                    }
                } else {
                    Log.i("no new media found for", entry.getKey());
                   imgurUtil.setLatestPostTimestamp(socialMedia, entry.getKey(), imgurUtil.getLatestStoredTimestamp(socialMedia, entry.getKey()));
                }
            }
            imgurUtil.updateListeners(socialMedia, latestPostEntries.stream().map(PostEntry::getUrl).collect(Collectors.toList()));
            Log.i(" latestPostEntries", String.valueOf(latestPostEntries.stream().map(PostEntry::getUrl).collect(Collectors.toList()).size()));
            return latestPostEntries;
        }
        logger.info("No new media found.");
        latestPostEntries = Collections.emptyList();
        newPostAvailable = false;
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNewPostAvailable() {
        return this.newPostAvailable;
    }

    /**
     * Returns a list of the latest found post entries
     */
    public List<PostEntry> getLatestPostEntries() {
        return this.latestPostEntries;
    }
}
