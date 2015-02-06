package com.wsuproj5.accuratedrivingtest.addroute;

public class RouteState {
	public String origin;
	public String terminus;
	
	public RouteState() {
		origin = null;
		terminus = null;
	}
	
	public RouteState(String origin, String terminus) {
		this.origin = origin;
		this.terminus = terminus;
	}
}
