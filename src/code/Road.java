package code;

import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name, city;
	public final Collection<Segment> components;
	public final int oneWay;
	private int speedLimit;
	public final int roadclass;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.components = new HashSet<Segment>();
		this.oneWay=oneway;
		if(speed==0) {speedLimit =5; }
		else if(speed==1) {speedLimit = 20;}
		else if(speed==2) {speedLimit = 40;}
		else if(speed==3) {speedLimit = 60;}
		else if(speed==4) {speedLimit = 80;}
		else if(speed==5) {speedLimit = 100;}
		else if(speed==6) {speedLimit = 110;}
		else if(speed==7) {speedLimit = 200;} // no limit i assume to be 200
		this.roadclass=roadclass;
	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}
	
	public int getSpeedLimit() {return speedLimit;}
}

// code for COMP261 assignments