package com.tumblr.railproboston.android.engine.types;

public class Journey {
	private final Route route;
	private final Trip trip;
	private final StopTime boardStop;
	private final StopTime departStop;

	public Journey(Route route, Trip trip, StopTime boardStop, StopTime departStop) {
		this.route = route;
		this.trip = trip;
		this.boardStop = boardStop;
		this.departStop = departStop;
	}

}
