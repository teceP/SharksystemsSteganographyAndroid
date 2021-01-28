package de.htw.berlin.steganography.apis;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.htw.berlin.steganography.apis.models.Token;

public class SocialMediaModel {

    private Map<String, Long> allSubscribedKeywordsAndLastTimeChecked = new HashMap<>();/**
     * Decoded messages from a media
     */
    protected List<String> message = new ArrayList<>();



    /**
     * Sets a new message list and updates all listeners about these new messages
     * @param messageList
     */
    public void setMessages(List<String> messageList){
        this.message = messageList;
    }

    public List<String> getMessage(){
        return message;
    }



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


}
