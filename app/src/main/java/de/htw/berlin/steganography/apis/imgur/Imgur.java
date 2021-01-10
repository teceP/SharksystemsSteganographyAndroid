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
import de.htw.berlin.steganography.apis.models.Token;
import de.htw.berlin.steganography.apis.imgur.models.ImgurPostResponse;
import de.htw.berlin.steganography.apis.interceptors.BearerInterceptor;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import de.htw.berlin.steganography.apis.utils.BlobConverterImpl;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static de.htw.berlin.steganography.apis.models.APINames.IMGUR;

/**
 * @author Mario Teklic
 */

/**
 * Imgur Social Media API implementation
 */
public class Imgur extends SocialMedia {

    private final static Logger logger = Logger.getLogger(Imgur.class.getName());

    /**
     * Subscription deamon can search automatically for new posts for given keywords in an given interval
     * Asynchron
     */
    private ImgurSubscriptionDeamon imgurSubscriptionDeamon;

    /**
     * Is used for uploading posts to Imgur
     */
    private Token token;

    /**
     * Handles the automatically process of looking for new post entries
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
     * Utilities
     */
    private ImgurUtil imgurUtil;

    /**
     * Standard constructor prepares the Subscriptiondeamon but does not start it
     */
    public Imgur() {
        imgurUtil = new ImgurUtil(this);
        imgurSubscriptionDeamon = new ImgurSubscriptionDeamon(this);
        executor = Executors.newScheduledThreadPool(1);
    }

    /**
     * Inject mocked Deamon for testing
     *
     * @param deamon
     */
    public void injectSubscriptionDeamon(ImgurSubscriptionDeamon deamon) {
        this.imgurSubscriptionDeamon = deamon;
    }

    @Override
    public boolean postToSocialNetwork(byte[] media, de.htw.berlin.steganography.apis.MediaType mediaType, String keyword) {
        if (this.getToken() == null || this.getToken().getToken() == null) {
            logger.info("No Token was set!");
        }

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BearerInterceptor()).build();

        String filename = "tmp_" + System.currentTimeMillis() + ".png";
        RequestBody body = null;
        try {
            body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", filename, RequestBody.create(media, MediaType.parse("image/*")))
                    .addFormDataPart("title", keyword)
                    .addFormDataPart("description", "Hello World!")
                    .build();

            Request request = new Request.Builder()
                    .headers(Headers.of("Authorization", ("Bearer " + this.token.getToken())))
                    .url(ImgurConstants.UPLOAD_URL + ".json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            ImgurPostResponse ipr = gson.fromJson(response.body().string(), ImgurPostResponse.class);

            int code = response.code();

            if (BaseUtil.hasErrorCode(code)) {
                logger.info("Not uploaded successfully. Errorcode: " + code);
                return false;
            } else {
                logger.info("Successfull uploaded.\nURL: " + ipr.getData().getLink());
                return shareWithCommunity(ipr, keyword);
            }
        } catch (IOException e) {
            logger.info("Error during posting to imgur with token (authenticated).");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Shares an uploaded media with the community/makes it public and searchable
     */
    private boolean shareWithCommunity(ImgurPostResponse postResponse, String keyword) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new BearerInterceptor()).build();

