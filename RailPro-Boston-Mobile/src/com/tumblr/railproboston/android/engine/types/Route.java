package com.tumblr.railproboston.android.engine.types;

import java.util.List;

import android.content.Context;

import com.tumblr.railproboston.android.engine.ScheduleEngine;

public class Route {
	public String routeId;
	public String shortName;
	public String longName;
	
	public Route(String route_id, String route_short_name, String route_long_name) {
		this.routeId = ScheduleEngine.clean(route_id);
		this.shortName = ScheduleEngine.clean(route_short_name);
		this.longName = ScheduleEngine.clean(route_long_name);
	}

	/*public Route(String route_id, String agency_id, String route_short_name, String route_long_name, String route_desc,
			String route_type, String route_url, String route_color, String route_text_color) {
		this(route_id, route_short_name, route_long_name);
	}*/

	public Route(String[] arr) {
		this(arr[0], arr[2], arr[3]);
	}
	
	public String getRouteId() {
		return routeId;
	}
	
	public String getLongName() {
		return longName;
	}
	
	public List<Trip> getTrips(Context ctx) {
		return ScheduleEngine.getTrips(ctx, this);
	}

	public String toString() {
		return longName;
	}
}