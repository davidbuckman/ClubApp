package edu.upenn.cis350.clubapp;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubEvent {

    String author, description, name;
    Date date;

    public ClubEvent() {

    }

    public ClubEvent(String author, String description, String name, Date date) {
        this.author = author;
        this.description = description;
        this.name = name;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
