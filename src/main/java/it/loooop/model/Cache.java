package it.loooop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Cache implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name;

    @JsonProperty("rows")
    private Integer rows;

    @JsonProperty("key")
    private String key;

    public Cache() {
    }

    public Cache(String name, Integer rows, String key) {
        this.name = name;
        this.rows = rows;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
