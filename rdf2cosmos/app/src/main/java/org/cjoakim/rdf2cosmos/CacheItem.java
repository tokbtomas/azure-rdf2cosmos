package org.cjoakim.rdf2cosmos;

public class CacheItem {

    private String key;
    private String data;

    public CacheItem() {

        super();
    }

    public CacheItem(String key, String data) {

        this();
        this.key  = key;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
