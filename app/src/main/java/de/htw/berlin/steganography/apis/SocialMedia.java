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

import android.util.Log;

import de.htw.berlin.steganography.apis.models.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;


public abstract class SocialMedia {
 
    protected Map<String, Long> allSubscribedKeywordsAndLastTimeChecked = new HashMap<>();
    protected List<String> message = new ArrayList<>();
    public static final Integer DEFAULT_INTERVALL = 5;

    protected List<SocialMediaListener> socialMediaListeners = new ArrayList<SocialMediaListener>();

    public void addAsListener(SocialMediaListener socialMediaListener){
        socialMediaListeners.add(socialMediaListener);
    }

    public void removeAsListener(SocialMediaListener socialMediaListener){
        socialMediaListeners.remove(socialMediaListener);
    }

    private void updateListenersMessages(){
        for(SocialMediaListener socialMediaListener : socialMediaListeners){
            socialMediaListener.updateSocialMediaMessage(this,message);
        }
    }

    private void updateListenersLastTimeChecked(String keyword){
        Log.i("SocialMedia updateListenersLastTimeChecked", "for keyword: " + keyword);
        for(SocialMediaListener socialMediaListener : socialMediaListeners){
            socialMediaListener.updateSocialMediaLastTimeChecked(this, keyword, allSubscribedKeywordsAndLastTimeChecked.get(keyword));
        }
        Log.i("SocialMedia updateListenersLastTimeChecked", "finished");
    }

    public void setMessages(List<String> messageList){
        this.message = messageList;
        updateListenersMessages();
    }

    public void addMessages(List<String> messageList){
        for(String string: messageList){
            this.message.add(string);
        }
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
     * Get Medias posted under keyword

     * @return true if successful
     */
    public abstract List<byte[]> getRecentMediaForKeyword();

    public abstract void stopSearch();

    public abstract void startSearch();

    public abstract void changeSchedulerPeriod(Integer interval);

    public abstract String getApiName();


    public abstract void setBlogName(String blogname);
}
