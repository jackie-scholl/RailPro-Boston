package com.tumblr.railproboston.android.engine.types;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.tumblr.railproboston.android.engine.ScheduleEngine;

public class StopTime implements Comparable<StopTime> {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();

	public String tripId;
	public String time;
	public String stopId;
	public int stopSequence;
	private String predictedLateness = null;

	public StopTime(String trip_id, String arrival_time, String stop_id, String stop_sequence) {
		this.tripId = ScheduleEngine.clean(trip_id);
		this.time = ScheduleEngine.clean(arrival_time);
		this.stopId = ScheduleEngine.clean(stop_id);
		this.stopSequence = Integer.parseInt(stop_sequence);
	}

	public StopTime(String trip_id, String arrival_time, String departure_time, String stop_id,
			String stop_sequence,
			String stop_headsign, String pickup_type, String drop_off_type) {
		this(trip_id, arrival_time, stop_id, stop_sequence);
	}

	public StopTime(String[] arr) {
		this(arr[0], arr[1], arr[3], arr[4]);
	}

	public String getStopId() {
		return stopId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getRouteId() {
		return tripId.replaceAll("(CR-\\w+)-CR-(\\w+)-(\\d+)", "$1");
	}

	public Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		String[] timeParts = time.split(":");
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
		c.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
		c.set(Calendar.SECOND, Integer.parseInt(timeParts[2]));
		return c;
	}

	public Date getTime() {
		return getCalendar().getTime();
	}

	public String getFormattedTime() {
		return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(getTime());
	}

	public void setPredictedLateness(String str) {
		predictedLateness = str;
	}

	public String toStringFull() {
		return String.format(Locale.US, "%s, %s, %s, %d", tripId, time, stopId, stopSequence);
	}

	/*@Override
	public String toString() {
		return String.format("StopTime [tripId=%s, time=%s, stopId=%s, stopSequence=%s, predictedLateness=%s]", tripId,
				time, stopId, stopSequence, predictedLateness);
	}*/

	@Override
	public String toString() {
		return String.format("%s - %s (%s)", stopId, getFormattedTime(), predictedLateness);
	}

	public int compareTo(StopTime other) {
		return this.getCalendar().compareTo(other.getCalendar());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + stopSequence;
		result = prime * result + ((tripId == null) ? 0 : tripId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StopTime other = (StopTime) obj;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopSequence != other.stopSequence)
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}
}