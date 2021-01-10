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

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.SubscriptionDeamon;
import de.htw.berlin.steganography.apis.reddit.RedditConstants;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.utils.BaseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    public ImgurSubscriptionDeamon(SocialMedia socialMedia) {
        this.imgurUtil = new ImgurUtil(socialMedia);
    }

    public void injectImgurUtil(ImgurUtil util){
        this.imgurUtil = util;
    }

    @Override
    public void run() {
        //bool newPostAvailable will be setted in getRecentMediaForSubscribedKeywords()
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
        List<String> keywords = imgurUtil.getKeywordList(onceUsedKeyword);

        if (keywords == null || keywords.size() == 0) {
            logger.info("No keyword(s) were set.");
            return null;
        }

        List<PostEntry> resultList = new ArrayList<>();

        for (String keyword : keywords) {
            logger.info("Check for new post entries for keyword '" + keyword + "' ...");

            try {
                URL url = new URL(
                        BASE_URI + SEARCH_URI + keyword);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(RedditConstants.GET);
                con.setRequestProperty("User-agent", ImgurConstants.APP_NAME);
                con.setRequestProperty("Authorization", "Client-ID " + ImgurConstants.CLIENT_ID);
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
                resultList.addAll(this.imgurUtil.getPosts(responseString));
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
        List<PostEntry> tmp = getRecentMedia(keyword);

        if (tmp != null) {
            BaseUtil.sortPostEntries(tmp);
            tmp = imgurUtil.elimateOldPostEntries(imgurUtil.getLatestStoredTimestamp(), tmp);
            if (tmp.size() > 0) {
                newPostAvailable = true;

                /**
                 * TODO 0 oder letztes element.
                 */

                imgurUtil.setLatestPostTimestamp(IMGUR, tmp.get(tmp.size()-1).getDate());
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
        return this.newPostAvailable;
    }

    /**
     * Returns a list of the latest found post entries
     */
    public List<PostEntry> getLatestPostEntries() {
        return this.latestPostEntries;
    }
}
