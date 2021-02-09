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

package de.htw.berlin.steganography.apis.reddit.models;
/*
 *
 * @author Mario Teklic
 */

import java.util.List;

/**
 * Represens a JSON response from reddit.com as a Java Model.
 * Has several properties which are not used for this application at this point.
 * Maybe they can be usefull later, therefore they should be kept.
 */
public class RedditGetResponse {
    private String kind;
    private ResponseData data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ResponseData getData() {
        return data;
    }

    public void setData(ResponseData data) {
        this.data = data;
    }

    public static class ResponseData {
        private String modhash;
        private List<ResponseChildData> children;
        private String after;
        private String before;

        public String getModhash() {
            return modhash;
        }

        public void setModhash(String modhash) {
            this.modhash = modhash;
        }

        public List<ResponseChildData> getChildren() {
            return children;
        }

        public void setChildren(List<ResponseChildData> children) {
            this.children = children;
        }

        public String getAfter() {
            return after;
        }

        public void setAfter(String after) {
            this.after = after;
        }

        public String getBefore() {
            return before;
        }

        public void setBefore(String before) {
            this.before = before;
        }
    }

    public static class ResponseChildData {
        private String kind;
        private ChildData data;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public ChildData getData() {
            return data;
        }

        public void setData(ChildData data) {
            this.data = data;
        }
    }

    public static class ChildData {
        private String url_overridden_by_dest;
        private String title;
        private String subreddit_id;
        private String name;
        private String created;
        private String url;
        private String created_utc;
        private PreviewData preview;

        public String getUrl_overridden_by_dest() {
            return url_overridden_by_dest;
        }

        public void setUrl_overridden_by_dest(String url_overridden_by_dest) {
            this.url_overridden_by_dest = url_overridden_by_dest;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubreddit_id() {
            return subreddit_id;
        }

        public void setSubreddit_id(String subreddit_id) {
            this.subreddit_id = subreddit_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCreated_utc() {
            return created_utc;
        }

        public void setCreated_utc(String created_utc) {
            this.created_utc = created_utc;
        }

        public PreviewData getPreview() {
            return preview;
        }

        public void setPreview(PreviewData preview) {
            this.preview = preview;
        }
    }

    public static class PreviewData{
        private List<ImageData> images;

        /**
         * Returns first Imageentry, which ist the source image.
         */
        public ImageData getImages() {
            return images.get(0);
        }

        public void setImages(List<ImageData> images) {
            this.images = images;
        }
    }

    public static class ImageData{
        private SourceData source;

        public SourceData getSource() {
            return source;
        }

        public void setSource(SourceData source) {
            this.source = source;
        }
    }

    public static class SourceData{
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

