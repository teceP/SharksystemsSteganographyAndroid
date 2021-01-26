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

package de.htw.berlin.steganography.apis.models;

/**
 * @author Mario Teklic
 */

/**
 * Represents a post from reddit
 *
 */
public class PostEntry implements Comparable<PostEntry>{

    /**
     * A downloadable URL for a media i.e. an image
     */
    private String url;

    /**
     * The timestamp, when the post was made as a long value
     * There is no setter because the timestamp should not be changeable
     */
    private final MyDate date;

    private String type;

    public PostEntry(String url, MyDate date, String type){
        this.url = url;
        this.date = date;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MyDate getDate() {
        return date;
    }

    /**
     *
     * @param postEntry
     * @return 1 if equal
     * @return -1 if not equal
     *
     * {@inheritDoc}
     */
    @Override
    public int compareTo(PostEntry postEntry) {
        return this.getDate().compareTo(postEntry.getDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return "URL: " + getUrl() + "\n"
            + "Timestamp: " + getDate().toString() + "\n"
            + "In ms: " + getDate().getTime();
    }
}
