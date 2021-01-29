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

package de.htw.berlin.steganography.apis.reddit;

import android.util.Log;

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.SubscriptionDeamon;
import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import de.htw.berlin.steganography.apis.models.PostEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.htw.berlin.steganography.apis.models.APINames.REDDIT;

/**
 * @author Mario Teklic
 */

/**
 *
 */
public class RedditSubscriptionDeamon implements SubscriptionDeamon {

    private static final Logger logger = Logger.getLogger(RedditSubscriptionDeamon.class.getName());

    /**
     * Utilities for processing the search
     */
    private RedditUtil redditUtil;

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
    public RedditSubscriptionDeamon(SocialMedia socialMedia, RedditUtil redditUtil) {
        this.redditUtil = redditUtil;
        this.socialMedia = socialMedia;
    }

    /**
     * Search for all subscribed keywords.
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Log.i("1. RedditSubscriptionDeamon run", "started");
        this.latestPostEntries = this.getRecentMediaForSubscribedKeywords();
        Log.i("RedditSubscriptionDeamon run", "finished");
    }

    /**
     * Searches for the latest upload medias in this social media network for the given keyword.
     */
    public Map<String, List<PostEntry>> getRecentMedia() {
        Log.i("3. RedditSubscriptionDeamon getRecentMedia called", "true");
        //Currently fix for not null
        Map<String, Long> keywords = redditUtil.getKeywordAndLastTimeCheckedMap(socialMedia);

        Log.i("6. RedditSubscriptionDeamon getRecentMedia keywords map size", String.valueOf(keywords.size()));
        if (keywords == null || keywords.size() == 0) {
            logger.info("No keyword(s) were set.");
            return Collections.emptyMap();
        }

        Map<String, List<PostEntry>> resultMap = new HashMap<>();

        try {
            URL url = new URL(
                    RedditConstants.BASE +
                            RedditConstants.SUBREDDIT_PREFIX +
                            "test/new/" +
                            RedditConstants.AS_JSON +
                            "?count=100");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(RedditConstants.GET);
            con.setRequestProperty("User-agent", RedditConstants.APP_NAME);
            con.setDoOutput(true);

            String responseString = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining());

            if (!BaseUtil.hasErrorCode(con.getResponseCode())) {
                logger.info("Response Code: " + con.getResponseCode() + ". No error.");
                for (String keyword : keywords.keySet()) {
                    Log.i("8. RedditSubscriptionDeamon getRecentMedia result URL for keyword " + keyword, String.valueOf(con.getURL()));
                    resultMap.put(keyword, this.redditUtil.getPosts(keyword, responseString));
                    Log.i("11. RedditSubscriptionDeamon getRecentMedia resultMap for keyword: " + keyword + " found post:", String.valueOf(resultMap.get(keyword).size()));
                }
            } else {
                logger.info("response string: /1 " + responseString);
                logger.info("Response Code: " + con.getResponseCode() + ". Has error.");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("12. RedditSubscriptionDeamon getRecentMedia result map size: ", String.valueOf(resultMap.size()));
        //resultList.stream().forEach(postEntry -> logger.info(postEntry.toString()));
        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PostEntry> getRecentMediaForSubscribedKeywords() {
        Log.i("2. RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords called ", "true");
        Map<String, List<PostEntry>> tmp = this.getRecentMedia();
        Log.i("13.3 RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords tmp size", String.valueOf(tmp.size()));
        List<PostEntry> latestPostEntries = new ArrayList<>();

        if (tmp != null) {
            for (Map.Entry<String, List<PostEntry>> entry : tmp.entrySet()) {
                Log.i("14.  tmp.entrySet for keyword:" + entry.getKey() + " postEntires List size:", String.valueOf(entry.getValue().size()));
                Log.i("14.2  last checked for keyword " + entry.getKey(), String.valueOf(redditUtil.getLatestStoredTimestamp(socialMedia, entry.getKey()).getTime()));
                MyDate myDate = redditUtil.getLatestStoredTimestamp(socialMedia, entry.getKey());
                entry.setValue(redditUtil.elimateOldPostEntries(myDate, entry.getValue()));
                Log.i("18.  eliminated old postEntires for keyword: " + entry.getKey() + ", remaining postEntries", String.valueOf(entry.getValue().size()));

                if (entry.getValue().size() > 0) {
                    newPostAvailable = true;
                    BaseUtil.sortPostEntries(entry.getValue());
                    Log.i("19. redditUitl.setLatestPostTiestamp for keyword:", entry.getKey() + " and postEntries List size: " + entry.getValue().size());
                    redditUtil.setLatestPostTimestamp(socialMedia, entry.getKey(), entry.getValue().get(entry.getValue().size() - 1).getDate());

                    Log.i("new media found for", entry.getKey());
                    for (PostEntry postEntry : entry.getValue()) {
                        latestPostEntries.add(postEntry);
                    }
                } else {
                    redditUtil.setLatestPostTimestamp(socialMedia, entry.getKey(), redditUtil.getLatestStoredTimestamp(socialMedia, entry.getKey()));
                }
            }
            redditUtil.updateListeners(socialMedia, latestPostEntries.stream().map(PostEntry::getUrl).collect(Collectors.toList()));
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
        return newPostAvailable;
    }

    /**
     * Returns a list of the latest found post entries
     */
    public List<PostEntry> getLatestPostEntries() {
        return this.latestPostEntries;
    }
}
