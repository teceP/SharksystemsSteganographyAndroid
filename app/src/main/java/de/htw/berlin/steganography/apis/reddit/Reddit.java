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

import de.htw.berlin.steganography.apis.MediaType;
import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.SocialMediaModel;
import de.htw.berlin.steganography.apis.models.Token;
import de.htw.berlin.steganography.apis.imgur.Imgur;
import de.htw.berlin.steganography.apis.interceptors.BearerInterceptor;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import de.htw.berlin.steganography.apis.utils.BlobConverterImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.htw.berlin.steganography.apis.models.APINames.REDDIT;
import static de.htw.berlin.steganography.apis.reddit.RedditConstants.SUBREDDIT;

/**
 * @author Mario Teklic
 */

/**
 * Reddit social media can upload, download, check for new postentries for a specific keyword/subreddit
 */
public class Reddit extends SocialMedia {

    private static final Logger logger = Logger.getLogger(Reddit.class.getName());

    /**
     * Utilities which are used while upF--loading, download, searching
     */
    private RedditUtil redditUtil;

    /**
     * For uploading and searching once or in an given interval for new post entries.
     * Asynchron.
     */
    private RedditSubscriptionDeamon redditSubscriptionDeamon;

    /**
     * Token which is needed while uploading a new image
     */
    private Token token;

    /**
     * Manages the interval called search for new post entries
     */
    private ScheduledExecutorService executor;

    /**
     * Future of scheduler service
     */
    private ScheduledFuture scheduledFuture;

    /**
     * Intervall of searching for new posts
     */
    private Integer interval;

    /**
     * Default constructor. Prepares the subscription deamon, utils and the executor.
     */
    public Reddit(SocialMediaModel socialMediaModel) {
        super(socialMediaModel);
        this.redditUtil = new RedditUtil();
        this.redditSubscriptionDeamon = new RedditSubscriptionDeamon(this,redditUtil);
        executor = Executors.newScheduledThreadPool(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postToSocialNetwork(byte[] media, MediaType mediaType, String hashtag) {
        if (this.token == null) {
            logger.info("User not logged in.");
            return false;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BearerInterceptor()).build();

        RequestBody mBody = null;

        try {

            String url = Imgur.uploadPicture(media, hashtag).data.link;

            if(url == null || url.isEmpty())
                return false;

            mBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", hashtag)
                    .addFormDataPart("kind", "image")
                    .addFormDataPart("text", hashtag)
                    .addFormDataPart("sr", SUBREDDIT)
                    .addFormDataPart("resubmit", "true")
                    .addFormDataPart("send_replies", "true")
                    .addFormDataPart("api_type", "json")
                    .addFormDataPart("url", url)
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .headers(Headers.of("Authorization", ("Bearer " + this.token.getToken())))
                    .url(RedditConstants.OAUTH_BASE +
                            RedditConstants.UPLOAD_PATH)
                    .post(mBody)
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();

            int respCode = response.code();
            logger.info("Response code: " + respCode);
            if(!BaseUtil.hasErrorCode(respCode)){
                JsonObject rootObj = JsonParser.parseString(responseString).getAsJsonObject();
                //    {"json": {"errors": [], "data": {"url": "https://www.reddit.com/r/test/comments/l4kuma/katze/", "drafts_count": 0, "id": "l4kuma", "name": "t3_l4kuma"}}}
                if(responseString.contains("url")){
                    logger.info("Uploaded: " + rootObj.get("url"));
                    return true;
                }
            }
        } catch (Exception e) {
            logger.info("Error while creating new post on reddit.");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Listens for new post entries in imgur network for stored keywords.
     * Asynchron.
     * @param interval Interval in minutes
     *
     * {@inheritDoc}
     */
    @Override
    public void changeSchedulerPeriod(Integer interval) {
        if(isSchedulerRunning()) {
            stopSearch();
            this.interval = interval;
            startSearch();
        }
        this.interval = interval;
    }

    /**
     * Returns if the Subscription deamon is running
     */
    public boolean isSchedulerRunning(){
        if(scheduledFuture!=null){
            return !scheduledFuture.isCancelled() && !scheduledFuture.isDone();
        }else{
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<byte[]> getRecentMediaForKeyword() {
        return Optional.ofNullable(this.redditSubscriptionDeamon.getRecentMediaForSubscribedKeywords())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(entry -> BlobConverterImpl.downloadToByte(entry.getUrl()))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token getToken() {
        return this.token;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setToken(Token token){
        this.token = token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApiName() {
        return REDDIT.getValue();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void stopSearch() {
        logger.info("Stop searched was executed.");
        if (scheduledFuture != null && !scheduledFuture.isCancelled())
            scheduledFuture.cancel(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startSearch() {
        logger.info("Start search was executed.");
        if (interval == null) {
            scheduledFuture = executor.scheduleAtFixedRate(this.redditSubscriptionDeamon,0, DEFAULT_INTERVALL, TimeUnit.MINUTES);
            Log.i("startsaerch","Start search was executed if.");
            System.out.println(executor);
        } else {
            Log.i("startsaerch","Start search was executed else.");
            scheduledFuture = executor.scheduleAtFixedRate(this.redditSubscriptionDeamon, 0, interval, TimeUnit.MINUTES);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unsubscribeKeyword(String keyword) {
        if (scheduledFuture == null) {
            if(this.getAllSubscribedKeywordsAndLastTimeChecked().containsKey(keyword)){
                this.getAllSubscribedKeywordsAndLastTimeChecked().remove(keyword);
                logger.info("Removed keyword '" + keyword + "' from Reddit.");
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (isSchedulerRunning())
                stopSearch();
            try {
                if(this.getAllSubscribedKeywordsAndLastTimeChecked().containsKey(keyword)){
                    this.getAllSubscribedKeywordsAndLastTimeChecked().remove(keyword);
                    logger.info("Removed keyword '" + keyword + "' from Reddit.");
                    return true;
                }

            } catch (Exception e) {
                logger.info(keyword + " was not found in keywordlist.");
            }
            if (isSchedulerRunning())
                startSearch();
        }
        return false;
    }
}