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

import de.htw.berlin.steganography.apis.SubscriptionDeamon;
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
import java.util.List;
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
    public RedditSubscriptionDeamon() {
        this.redditUtil = new RedditUtil();
    }

    @Override
    public void run() {
        logger.info("Run subs.deamon reddit");
        this.latestPostEntries = this.getRecentMediaForSubscribedKeywords(null);
    }

    /**
     * Searches for the latest upload medias in this social media network for the given keyword.
     * @param onceUsedKeyword If this String is not null and has more characters than 0, the method will
     *                        search only for this keyword.
     *                        If this param is null or has 0 characters, the stored keywordlist will be
     *                        restored and for earch keyword will be searched in the network.
     */
    public List<PostEntry> getRecentMedia(String onceUsedKeyword) {
        List<String> keywords = redditUtil.getKeywordList(REDDIT, onceUsedKeyword);

        if (keywords == null || keywords.size() == 0) {
            logger.info("No keyword(s) were set.");
            return null;
        }

        List<PostEntry> resultList = new ArrayList<>();

        for (String keyword : keywords) {
            try {
                URL url = new URL(
                        RedditConstants.BASE +
                                RedditConstants.SUBREDDIT_PREFIX + keyword +
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

                logger.info(String.valueOf(con.getURL()));
                resultList.addAll(this.redditUtil.getPosts(responseString));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //logger.info((resultList.size()) + " postentries found.");
        //resultList.stream().forEach(postEntry -> logger.info(postEntry.toString()));
        return resultList;
    }

    @Override
    public List<PostEntry> getRecentMediaForSubscribedKeywords(String keyword) {
        List<PostEntry> tmp = this.getRecentMedia(keyword);

        if (tmp != null) {
            BaseUtil.sortPostEntries(tmp);
            tmp = redditUtil.elimateOldPostEntries(redditUtil.getLatestStoredTimestamp(REDDIT), tmp);
            logger.info((tmp.size()) + " postentries found after eliminate old entries.");

            if (tmp.size() > 0) {
                newPostAvailable = true;

                /**
                 * TODO 0 oder letztes element.
                 */
                redditUtil.setLatestPostTimestamp(REDDIT, tmp.get(tmp.size()-1).getDate());
                latestPostEntries = tmp;
                logger.info("New media found.");
                return tmp;
            }
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
