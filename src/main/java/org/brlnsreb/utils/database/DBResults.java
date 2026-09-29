package org.brlnsreb.utils.database;

import java.util.*;

public class DBResults {

    public final List<Map<String, Object>> results;
    
    public DBResults(List<Map<String, Object>> results) {
        this.results = results;
    }

    public boolean isEmpty() {
        return results.isEmpty(); 
    }

    public String getString(int index, String key) {
        return (String) results.get(index).get(key);
    }

    public String getString(String key) {
        return (String) results.getFirst().get(key);
    }

    public int getInt(int index, String key) {
        return getNumber(index, key).intValue();
    }

    public int getInt(String key) {
        return getNumber(key).intValue();
    }

    public Number getNumber(int index, String key) {
        return (Number) results.get(index).get(key);
    }

    public Number getNumber(String key) {
        return (Number) results.getFirst().get(key);
    }

    public boolean getBoolean(int index, String key) {
        return (boolean) results.get(index).get(key);
    }

    public boolean getBoolean(String key) {
        return (boolean) results.getFirst().get(key);
    }

}