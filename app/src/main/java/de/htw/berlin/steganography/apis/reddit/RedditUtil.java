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
    public String getUrl(RedditGetResponse.ResponseChildData child){
        Log.i("getUrl", "Get URL Start");
        String url = "";

        //Zuerst mit overridden by dest versuchen, falls .png im link ist
        if(child.getData().getUrl_overridden_by_dest() != null
                && !child.getData().getUrl_overridden_by_dest().isEmpty()
                && (child.getData().getUrl_overridden_by_dest().toLowerCase().contains(".png")
                || (child.getData().getUrl_overridden_by_dest().toLowerCase().contains("https://imgur.com/") && !child.getData().getUrl_overridden_by_dest().toLowerCase().contains(".mp4")))){

            //Imgurbilder können einfach mit .png appended werden.
            //Das ist eine Ausnahme, da wir das in diesem Falle wissen und wir den Dienst selbst zum hochladen benutzen
            if(child.getData().getUrl_overridden_by_dest().toLowerCase().contains("https://imgur.com/")
                    && !child.getData().getUrl_overridden_by_dest().toLowerCase().contains(".png")){
                child.getData().setUrl_overridden_by_dest(child.getData().getUrl_overridden_by_dest() + ".png");
                Log.i("getUrl", "Appended .png to imgur link.");
            }

            Log.i("IMG URL set by 'overridden by dest'", (child.getData().getUrl_overridden_by_dest()));
            url = this.decodeUrl(child.getData().getUrl_overridden_by_dest());
        }

        //falls overridden by dest nicht vorhanden oder kein .png enthalten ist
        //mit preview versuchen
        if(url.equals("")
                && child.getData().getPreview() != null
                && child.getData().getPreview().getImages().getSource() != null
                && child.getData().getPreview().getImages().getSource().getUrl() != null
                && !child.getData().getPreview().getImages().getSource().getUrl().isEmpty()
                && child.getData().getPreview().getImages().getSource().getUrl().toLowerCase().contains(".png")){
            url = this.decodeUrl(child.getData().getPreview().getImages().getSource().getUrl());
            Log.i("IMG URL set by 'preview.source'", (child.getData().getPreview().getImages().getSource().getUrl()));
        }

        Log.i("Returning ", (url.equals("") ? "empty String, due to no .png found in child data" : "following String as image url: " + url));

        Log.i("getUrl", "Get URL End");

        return url;
    }

    /**
     *  Returns the timestamp from a reddit post
     * @return
     */
    public MyDate getTimestamp(RedditGetResponse.ResponseChildData child){
        if(child.getData().getCreated() != null && !child.getData().getCreated().isEmpty()){
            child.getData().setCreated(BaseUtil.cutTimestamp(child.getData().getCreated()));
            Log.i("CREATED: ", child.getData().getCreated() );
            return new MyDate(new Date(Long.parseLong(child.getData().getCreated())));
        }else{
            child.getData().setCreated_utc(BaseUtil.cutTimestamp(child.getData().getCreated_utc()));
            Log.i("CREATED_UTC: ", child.getData().getCreated_utc() );
            return new MyDate(new Date(Long.parseLong(child.getData().getCreated_utc())));
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

            for(RedditGetResponse.ResponseChildData child : responseArray.getData().getChildren()){
               /* Log.i("asdasdasdasd", "===========================================");
                Log.i("asdasdasdasd title:", (child.getData().getTitle()) + "");
                Log.i("asdasd nicht null oben:", (child != null) + "");
                Log.i("asdasd title contains", (child.getData().getTitle().toLowerCase().contains(keyword.toLowerCase()) + ""));
                Log.i("asdasdasdasd", "===========================================");*/

                if(child != null
                        && child.getData().getTitle().toLowerCase().contains(keyword.toLowerCase())){
                    String url = this.decodeUrl(this.getUrl(child));
                    if(!url.equals("") || !url.isEmpty()){
                        postEntries.add(new PostEntry(url, this.getTimestamp(child), ".png"));
                        Log.i("Hallo 4 (loop) ", "added " + new PostEntry(url, this.getTimestamp(child), ".png").toString());
                    }else{
                        Log.i("Hallo 4 (loop) ", "NOT added bc url was empty ");
                    }
                }
            }
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
    public boolean hasNullObjects(RedditGetResponse.ResponseChildData child){
        MyDate myDate;
        String url;
        try{
            url = this.getUrl(child);
            myDate = this.getTimestamp(child);
        }catch (Exception e){
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
