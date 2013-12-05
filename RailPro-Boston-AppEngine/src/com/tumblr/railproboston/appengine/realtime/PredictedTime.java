package com.tumblr.railproboston.appengine.realtime;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class PredictedTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	private String tripId;
	private String stopId;
	private Date scheduledTime;
	private Integer predictedLateness;

	public Key getKey() {
		return key;
	}

	public String getTripId() {
		return tripId;
	}

	public PredictedTime setTripId(String tripId) {
		this.tripId = tripId;
		return this;
	}

	public String getStopId() {
		return stopId;
	}

	public PredictedTime setStopId(String stopId) {
		this.stopId = stopId;
		return this;
	}

	public Date getScheduledTime() {
		return scheduledTime;
	}

	public PredictedTime setScheduledTime(Date scheduledTime) {
		this.scheduledTime = scheduledTime;
		return this;
	}

	public Integer getPredictedLateness() {
		return predictedLateness;
	}

	public PredictedTime setPredictedLateness(Integer predictedLateness) {
		this.predictedLateness = predictedLateness;
		return this;
	}

}
