package com.kodexlabs.eventiles;

/**
 * Created by 1505560 on 20-May-17.
 */

public class Event {

    private String id;
    private String title;
    private String date;
    private String poster;
    private String type;
    private String status;
    private String state;
    private String city;

    public Event() {}

    public Event(String id, String title, String institute, String date, String poster, String type, String status, String state, String city) {
        id = id;
        title = title;
        date = date;
        poster = poster;
        type = type;
        status = status;
        state = state;
        city = city;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getPoster(){
        return poster;
    }

    public String getType(){
        return type;
    }

    public String getStatus() { return  status; }

    public String getState() { return state; }

    public String getCity() { return city; }
}
