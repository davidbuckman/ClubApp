package edu.upenn.cis350.clubapp;

/**
 * Created by jennabarton on 4/3/17.
 */

public class ClubInvitation {
    private boolean isAdmin;
    private String title;

    ClubInvitation(){}

    ClubInvitation(boolean admin, String t){
        this.isAdmin = admin;
        this.title = t;
    }


    public boolean getIsAdmin(){
        return this.isAdmin;
    }

    public String getTitle(){
        return this.title;
    }

}
