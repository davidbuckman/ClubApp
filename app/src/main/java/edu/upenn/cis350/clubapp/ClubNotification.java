package edu.upenn.cis350.clubapp;

import java.util.Date;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubNotification implements Comparable{
    String title, channel, body;
    long timeStamp;

    public ClubNotification() {

    }





    public ClubNotification(String title, String channel, String body, long timeStamp) {
        this.title = title;
        this.channel = channel;
        this.body = body;
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String author) {
        this.title = title;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int compareTo(Object o) {
        ClubNotification other = (ClubNotification) o;

        if(this.getTimeStamp() > other.getTimeStamp()){
            //comes earlier
            return -1;
        } else if(this.getTimeStamp() < other.getTimeStamp()){
            //comes later
            return 1;
        } else{
            //same timestamp
            return this.getChannel().compareTo(other.getChannel());
        }
    }
}
