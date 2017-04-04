package edu.upenn.cis350.clubapp;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubMember implements Comparable{
    //allows for additional capabilities for managing club
    public boolean isAdmin;
    //role in club
    public String title;


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
        return this.title;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setTitle(String title) {
        this.title = title;
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
            this.title.compareTo(other.title);
        }
        return 0;
    }
}
