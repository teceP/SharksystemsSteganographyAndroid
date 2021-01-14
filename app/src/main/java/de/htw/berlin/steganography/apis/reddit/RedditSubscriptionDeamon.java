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
    public RedditSubscriptionDeamon(RedditUtil redditUtil) {
        this.redditUtil = redditUtil;
    }

    @Override
    public void run() {
        Log.i("1. RedditSubscriptionDeamon run","started");
        this.latestPostEntries = this.getRecentMediaForSubscribedKeywords("DUMMYKEYWORD");
        Log.i("RedditSubscriptionDeamon run","finished");
    }

    /**
     * Searches for the latest upload medias in this social media network for the given keyword.
     * @param onceUsedKeyword If this String is not null and has more characters than 0, the method will
     *                        search only for this keyword.
     *                        If this param is null or has 0 characters, the stored keywordlist will be
     *                        restored and for earch keyword will be searched in the network.
     */
    public Map<String, List<PostEntry>> getRecentMedia(String onceUsedKeyword) {
        Log.i("3. RedditSubscriptionDeamon getRecentMedia called with onceUsedKeyword", onceUsedKeyword);
        //Currently fix for not null
        Map<String,Long> keywords = redditUtil.getKeywordAndLastTimeCheckedMap(onceUsedKeyword);

        Log.i("6. RedditSubscriptionDeamon getRecentMedia keywords map size", String.valueOf(keywords.size()));
        if (keywords == null || keywords.size() == 0) {
            logger.info("No keyword(s) were set.");
            return Collections.emptyMap();
        }

        Map<String, List<PostEntry>> resultMap = new HashMap<>();

        for (String key : keywords.keySet()) {
            Log.i("7. RedditSubscriptionDeamon getRecentMedia search PostEntrys for keyword", key);
            try {
                URL url = new URL(
                        RedditConstants.BASE +
                                RedditConstants.SUBREDDIT_PREFIX + key +
                                "/new/" +
                                RedditConstants.AS_JSON +
                                "?count=20");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(RedditConstants.GET);
                con.setRequestProperty("User-agent", RedditConstants.APP_NAME);
                con.setDoOutput(true);

                String responseString = "";

                if (!BaseUtil.hasErrorCode(con.getResponseCode())) {
                    responseString = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining());
                    logger.info("Response Code: " + con.getResponseCode() + ". No error.");
                } else {
                    logger.info("Response Code: " + con.getResponseCode() + ". Has error.");
                    return null;
                }

                Log.i("8. RedditSubscriptionDeamon getRecentMedia result URL for keyword "+key,String.valueOf(con.getURL()));
                resultMap.put(key, this.redditUtil.getPosts(responseString));
                Log.i("11. RedditSubscriptionDeamon getRecentMedia resultMap for keyword: "+key+" found post:", String.valueOf( resultMap.get(key).size() ) );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i("12. RedditSubscriptionDeamon getRecentMedia result map size: ",String.valueOf(resultMap.size()));
        //resultList.stream().forEach(postEntry -> logger.info(postEntry.toString()));
        return resultMap;
    }

    @Override
    public List<PostEntry> getRecentMediaForSubscribedKeywords(String keyword) {
        Log.i("2. RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords called with keyword", keyword);
        Map<String, List<PostEntry>> tmp = this.getRecentMedia(keyword);
        Log.i("13 RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords tmp.get(\"test\").size", String.valueOf(tmp.get("test").size()));
        Log.i("13.2 RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords tmp size", String.valueOf(tmp.size()));

        List<PostEntry> latestPostEntries = new ArrayList<>();
        if (tmp != null) {
            for (Map.Entry<String, List<PostEntry>> entry : tmp.entrySet()) {
                Log.i("14. RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords iterate through tmp.entrySet for keyword:"+entry.getKey()+" postEntires List size:", String.valueOf(entry.getValue().size()) );

                //remove old posts
                entry.setValue(redditUtil.elimateOldPostEntries(redditUtil.getLatestStoredTimestamp(entry.getKey()), entry.getValue()));
                Log.i("18. RedditSubscriptionDeamon getRecentMediaForsubscribedKeywords eliminated old postEntires for keyword: "+entry.getKey()+", remaining postEntries", String.valueOf(entry.getValue().size()));
                //logger.info((entry.getValue().size()) + " postentries found after eliminate old entries for keyword: "+ entry.getKey());

                if (entry.getValue().size() > 0) {
                    newPostAvailable = true;
                    /**
                     * TODO 0 oder letztes element.
                     */
                    ///keywordchange STILL NEED TO SORT LIST FOR CORRECT TIMESTAMP UPDATE
                    BaseUtil.sortPostEntries(entry.getValue());
                    Log.i("19. RedditSubscriptionDeamon called redditUitl.setLatestPostTiestamp for keyword:", entry.getKey() +" and postEntries List size: "+entry.getValue().size());
                    redditUtil.setLatestPostTimestamp(entry.getKey(), entry.getValue().get(entry.getValue().size() - 1).getDate());


                    Log.i("new media found for", entry.getKey());
                    for(PostEntry postEntry: entry.getValue()) {
                        latestPostEntries.add(postEntry);
                    }



                }


            }
            redditUtil.updateListeners(latestPostEntries.stream().map(PostEntry::getUrl).collect(Collectors.toList()));
            Log.i(" latestPostEntries", String.valueOf(latestPostEntries.stream().map(PostEntry::getUrl).collect(Collectors.toList()).size()));

            return latestPostEntries;
        }

        logger.info("No new media found.");
        latestPostEntries = Collections.emptyList();
        newPostAvailable = false;
        return Collections.emptyList();
    }

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
