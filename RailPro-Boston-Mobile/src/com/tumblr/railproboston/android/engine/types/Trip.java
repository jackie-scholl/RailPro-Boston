package com.tumblr.railproboston.android.engine.types;

import java.util.Date;
import java.util.List;

import com.tumblr.railproboston.android.engine.ScheduleEngine;
import android.content.Context;

public class Trip implements Comparable<Trip> {
	private static final String CLASSNAME = new Object() {}.getClass().getEnclosingClass().getSimpleName();

	public String routeId;
	public String serviceId;
	public String tripId;
	public String headsign;
	public String direction;
	public String blockId;
	public String shapeId;
	private List<StopTime> stopTimes;

	public Trip(String route_id, String service_id, String trip_id, String trip_headsign, String direction_id,
			String block_id, String shape_id) {
		this.routeId = ScheduleEngine.clean(route_id);
		this.serviceId = ScheduleEngine.clean(service_id);
		this.tripId = ScheduleEngine.clean(trip_id);
		this.headsign = ScheduleEngine.clean(trip_headsign);
		this.direction = ScheduleEngine.clean(direction_id);
		this.blockId = ScheduleEngine.clean(block_id);
		this.shapeId = ScheduleEngine.clean(shape_id);
		this.stopTimes = null;
	}

	public Trip(String[] arr) {
		this(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5], arr[6]);
	}

	public List<StopTime> getStopTimes(Context ctx) {
		if (stopTimes == null)
			stopTimes = ScheduleEngine.getStopTimes(ctx, this);
		return stopTimes;
	}

	public StopTime getFirstStopTime(Context ctx) {
		return getStopTimes(ctx).get(0);
	}

	public StopTime getLastStopTime(Context ctx) {
		getStopTimes(ctx);
		return stopTimes.get(stopTimes.size() - 1);
	}

	public Date getStartTime(Context ctx) {
		return getFirstStopTime(ctx).getTime();
	}

	public Date getEndTime(Context ctx) {
		return getLastStopTime(ctx).getTime();
	}

	public String getRouteId() {
		return routeId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getTripId() {
		return tripId;
	}

	public String getHeadsign() {
		return headsign;
	}

	public String toStringFull() {
		return String.format("%s, %s, %s, %s, %s, %s, %s", routeId, serviceId, tripId, headsign, direction, blockId,
				shapeId);
	}

	public String toString() {
		//Context ctx = MainActivity.getInstance();
		Context ctx = null;
		return String.format("%s %tl:%<tM %<Tp - %tl:%<tM %<Tp", headsign.split(" \\(")[0], getStartTime(ctx), getEndTime(ctx));
	}

	public int compareTo(Trip other) {
		if (stopTimes == null)
			throw new RuntimeException();
		return stopTimes.get(0).compareTo(other.stopTimes.get(0));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((headsign == null) ? 0 : headsign.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
		result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
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
		Trip other = (Trip) obj;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (headsign == null) {
			if (other.headsign != null)
				return false;
		} else if (!headsign.equals(other.headsign))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		if (shapeId == null) {
			if (other.shapeId != null)
				return false;
		} else if (!shapeId.equals(other.shapeId))
			return false;
		if (tripId == null) {
			if (other.tripId != null)
				return false;
		} else if (!tripId.equals(other.tripId))
			return false;
		return true;
	}
}