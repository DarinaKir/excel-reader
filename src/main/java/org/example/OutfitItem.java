package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class OutfitItem {
    private int id;
    private String type;
    private String style;
    private String color;
    private List<String> season;
    private String description;

    public OutfitItem(int id) {
        this.id = id;
    }

    public OutfitItem(int id, String type, String style, String color, List<String> season, String description) {
        this.id = id;
        this.type = type;
        this.style = style;
        this.color = color;
        this.season = season;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getSeason() {
        return season;
    }

    public void setSeason(List<String> season) {
        this.season = season;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OutfitItem{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", style='" + style + '\'' +
                ", color='" + color + '\'' +
                ", season=" + season +
                ", description='" + description + '\'' +
                '}';
    }
}
