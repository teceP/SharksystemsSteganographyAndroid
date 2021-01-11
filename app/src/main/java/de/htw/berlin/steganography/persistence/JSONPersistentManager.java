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

import de.htw.berlin.steganography.apis.models.APINames;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JSONPersistentManager {
    private Map<String, Map<String, List<String>>> jsonMap = new ConcurrentHashMap<>();

    private JSONPersistentHelper jsonPersistentHelper;

    private static final JSONPersistentManager instance = new JSONPersistentManager();

    private JSONPersistentManager() {
    }

    public static JSONPersistentManager getInstance() {
        return instance;
    }

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

    public Map<String, Map<String, List<String>>> getJsonMap()throws Exception{
        try {
            return jsonMap;
        } catch (NullPointerException ne){
            ne.printStackTrace();
            throw new Exception("jsonMap is null",ne);
        }
    }

    public void addKeywordForAPI(APINames apiName, String keyword){
        if(!jsonMap.containsKey(apiName.getValue())){
            jsonMap.put(apiName.getValue(),new HashMap<String, List<String>>());
        }
        if(!jsonMap.get(apiName.getValue()).containsKey("keywords")){
            jsonMap.get(apiName.getValue()).put("keywords", new ArrayList<String>());
        }
        if(!jsonMap.get(apiName.getValue()).get("keywords").contains(keyword)) {
            jsonMap.get(apiName.getValue()).get("keywords").add(keyword);
        }
        try {
            jsonPersistentHelper.writeToJsonFile(jsonMapToJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeKeywordForAPI(APINames apiName, String keyword){
        if(!jsonMap.containsKey(apiName.getValue())){
            jsonMap.put(apiName.getValue(),new HashMap<String, List<String>>());
        }
        if(!jsonMap.get(apiName.getValue()).containsKey("keywords")){
            jsonMap.get(apiName.getValue()).put("keywords", new ArrayList<String>());
        }

        if(jsonMap.get(apiName.getValue()).get("keywords").contains(keyword)) {
            jsonMap.get(apiName.getValue()).get("keywords").remove(keyword);
        }

        try {
            jsonPersistentHelper.writeToJsonFile(jsonMapToJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getKeywordListForAPI(APINames apiName) throws Exception{
        try {
            Map<String, List<String>> specificAPIMap = jsonMap.get(apiName.getValue());
            return specificAPIMap.get("keywords");
        } catch (NullPointerException ne){
            ne.printStackTrace();
            throw new Exception("No entry for API: "+apiName.getValue(),ne);
        }
    }

    public void setLastTimeCheckedForAPI(APINames apiName, long lastTimeCheckedSystemTimeMillis){
        if(!jsonMap.containsKey(apiName.getValue())){
            jsonMap.put(apiName.getValue(),new HashMap<String, List<String>>());
        }
        if(!jsonMap.get(apiName.getValue()).containsKey("last-checked")){
            jsonMap.get(apiName.getValue()).put("last-checked", Arrays.asList(new String[1]));
        }

        jsonMap.get(apiName.getValue()).get("last-checked").set(0, Long.toString(lastTimeCheckedSystemTimeMillis));

        try {
            jsonPersistentHelper.writeToJsonFile(jsonMapToJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastTimeCheckedForAPI(APINames apiName) throws Exception{
        try {
            return  jsonMap.get(apiName.getValue()).get("last-checked").get(0);
        } catch (NullPointerException ne){
            ne.printStackTrace();
            throw new Exception("No entry for API: "+apiName.getValue(),ne);
        }
    }

    private String jsonMapToJsonString(){
        Gson gson = new Gson();
        String json = gson.toJson(jsonMap);
        return  json;
    }

    private void jsonStringToJsonMap(String jsonString){
        Gson gson = new Gson();
        jsonMap = gson.fromJson(jsonString, Map.class);
    }
}
