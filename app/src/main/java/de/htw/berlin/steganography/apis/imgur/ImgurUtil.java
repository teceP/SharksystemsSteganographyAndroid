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
import de.htw.berlin.steganography.apis.imgur.models.ImgurGetResponse;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.utils.BaseUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * @author Mario Teklic
 */

/**
 * Imgur Utilities
 */
public class ImgurUtil extends BaseUtil {

    public ImgurUtil() {
    }

    /**
     * Converts a response String in json-format from an Imgur Response, to PostEntry-Objects
     * @param responseString JSON String (Imgur response)
     * @return Returns a sorted list of Postentries (downloadlinks and timestamps) from a json-String
     */
    public List<PostEntry> getPosts(String responseString){
        List<PostEntry> postEntries = new ArrayList<>();
        ImgurGetResponse responseObject = new Gson().fromJson(responseString, ImgurGetResponse.class);

        for(ImgurGetResponse.ImgurData child : responseObject.getData()){
            if(child != null && child.getImages() != null){
                if(supportedFormat(child.getImages())){
                    postEntries.add(new PostEntry(child.getImages().get(0).getLink(), this.getTimestamp(child.getDatetime()), child.getDatetime()));
                }
            }
        }
        return postEntries;
    }

    /**
     * Returns which media types are supported by this network
     */
    private boolean supportedFormat(List<ImgurGetResponse.ImgurImages> data) {
        String[] supported = {"image/png"};
        ImgurGetResponse.ImgurImages i = data.get(0);
        if(i.getType() == null
                || i.getLink() == null){
            return false;
        }

        if(Arrays.asList(supported).contains(i.getType())
                && !i.getLink().isEmpty()){
            return true;
        }
        return false;
    }
}
