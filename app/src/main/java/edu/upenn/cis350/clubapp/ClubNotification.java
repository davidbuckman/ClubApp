package edu.upenn.cis350.clubapp;

import java.util.Date;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubNotification {
    String author, channel, content;
    long timeStamp;

    public ClubNotification() {

    }


    public ClubNotification(String author, String channel, String content, long timeStamp) {
        this.author = author;
        this.channel = channel;
        this.content = content;
        this.timeStamp = timeStamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
