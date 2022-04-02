package code;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	Node highlightedNode;
	Node fromNode;
	Node toNode;
	Collection<Road> highlightedRoads = new HashSet<>();
	ArrayList<Segment> highlightedSegments = new ArrayList<>();
	ArrayList<Node> highlightedNodes = new ArrayList<>();
	ArrayList<Node> highlightedAPs = new ArrayList<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(Mapper.SEGMENT_COLOUR);
		for (Segment s : segments)
			s.draw(g2, origin, scale);
		
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Segment segment : highlightedSegments) {
				segment.draw(g2, origin, scale);
			}
		
	

		// draw the segments of all highlighted roads.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}

		// draw all the nodes.

		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);
		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}
		if (fromNode != null) {
			g2.setColor(Color.red);
			fromNode.draw(g2, screen, origin, scale);
		}
		if (toNode != null) {
			g2.setColor(Color.green);
			toNode.draw(g2, screen, origin, scale);
		}
		
		if(highlightedNodes!=null) {
			for(Node node:highlightedNodes) {
				node.draw(g2, screen, origin, scale);
			}
		}
		
		if(highlightedAPs!=null) {
			g2.setColor(Color.MAGENTA);
			for(Node node:highlightedAPs) {
				node.draw(g2, screen, origin, scale);
			}
		}
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}
	public void setHighlightFromNode(Node node) {
		this.fromNode = node;
	}
	public void setHighlightToNode(Node node) {
			this.toNode = node;	
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}
	
	public void setHightlightSegment(Segment segment) {
		this.highlightedSegments.add(segment);
	}
	
	public void setHighlightAP(Node node) {
		this.highlightedAPs.add(node);
	}
}

// code for COMP261 assignments