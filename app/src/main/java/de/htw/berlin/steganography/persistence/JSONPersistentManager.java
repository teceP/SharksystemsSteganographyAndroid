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

package de.htw.berlin.steganography.persistence;

import android.util.Log;

import de.htw.berlin.steganography.apis.models.APINames;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JSONPersistentManager {
    private Map<String, Map<String, Long>> jsonMap = new ConcurrentHashMap<>();

    private JSONPersistentHelper jsonPersistentHelper;


    private static final JSONPersistentManager instance = new JSONPersistentManager();

    private JSONPersistentManager() {
    }

    public static JSONPersistentManager getInstance() {
        return instance;
    }

    /**
     * sets the JsonPersistentHelper
     * @param jsonPersistentHelper
     */
    public void setJsonPersistentHelper(JSONPersistentHelper jsonPersistentHelper){
        this.jsonPersistentHelper = jsonPersistentHelper;
        try {
            jsonStringToJsonMap(jsonPersistentHelper.readFromJsonFile());
            if(jsonMap==null){
                jsonMap = new ConcurrentHashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns the entire ConcurrentHashMap
     * @return
     */
    public Map<String, Map<String, Long>> getJsonMap(){
        return jsonMap;

    }

    /**
     * sets the keyword and the lastTimeChecked for the speicified keyword and Social Media Name
     * @param apiName
     * @param keyword
     * @param lastTimeChecked
     */
    public void setKeywordForAPI(APINames apiName, String keyword, Long lastTimeChecked){
        if(!jsonMap.containsKey(apiName.getValue())){
            jsonMap.put(apiName.getValue(),new HashMap<String, Long>());
        }
        jsonMap.get(apiName.getValue()).put(keyword, lastTimeChecked);
        try {
            jsonPersistentHelper.writeToJsonFile(jsonMapToJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a new keyword with lastTimeChecked of 0L and returns true. Will not overwrite existing keywords and returns false if keyword already exists.
     * @param apiName
     * @param keyword
     * @return
     */
    public boolean addKeywordForApi(APINames apiName, String keyword){
        if(jsonMap.containsKey(apiName.getValue()) && jsonMap.get(apiName.getValue()).containsKey(keyword)){
            return false;
        }
        else{
            this.setKeywordForAPI(apiName, keyword, new Long(0));
            return true;
        }
    }

    /**
     * removes the keyword with the associated Social Media. Returns false if either no entry for the given Social Media or no keyword
     * @param apiName
     * @param keyword
     * @return
     */
    public boolean removeKeywordForAPI(APINames apiName, String keyword){
        if(jsonMap.containsKey(apiName.getValue()) && jsonMap.get(apiName.getValue()).containsKey(keyword)){
            jsonMap.get(apiName.getValue()).remove(keyword);
            try {
                jsonPersistentHelper.writeToJsonFile(jsonMapToJsonString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        else{
            return false;
        }


    }

    /**
     * returns the Map consisting of all keywords and corresponding lastTimeChecked. If there is no Map it returns a new HashMap
     * @param apiName
     * @return
     */
    public Map<String, Long> getKeywordAndLastTimeCheckedMapForAPI(APINames apiName){
        if(jsonMap.containsKey(apiName.getValue())) {
            return jsonMap.get(apiName.getValue());
        }
        else{
            return new HashMap<>();
        }

    }

    /**
     * sets the Long for lastTimeChecked associated with the keyword for the specified Social Media Name
     * @param apiName
     * @param keyword
     * @param lastTimeChecked
     */
    public void setLastTimeCheckedForKeywordForAPI(APINames apiName, String keyword, long lastTimeChecked){
        this.setKeywordForAPI(apiName, keyword, lastTimeChecked);
        try {
            jsonPersistentHelper.writeToJsonFile(jsonMapToJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a Long representing the lastTimeChecked associated with the keyword for the specified Social Media.
     * If the Social Media or the keyword associated with the Social Media does not exist the return is null.
     * @param apiName
     * @param keyword
     * @return
     */
    public Long getLastTimeCheckedForKeywordForAPI(APINames apiName, String keyword){
        if(!jsonMap.containsKey(apiName.getValue()) && jsonMap.get(apiName.getValue()).containsKey(apiName.getValue())){
            return jsonMap.get(apiName.getValue()).get(keyword);
        }
        else{
            return null;
        }
    }
/*
    private String jsonMapToJsonString(){
        Gson gson = new Gson();
        String json = gson.toJson(jsonMap);
        Log.i("JSONPersistentManager jsonMapToJsonString", json);
        return  json;
    }

    private void jsonStringToJsonMap(String jsonString){
        Log.i("JSONPersistentManager jsonStringToJsonMap", jsonString);
        Gson gson = new Gson();
        jsonMap = gson.fromJson(jsonString, Map.class);
    }*/



    private String jsonMapToJsonString(){

        try {
            String json = new String();
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(jsonMap);
            Log.i("JSONPersistentManager jsonMapToJsonString", json);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void jsonStringToJsonMap(String jsonString){
        Log.i("JSONPersistentManager jsonStringToJsonMap", jsonString);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
            jsonMap = objectMapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
