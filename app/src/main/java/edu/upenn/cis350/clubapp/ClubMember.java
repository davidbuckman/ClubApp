package edu.upenn.cis350.clubapp;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubMember implements Comparable{
    public boolean isAdmin;
    public String title;
    public String name;
    public int unreadNotifs;

    public ClubMember() {
    }

    public ClubMember(String name, boolean isAdmin, String title) {
        this.name = name;
        this.isAdmin = isAdmin;
        this.title = title;
    }

    public boolean getIsAdmin() {
        return this.isAdmin;
    }

    public String getTitle() {
        return this.isAdmin ? this.title : "General Member";
    }

    public String getName() {  return this.name; }

    public void setName(String nm) { this.name = nm; }

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

    @Override
    public int compareTo(Object o) {
        ClubMember other = (ClubMember) o;


        if(this.getIsAdmin() == true && other.getIsAdmin() == false){
            return -1;
        } else if(this.getIsAdmin() == false && other.getIsAdmin() == true){
            return 1;
        } else{
            //both admin or both general users
            this.getName().compareTo(other.getName());
        }



        return 0;
    }
}
