package edu.upenn.cis350.clubapp;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by abhi on 2/18/17.
 */

@IgnoreExtraProperties
public class User implements Comparable{
    public String firstName;
    public String lastName;
    public String email;
    public Map<String, Boolean> clubs = new HashMap<>();

    public User() {
    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public void addClub(String clubID) {
        clubs.put(clubID, true);
    }

    public void removeClub(String clubID) {
        clubs.remove(clubID);
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    @Override
    public int compareTo(Object o) {
        User other = (User) o;

        if(this.getLastName().equals(other.getLastName())){
            //same last names, look to first names
            if(this.getFirstName().equals(other.getFirstName())){
                //same first name, look to emails
                if(this.getEmail().equals(other.getEmail())){
                    //same email. Assumes same user
                    return 0;
                } else{
                    return this.getEmail().compareTo(other.getEmail());
                }
            } else {
                return this.getFirstName().compareTo(other.getFirstName());
            }
        } else{
            return this.getLastName().compareTo(other.getLastName());
        }
    }

}
