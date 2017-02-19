package edu.upenn.cis350.clubapp;

import java.util.HashMap;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubChannel {
    String name, purpose;
    HashMap<String, Boolean> subscribers;
    public ClubChannel() {

    }

    public ClubChannel(String name, String purpose) {
        this.name = name;
        this.purpose = purpose;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public HashMap<String, Boolean> getSubscribers() {
        return this.subscribers;
    }

    public void addSubscriber(String uid) {
        this.subscribers.put(uid, true);
    }

    public void removeSubscriber(String uid) {
        this.subscribers.remove(uid);
    }
}
