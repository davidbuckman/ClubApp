package edu.upenn.cis350.clubapp;

/**
 * Created by abhi on 2/18/17.
 */

public class ClubMember implements Comparable{
    public boolean isAdmin;
    public String title;
    public String userId;

    public ClubMember() {
    }

    public ClubMember(String id, boolean isAdmin, String title) {
        this.userId = id;
        this.isAdmin = isAdmin;
        this.title = title;
    }

    public boolean getIsAdmin() {
        return this.isAdmin;
    }

    public String getTitle() {
        return this.isAdmin ? this.title : "General Member";
    }


    public String getUserId() { return this.userId; }


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
