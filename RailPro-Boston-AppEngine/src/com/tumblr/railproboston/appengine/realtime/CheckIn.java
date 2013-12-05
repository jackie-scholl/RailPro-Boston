package com.tumblr.railproboston.appengine.realtime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
    
    private String route;
    private String tripId;
    private String station;
    
    public CheckIn(String route, String tripId, String station) {
        this.route = route;
        this.tripId = tripId;
        this.station = station;
    }
    
    public CheckIn() {
        this(null, null, null);
    }
    
    public String getRoute() {
        return route;
    }
    
    public String getTripId() {
        return tripId;
    }
    
    public String getStation() {
        return station;
    }
    
    public String getTripNumber() {
        return tripId.split("-")[4];
    }
    
}
