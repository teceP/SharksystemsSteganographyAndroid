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

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.reddit.models.RedditAboutResponse;
import de.htw.berlin.steganography.apis.reddit.models.RedditGetResponse;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Mario Teklic
 */


public class RedditUtil extends BaseUtil {

    private final static Logger logger = Logger.getLogger(Reddit.class.getName());

    public RedditUtil(SocialMedia socialMedia) {
        super(socialMedia);
    }

    /**
     * Returns the downloadable and decoded URL of an image from a reddit post
     * @param data GET Response of reddit
     * @return
     */
    public String getUrl(RedditGetResponse.ResponseChildData data){
        return this.decodeUrl(data.getData().getPreview().getImages().getSource().getUrl());
    }

    /**
     *  Returns the timestamp from a reddit post
     * @param data GET Response of reddit
     * @return
     */
    public MyDate getTimestamp(RedditGetResponse.ResponseChildData data){
        String info = data.getData().getCreated();
        if(info == null || info.isEmpty()){
            info = data.getData().getCreated_utc();
        }
        return this.getTimestamp(info);
    }

    /**
     /**
     * Converts a response String in json-format from an Imgur Response, to PostEntry-Objects
     * Filters all types but .png out of the list.
     * @param responseString JSON String (Reddit response)
     * @return Returns a sorted list of Postentries (downloadlinks and timestamps) from a json-String
     */
    public List<PostEntry> getPosts(String responseString){
        List<PostEntry> postEntries = new ArrayList<>();
        try{
            RedditGetResponse responseArray = new Gson().fromJson(responseString, RedditGetResponse.class);

            for(RedditGetResponse.ResponseChildData child : responseArray.getData().getChildren()){
                if(child != null && !this.hasNullObjects(child)){
                    postEntries.add(new PostEntry(this.decodeUrl(this.getUrl(child)), this.getTimestamp(child), ".png"));
                }
            }

            //postEntries.stream().forEach(postEntry -> System.out.println(postEntry.toString()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return postEntries;
    }

    /**
     * Proofs if there are null objects in a GET responses child data
     * Tests only objcets which are used in the process after this method.
     * Only date and download-url.
     * @param responseChildData
     * @return true if HAS null objects, false if NOT. False is in this case GOOD.
     * @throws Exception while trying to initialize variables.
     */
    public boolean hasNullObjects(RedditGetResponse.ResponseChildData responseChildData){
        MyDate myDate = null;
        String url = null;
        try{
            myDate = this.getTimestamp(responseChildData);
            url = this.getUrl(responseChildData);
        }catch (Exception e){
            /*
            logger.info("Post entry has null object.");
            if(myDate == null && url != null)
                logger.info("Date was null. URL: " + url);
            if(myDate != null && url == null)
                logger.info("URL was null. No media found. This happens, when this entry was is not a picture but for an example a comment. Date: " + myDate.getTime());
            if(myDate == null && url == null)
                logger.info("URL and Date are null.");
              */
            return true;
        }

        if(myDate == null && url != null)
            return true;
        if(myDate != null && url == null)
            return true;
        if(myDate == null && url == null)
            return true;

        return false;
    }

    /**
     * Tests if on a specific subreddit, image uploads are allowed or not.
     * @param subreddit E.g. 'nature' but NOT 'r/nature'! Subreddit prefix will be added in this method. Dont need to provide before.
     * @return true if is allowed.
     */
    public boolean isImageUploadAllowed(String subreddit){
        //key allows_images isnt supported anymore
        return true;
        /*try {
            URL url = new URL(RedditConstants.BASE +
                    RedditConstants.SUBREDDIT_PREFIX + subreddit +
                    "/about" + RedditConstants.AS_JSON);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(RedditConstants.GET);
            con.setRequestProperty("User-agent", RedditConstants.APP_NAME);
            con.setDoOutput(true);

            String responseString = "";

            if (!this.hasErrorCode(con.getResponseCode())) {
                responseString = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining());
                logger.info("Response Code: " + con.getResponseCode() + ". No error.");
            } else {
                logger.info("Response Code: " + con.getResponseCode() + ". Has error.");
                return false;
            }

            RedditAboutResponse redditAboutResponse = new Gson().fromJson(responseString, RedditAboutResponse.class);
            return redditAboutResponse.getData().isAllow_images();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;*/
    }
}
