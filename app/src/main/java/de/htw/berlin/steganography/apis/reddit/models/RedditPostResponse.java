/*
 * Copyright (c) 2020
 * Contributed by NAME HERE
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
 * Represents a POST response by the Reddit network
 */
public class RedditPostResponse {

    private JsonItems json;

    public JsonItems getJson() {
        return json;
    }

    public void setJson(JsonItems json) {
        this.json = json;
    }

    public class JsonItems{

        private List<String> errors;
        private RedditPostData data;

        public RedditPostData getData() {
            return data;
        }

        public void setData(RedditPostData data) {
            this.data = data;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }

    public class RedditPostData{

        private String url;
        private int drafts_count;
        private String id;
        private String name;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getDrafts_count() {
            return drafts_count;
        }

        public void setDrafts_count(int drafts_count) {
            this.drafts_count = drafts_count;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
