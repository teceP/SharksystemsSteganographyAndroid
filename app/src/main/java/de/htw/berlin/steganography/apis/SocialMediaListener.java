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

import java.util.List;

public interface SocialMediaListener {
    /**
     * Function that gets called if there is a new message in the SocialMediaModel for the given SocialMedia
     * @param socialMedia
     * @param messages
     */
    public void updateSocialMediaMessage(SocialMedia socialMedia, List<String> messages);

    /**
     * Function that gets called if there is a new lastTimChecked for the keyword in the SocialMediaModel for the given SocialMedia
     * @param socialMedia
     * @param keyword
     * @param lastTimeChecked
     */
    public void updateSocialMediaLastTimeChecked(SocialMedia socialMedia, String keyword, long lastTimeChecked);
}
