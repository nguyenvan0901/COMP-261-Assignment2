package code;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author tony
 */
public class Node {
	
	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;
	private Node previous = null;
	private int depth = -1;
	private int reachBack=0;
	private Node parent = null;
	private int numSubTree =0;
	private ArrayList<Node> neighbour = new ArrayList<>();
	private HashSet<Node> incomingList = new HashSet<>();
	private HashSet<Node> outGoingList = new HashSet<>();
	private HashSet<Segment> outGoingSegments = new HashSet<>();
	private HashSet<Segment> neighbourSegments = new HashSet<>();
	
	
	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<Segment>();
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}
	public void setPrev(Node node) {previous = node;}
	public Node getPre() {return previous;}
	public void setDepth(int x) {depth=x;}
	public int getDepth() {return depth;}
	public void setReachBack(int x) {reachBack =x;}
	public int getReachBack() {return reachBack;}
	public ArrayList<Node> getNeighbour() {return neighbour;}
	public void addToNeighbour(Node node) {neighbour.add(node);}
	public void removeNeighbour(Node node) {neighbour.remove(node);}
	public int getNOSubtree() {return numSubTree;}
	public void setNOSubtree() {numSubTree++;}
	public Node getParent() {return parent;}
	public HashSet<Node> getIncoming(){return incomingList;}
	public HashSet<Node> getOutgoing(){return outGoingList;}
	public void addToInc(Node node) {incomingList.add(node);}
	public void addToOutg(Node node) {outGoingList.add(node);}
	public void addToOutSegments(Segment segment) {outGoingSegments.add(segment);}
	public HashSet<Segment> getOutgoingSegment(){return outGoingSegments;}
	public void addToNeighbourtSegments(Segment segment) {neighbourSegments.add(segment);}
	public HashSet<Segment> getNeighbourSegment(){return neighbourSegments;}
	
	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;
		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}
	
	public int getID() {return nodeID;}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
}

// code for COMP261 assignments