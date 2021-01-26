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

import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.reddit.models.RedditGetResponse;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

/**
 * @author Mario Teklic
 */

/**
 * Helperclass for Reddit and RedditSubscriptionDeamon
 */
public class RedditUtil extends BaseUtil {

    public RedditUtil() {
    }

    /**
     * Returns the downloadable and decoded URL of an image from a reddit post
     * @param
     * @return
     */
    public String getUrl(JsonObject jsonObject){
        if(jsonObject.has("url_overridden_by_dest")){
            return this.decodeUrl(jsonObject.get("url_overridden_by_dest").getAsString());
        }

        Log.i("1. RedditSubscriptionDeamon run", "url_overridden_by_dest was null. Returning empty string as url...");
        return "";
    }

    /**
     *  Returns the timestamp from a reddit post
     * @return
     */
    public MyDate getTimestamp(JsonObject jsonObject){
        if(jsonObject.has("created")){
            return new MyDate(new Date(jsonObject.get("created").getAsLong()));
        }else{
            return new MyDate(new Date(jsonObject.get("created_utc").getAsLong()));
        }
    }

    /**
     /**
     * Converts a response String in json-format from an Imgur Response, to PostEntry-Objects
     * Filters all types but .png out of the list.
     * @param responseString JSON String (Reddit response)
     * @return Returns a sorted list of Postentries (downloadlinks and timestamps) from a json-String
     */
    public List<PostEntry> getPosts(String keyword, String responseString){
        //Log.i("9. RedditUtil getPosts called with URL String", responseString);
        List<PostEntry> postEntries = new ArrayList<>();
        try{
            RedditGetResponse responseArray = new Gson().fromJson(responseString, RedditGetResponse.class);
            JsonObject jsonObject = JsonParser.parseString(responseString).getAsJsonObject();

            for(RedditGetResponse.ResponseChildData child : responseArray.getData().getChildren()){
                if(child != null
                        && child.getData().getTitle().contains(keyword)
                        && !this.hasNullObjects(jsonObject)){
                    postEntries.add(new PostEntry(this.decodeUrl(this.getUrl(jsonObject)), this.getTimestamp(jsonObject), ".png"));
                }
            }

            //postEntries.stream().forEach(postEntry -> System.out.println(postEntry.toString()));
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.i("10. RedditUtil getPosts return postEntries size", String.valueOf(postEntries.size()));
        return postEntries;
    }

    /**
     * Proofs if there are null objects in a GET responses child data
     * Tests only objcets which are used in the process after this method.
     * Only date and download-url.
     * @return true if HAS null objects, false if NOT. False is in this case GOOD.
     * @throws Exception while trying to initialize variables.
     */
    public boolean hasNullObjects(JsonObject jsonObject){
        MyDate myDate = null;
        String url = null;
        try{
            myDate = this.getTimestamp(jsonObject);
            url = this.getUrl(jsonObject);
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
}