        RequestBody body = null;
        try {
            body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title", keyword)
                    .addFormDataPart("topic", keyword)
                    .addFormDataPart("terms", "1")
                    .addFormDataPart("tags", keyword)
                    .build();

            Request request = new Request.Builder()
                    .headers(Headers.of("Authorization", ("Bearer " + this.token.getToken())))
                    .url("https://api.imgur.com/3/gallery/image/" + postResponse.getData().getId())
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            int code = response.code();
            if (BaseUtil.hasErrorCode(code)) {
                logger.info("Could not share with community. Errorcode: " + code);
                return false;
            } else {
                logger.info("Shared with community. Code: " + code);
                return true;
            }
        } catch (Exception e) {
            logger.info("Exception during share with community request.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Uploads a picture to Imgur anonymoulsy
     *
     * @param media   File as bytearray
     * @param keyword Keyword will be used as the title
     * @return JSON response from Imgur as a POJO
     */
    public static ImgurPostResponse uploadPicture(byte[] media, String keyword) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        String filename = "tmp_" + System.currentTimeMillis() + ".png";
        RequestBody body = null;
        try {
            body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", filename, RequestBody.create(media, MediaType.parse("image/*")))
                    .addFormDataPart("title", keyword)
                    .addFormDataPart("description", "Hello World!")
                    .build();

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Sharksystems Steganography by Anon-User")
                    .headers(Headers.of("Authorization", ("Client-ID " + ImgurConstants.CLIENT_ID)))
                    .url(ImgurConstants.UPLOAD_URL)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            String res = response.body().string();
            ImgurPostResponse ipr = gson.fromJson(res, ImgurPostResponse.class);

            if (ipr == null || ipr.data == null || ipr.data.link == null || BaseUtil.hasErrorCode(ipr.status)) {
                logger.info("Unsuccessfull uploaded!");
                logger.info("Request was: " + request.toString());
                logger.info("Response String was: " + res);
            } else {
                logger.info("Successfull uploaded anonymously.\nURL: " + ipr.data.link);
            }
            return ipr;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Listens for new post entries in imgur network for stored keywords.
     * Asynchron.
     *
     * @param interval Interval in minutes
     */
    @Override
    public void changeSchedulerPeriod(Integer interval) {
        if(isSchedulerRunning())
            stopSearch();

        this.interval = interval;

        if(isSchedulerRunning())
            startSearch();
    }

    /**
     * Returns if the Subscription deamon is running
     * @return
     */
    public boolean isSchedulerRunning(){
        return !scheduledFuture.isCancelled() && !scheduledFuture.isDone();
    }



    @Override
    public List<byte[]> getRecentMediaForKeyword(String keyword) {
        return Optional.ofNullable(this.imgurSubscriptionDeamon.getRecentMediaForSubscribedKeywords(keyword))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(entry -> BlobConverterImpl.downloadToByte(entry.getUrl()))
                .collect(Collectors.toList());
    }

    @Override
    public Token getToken() {
        return this.token;
    }

    @Override
    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public String getApiName() {
        return IMGUR.getValue();
    }

    @Override
    public List<String> getAllSubscribedKeywords() {
        try{
            return this.allSubscribedKeywords;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void setBlogName(String blogname) {
        //Only used in tumblr implementation
    }

    @Override
    public void stopSearch() {
        logger.info("Stop searched was executed.");
        if (scheduledFuture != null && !scheduledFuture.isCancelled())
            scheduledFuture.cancel(false);
    }

    @Override
    public void startSearch() {
        logger.info("Start search was executed.");
        if (interval == null) {
            System.out.println(executor);
            scheduledFuture = executor.scheduleAtFixedRate(this.imgurSubscriptionDeamon,0, DEFAULT_INTERVALL, TimeUnit.MINUTES);
            System.out.println(executor);

        } else {
            scheduledFuture = executor.schedule(this.imgurSubscriptionDeamon, interval, TimeUnit.MINUTES);
        }
    }

    @Override
    public boolean unsubscribeKeyword(String keyword) {
        if (scheduledFuture == null) {
            if (allSubscribedKeywords.stream().anyMatch(s -> s.equals(keyword))) {
                allSubscribedKeywords.remove(keyword);
                logger.info("Removed keyword '" + keyword + "' from Imgur.");
                return true;
            }
        } else {
            if (isSchedulerRunning())
                stopSearch();

            try {
                if (getAllSubscribedKeywords().stream().anyMatch(s -> s.equals(keyword))) {
                    unsubscribeKeyword(keyword);
                    logger.info("Removed keyword '" + keyword + "' from Imgur.");
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
