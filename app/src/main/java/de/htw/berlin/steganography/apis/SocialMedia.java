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

package de.htw.berlin.steganography.apis;

import android.util.Log;

import de.htw.berlin.steganography.apis.models.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic Socialmedia abstract implementation
 */
public abstract class SocialMedia {

    /**
     * All Subscribed keyword and its last time checked-timestamp
     */
    protected Map<String, Long> allSubscribedKeywordsAndLastTimeChecked = new HashMap<>();

    /**
     * Decoded messages from a media
     */
    protected List<String> message = new ArrayList<>();

    /**
     * Default search interval for the automatic search
     */
    public static final Integer DEFAULT_INTERVALL = 5;

    /**
     * Listeners which gets updated when a new message has arrived
     */
    protected List<SocialMediaListener> socialMediaListeners = new ArrayList<SocialMediaListener>();

    /**
     * Add a new listener
     * @param socialMediaListener
     */
    public void addAsListener(SocialMediaListener socialMediaListener){
        socialMediaListeners.add(socialMediaListener);
    }

    /**
     * Remove a listener
     * @param socialMediaListener
     */
    public void removeAsListener(SocialMediaListener socialMediaListener){
        socialMediaListeners.remove(socialMediaListener);
    }

    /**
     * Update all listeners messages
     */
    private void updateListenersMessages(){
        for(SocialMediaListener socialMediaListener : socialMediaListeners){
            socialMediaListener.updateSocialMediaMessage(this,message);
        }
    }

    /**
     * Upadte all listeners last time checked
     * @param keyword
     */
    private void updateListenersLastTimeChecked(String keyword){
        Log.i("SocialMedia updateListenersLastTimeChecked", "for keyword: " + keyword);
        for(SocialMediaListener socialMediaListener : socialMediaListeners){
            socialMediaListener.updateSocialMediaLastTimeChecked(this, keyword, allSubscribedKeywordsAndLastTimeChecked.get(keyword));
        }
        Log.i("SocialMedia updateListenersLastTimeChecked", "finished");
    }

    /**
     * Sets a new message list and updates all listeners about these new messages
     * @param messageList
     */
    public void setMessages(List<String> messageList){
        this.message = messageList;
        updateListenersMessages();
    }

    /**
     * Adds a new message list to the message list and updates all listeners about the whole message list
     * @param messageList
     */
    public void addMessages(List<String> messageList){
        this.message.addAll(messageList);
        updateListenersMessages();
    }

    public List<String> getMessage(){
        return message;
    }

    public abstract Token getToken();

    public abstract void setToken(Token token);

    /**
     * Post media on this Social Media under the keyword
     * @param media data to upload
     * @param keyword keyword to search this post by
     * @return true if successful
     */
    public abstract boolean postToSocialNetwork(byte[] media, MediaType mediaType, String keyword);

    /**
     * Sets keywords and last time checked
     * @param keywordsAndLastTimeChecked
     */
    public void putAllSubscribedKeywordsAndLastTimeChecked(Map<String,Long> keywordsAndLastTimeChecked){
        this.allSubscribedKeywordsAndLastTimeChecked = keywordsAndLastTimeChecked;
    }

    public Map<String, Long> getAllSubscribedKeywordsAndLastTimeChecked(){
        return allSubscribedKeywordsAndLastTimeChecked;
    }

    public List<String> getAllSubscribedKeywordsAsList(){
        List<String> keywordList = new ArrayList<>();
        for (String key : getAllSubscribedKeywordsAndLastTimeChecked().keySet()) {
            keywordList.add(key);
        }
        return keywordList;
    }

    public void putKeywordAndLastTimeChecked(String keyword, Long lastTimeChecked){
        allSubscribedKeywordsAndLastTimeChecked.put(keyword, lastTimeChecked);
    }

    public boolean setLastTimeCheckedForKeyword(String keyword, Long lastTimeChecked){
        if(allSubscribedKeywordsAndLastTimeChecked.containsKey(keyword)){
            allSubscribedKeywordsAndLastTimeChecked.put(keyword, lastTimeChecked);
            this.updateListenersLastTimeChecked(keyword);
            return true;
        }
        else{
            return false;
        }
    }

    public Long getLastTimeCheckedForKeyword(String keyword){
        if(allSubscribedKeywordsAndLastTimeChecked.containsKey(keyword)) {
            Log.i("SocialMedia getLastTimeCheckedForKeyword map contains keyword: ", keyword);
            if(allSubscribedKeywordsAndLastTimeChecked.get(keyword)!=null) {
                Log.i("SocialMedia getLastTimeCheckedForKeyword keyword: "+ keyword+ " lastTimeChecked", String.valueOf(allSubscribedKeywordsAndLastTimeChecked.get(keyword)));
                return allSubscribedKeywordsAndLastTimeChecked.get(keyword);
            }
            else{
                return new Long(0);
            }
        }
        return null;
    }


    /**
     * Subscribe to a keyword (Hashtag / Title / ...)
     * @param keyword keyword to subscribe to
     * @return true if successful
     */
    public boolean subscribeToKeyword(String keyword){
        if(!allSubscribedKeywordsAndLastTimeChecked.containsKey(keyword)){
            this.allSubscribedKeywordsAndLastTimeChecked.put(keyword, new Long(0));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Unsubscribes for a keyword
     * @param keyword
     * @return false if this keyword wasnt found in the subscribed-keywords-list
     */
    public boolean unsubscribeKeyword(String keyword){
        if(allSubscribedKeywordsAndLastTimeChecked.containsKey(keyword)){
            allSubscribedKeywordsAndLastTimeChecked.remove(keyword);
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Get Medias posted with keyword
     * @return true if successful
     */
    public abstract List<byte[]> getRecentMediaForKeyword();

    /**
     * Stops the automatic search
     */
    public abstract void stopSearch();

    /**
     * Starts the automatic search.
     * If no interval was set, the default interval (5 minutes) will be used.
     */
    public abstract void startSearch();

    /**
     * Changes the automatic search interval
     * @param interval
     */
    public abstract void changeSchedulerPeriod(Integer interval);

    public abstract String getApiName();
}
