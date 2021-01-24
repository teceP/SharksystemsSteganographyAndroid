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

/**
 * Reddit 'About' response, which has information about a specific
 * subreddit such as 'allow_images': some subreddits doesnt allow to
 * upload images. If the user wants to upload an image, this should
 * be checked before the upload is made
 */
public class RedditAboutResponse {

    private AboutData data;

    public AboutData getData() {
        return data;
    }

    public void setData(AboutData data) {
        this.data = data;
    }

    public class AboutData{
        private boolean allow_images;
        private boolean allow_videos;

        public boolean isAllow_images() {
            return allow_images;
        }

        public void setAllow_images(boolean allow_images) {
            this.allow_images = allow_images;
        }

        public boolean isAllow_videos() {
            return allow_videos;
        }

        public void setAllow_videos(boolean allow_videos) {
            this.allow_videos = allow_videos;
        }
    }
}
