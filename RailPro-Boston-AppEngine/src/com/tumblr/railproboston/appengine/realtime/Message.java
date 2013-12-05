package com.tumblr.railproboston.appengine.realtime;

import java.io.Serializable;
import java.util.Date;

class Message implements Comparable<Message>, Serializable {
	private static final long serialVersionUID = 1669267860503776753L;

	private final String route;
	private final String timeStamp;
	private final String trip;
	private final String destination;
	private final String station;
	private final String scheduled;
	private final String flag;
	private final String vehicle;
	private final String latitude;
	private final String longitude;
	private final String heading;
	private final String speed;
	private final String lateness;
	private final Date dateFetched;

	public Message(String route, String timeStamp, String trip, String destination, String stop, String scheduled,
			String flag, String vehicle, String latitude, String longitude, String heading, String speed,
			String lateness, Date dateFetched) {
		this.route = route;
		this.timeStamp = timeStamp;
		this.trip = trip;
		this.destination = destination;
		this.station = stop;
		this.scheduled = scheduled;
		this.flag = flag;
		this.vehicle = vehicle;
		this.latitude = latitude;
		this.longitude = longitude;
		this.heading = heading;
		this.speed = speed;
		this.lateness = lateness;
		this.dateFetched = dateFetched;
	}

	public Message(String route, String timeStamp, String trip, String destination, String stop, String scheduled,
			String flag, String vehicle, String latitude, String longitude, String heading, String speed,
			String lateness) {
		this(route, timeStamp, trip, destination, stop, scheduled, flag, vehicle, latitude, longitude, heading, speed,
				lateness, new Date());
	}

	public String getRoute() {
		return route;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public String getTrip() {
		return trip;
	}

	public String getDestination() {
		return destination;
	}

	public String getStation() {
		return station;
	}

	public String getScheduled() {
		return scheduled;
	}

	public String getFlag() {
		return flag;
	}

	public String getVehicle() {
		return vehicle;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getHeading() {
		return heading;
	}

	public String getSpeed() {
		return speed;
	}

	public boolean hasLateness() {
		return lateness != null && lateness.length() != 0;
	}

	public String getLateness() {
		return lateness;
	}

	public Date getDateFetched() {
		return dateFetched;
	}

	public int compareTo(Message m) {
		return dateFetched.compareTo(m.dateFetched);
	}

	@Override
	public String toString() {
		return "Message [route=" + route + ", timeStamp=" + timeStamp + ", trip=" + trip + ", destination="
				+ destination + ", stop=" + station + ", scheduled=" + scheduled + ", flag=" + flag + ", vehicle="
				+ vehicle + ", latitude=" + latitude + ", longitude=" + longitude + ", heading=" + heading + ", speed="
				+ speed + ", lateness=" + lateness + ", dateFetched=" + dateFetched + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateFetched == null) ? 0 : dateFetched.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((flag == null) ? 0 : flag.hashCode());
		result = prime * result + ((heading == null) ? 0 : heading.hashCode());
		result = prime * result + ((lateness == null) ? 0 : lateness.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		result = prime * result + ((scheduled == null) ? 0 : scheduled.hashCode());
		result = prime * result + ((speed == null) ? 0 : speed.hashCode());
		result = prime * result + ((station == null) ? 0 : station.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		result = prime * result + ((trip == null) ? 0 : trip.hashCode());
		result = prime * result + ((vehicle == null) ? 0 : vehicle.hashCode());
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
		Message other = (Message) obj;
		if (dateFetched == null) {
			if (other.dateFetched != null)
				return false;
		} else if (!dateFetched.equals(other.dateFetched))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (flag == null) {
			if (other.flag != null)
				return false;
		} else if (!flag.equals(other.flag))
			return false;
		if (heading == null) {
			if (other.heading != null)
				return false;
		} else if (!heading.equals(other.heading))
			return false;
		if (lateness == null) {
			if (other.lateness != null)
				return false;
		} else if (!lateness.equals(other.lateness))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		if (scheduled == null) {
			if (other.scheduled != null)
				return false;
		} else if (!scheduled.equals(other.scheduled))
			return false;
		if (speed == null) {
			if (other.speed != null)
				return false;
		} else if (!speed.equals(other.speed))
			return false;
		if (station == null) {
			if (other.station != null)
				return false;
		} else if (!station.equals(other.station))
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		if (trip == null) {
			if (other.trip != null)
				return false;
		} else if (!trip.equals(other.trip))
			return false;
		if (vehicle == null) {
			if (other.vehicle != null)
				return false;
		} else if (!vehicle.equals(other.vehicle))
			return false;
		return true;
	}
}