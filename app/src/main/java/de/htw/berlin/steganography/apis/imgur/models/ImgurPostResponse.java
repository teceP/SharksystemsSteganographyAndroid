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

package de.htw.berlin.steganography.apis.imgur.models;

/**
 * @author Mario Teklic
 */

/**
 * Represtens a POST response from Imgur API
 */
public class ImgurPostResponse {
    public boolean success;
    public int status;
    public UploadedImage data;

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public UploadedImage getData() {
        return data;
    }

    public static class UploadedImage {
        public String id;
        public String title;
        public String description;
        public String type;
        public boolean animated;
        public int width;
        public int height;
        public int size;
        public int views;
        public int bandwidth;
        public String vote;
        public boolean favorite;
        public String account_url;
        public String deletehash;
        public String name;
        public String link;

        public String getId(){
            return this.id;
        }

        public String getLink() {
            return link;
        }

        @Override
        public String toString(){
            return "Link: " + this.getLink();
        }
    }
}
