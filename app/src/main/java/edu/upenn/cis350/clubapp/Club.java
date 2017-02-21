package edu.upenn.cis350.clubapp;

import java.lang.reflect.Member;
import java.util.HashMap;

/**
 * Created by abhi on 2/18/17.
 */

public class Club {
    String name, about;
    HashMap<String, Boolean> channels = new HashMap<String, Boolean>();
    HashMap<String, Boolean> notifications = new HashMap<String, Boolean>();
    HashMap<String, Boolean> events = new HashMap<String, Boolean>();
    HashMap<String, ClubMember> members = new HashMap<String, ClubMember>();
    public Club() {
    }

    public Club(String name, String aboutText, String creatorUid) {
        this.name = name;
        this.about = aboutText;
        this.members.put(creatorUid, new ClubMember(false, "Creator"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAboutText() {
        return about;
    }

    public void setAboutText(String aboutText) {
        this.about = aboutText;
    }

    public HashMap<String, Boolean> getChannels() {
        return channels;
    }

    public void setChannels(HashMap<String, Boolean> channels) {
        this.channels = channels;
    }

    public void addChannel(String s) {
        this.channels.put(s, true);
    }

    public void removeChannel(String s) {
        this.channels.remove(s);
    }

    // events
    public HashMap<String, Boolean> getEvents() {
        return events;
    }

    public void setEvents(HashMap<String, Boolean> events) {
        this.events = events;
    }

    public void addEvent(String e) {
        this.events.put(e, true);
    }

    public void removeEvent(String e) {
        this.events.remove(e);
    }

    // members
    public HashMap<String, ClubMember> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, ClubMember> members) {
        this.members = members;
    }

    public void addMember(String s, ClubMember clubMember) {
        this.members.put(s, clubMember);
    }

    public void removeMember(String s) {
        this.members.remove(s);
    }

    //notifications

    public HashMap<String, Boolean> getNotifications() {
        return notifications;
    }

    public void setNotifications(HashMap<String, Boolean> notifications) {
        this.notifications = notifications;
    }

    public void addNotification(String s) {
        this.notifications.put(s, true);
    }

    public void removeNotifications(String s) {
        this.notifications.remove(s);
    }
}
