package edu.upenn.cis350.clubapp;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubEvent {

    String author, description, name;
    String eventId;
    Long date;

    public ClubEvent() {

    }

    public ClubEvent(String author, String description, String name, Long date, String eventId) {
        this.author = author;
        this.description = description;
        this.name = name;
        this.date = date;
        this.eventId = eventId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
