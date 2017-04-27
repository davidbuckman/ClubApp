package edu.upenn.cis350.clubapp;

/**
 * Created by David on 4/27/2017.
 */

public class UserChannel {
    String name;
    boolean active;

    UserChannel(String name){
        this.name = name;
        active = false;
    }

    UserChannel(String name, boolean active){
        this.name = name;
        this.active = active;
    }
    public String getName(){
        return this.name;
    }
    public boolean getActive(){
        return this.active;
    }
    public void setActive(boolean active){
        this.active = active;
    }

}
