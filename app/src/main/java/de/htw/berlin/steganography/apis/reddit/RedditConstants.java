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

/**
 * @author Mario Teklic
 */


public interface RedditConstants {
    String APP_NAME = "SharksystemsStega";
    String BASE = "https://www.reddit.com";
    String OAUTH_BASE = "https://oauth.reddit.com";
    String SUBREDDIT_PREFIX = "/r/";
    String UPLOAD_PATH = "/api/submit";
    String GET = "GET";
    String AS_JSON = ".json";
    String SUBREDDIT = "test";
}
