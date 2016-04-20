package com.example.ryan.whosaround;

import android.os.Parcel;
import android.os.Parcelable;

import com.cloudmine.api.db.LocallySavableCMObject;
import com.cloudmine.api.CMGeoPoint;

public class Person extends LocallySavableCMObject implements Parcelable{
    public static final String CLASS_NAME = "Person";
    private String firstName;
    private String lastName;
    private String miles;
    private String lastSeen;
    private CMGeoPoint location; //Geolocation Tagging and Searching
    static final long serialVersionUID = 465489764; //Added serialVersionUID variable


    //there must be a no-args constructor for deserializing to work
    Person(){ super(); }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.miles = "0.0";
        this.location = null;
    }

    protected Person(Parcel in) {
   /*
     * Reconstruct from the Parcel
     */
        System.out.println("ParcelData(Parcel source): time to put back parcel data");
        firstName = in.readString();
        lastName = in.readString();
        miles = in.readString();
        lastSeen = in.readString();
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    //Your getter and setters determine what gets serialized and what doesn't
    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}

    public CMGeoPoint getLocation() {
        return location;
    }

    public void setLocation(CMGeoPoint location) {
        this.location = location;
    }

    public String getMiles() {
        return miles;
    }

    public void setMiles(String miles) {
        this.miles = miles;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        System.out.println("writeToParcel..." + flags);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(miles);
        dest.writeString(lastSeen);
    }


}

