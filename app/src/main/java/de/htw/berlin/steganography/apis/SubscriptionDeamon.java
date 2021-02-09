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

package de.htw.berlin.steganography.apis;

import de.htw.berlin.steganography.apis.models.PostEntry;

import java.util.List;

/**
 * @author Mario Teklic
 */

public interface SubscriptionDeamon extends Runnable {

    /**
     * Searches for new post entries for a specific keyword, or for all stored keywords.
     *

     * @return All new post entries for all subscribed keywords
     */
    List<PostEntry> getRecentMediaForSubscribedKeywords();

    /**
     * Getter & Setter
     */
    boolean isNewPostAvailable();
}
