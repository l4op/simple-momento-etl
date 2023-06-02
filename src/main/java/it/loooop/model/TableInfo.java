package it.loooop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TableInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("table")
    private String table;

    @JsonProperty("query")
    private String query;

    public TableInfo() {
    }

    public TableInfo(String table, String query) {
        this.table = table;
        this.query = query;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
