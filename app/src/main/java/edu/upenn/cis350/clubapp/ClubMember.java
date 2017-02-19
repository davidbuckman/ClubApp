package edu.upenn.cis350.clubapp;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubMember {
    public boolean isAdmin;
    public String title;
    public int unreadNotifs;

    public ClubMember() {
    }

    public ClubMember(boolean isAdmin, String title) {
        this.isAdmin = isAdmin;
        this.title = title;
    }

    public boolean getIsAdmin() {
        return this.isAdmin;
    }

    public String getTitle() {
        return this.isAdmin ? this.title : "General Member";
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int addUnreadNotification() {
        this.unreadNotifs++;
        return this.unreadNotifs;
    }

    public void resetUnreadNotifcations() {
        this.unreadNotifs = 0;
    }
}
