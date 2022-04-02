package code;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;
	
	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;
	
	private Node fromNode = null;
	private Node toNode = null;
	private Queue<AStarElem> fringe = new PriorityQueue<>();	
	private ArrayList<ArrayList<Node>> nodeGroups = new ArrayList<ArrayList<Node>>();
	public boolean distance=true;
	public boolean time =false;
	
	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}
		
		if(fromNode == null) {
			fromNode = closest;
			graph.setHighlightFromNode(closest);}
		else {
			toNode = closest;
			graph.setHighlightToNode(closest);}
		
		if(!graph.highlightedSegments.isEmpty()) {
			graph.toNode=null;
			graph.fromNode=null;
			graph.highlightedSegments.clear();
			fromNode = null;
			toNode = null;
		}
	}

	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-9, 2); // center the small graph
		scale = 66;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		Mapper m = new Mapper();
		String path = "/Users/vanbanguyen0901/Desktop/broken eclipse-workspace/Assign2/src/data/small/";
		
		m.onLoad(new File(path + NODES_FILENAME), new File(path + ROADS_FILENAME), new File(path + SEGS_FILENAME), new File(path + POLYS_FILENAME));
		m.redraw();
	}	

	@Override
	protected void onAStar() {
		String text="";
		fringe.clear();
		double dis = 0;
		ArrayList<Node> visitedNode = new ArrayList<>();
		if(fromNode == null || toNode == null) {
			System.err.println("one of the two node is null");
		}
		else {
			graph.setHighlightFromNode(fromNode);
			graph.setHighlightToNode(toNode);
			findAllNeighbourForAllNodes();
			AStarElem firstElem = new AStarElem(fromNode, null, 0 , euclideanDistance(fromNode,toNode));
			fringe.add(firstElem);
		while(!fringe.isEmpty()) {
			AStarElem aStarElement = fringe.poll();
			Node elemNode = aStarElement.getCurrentNode();		
			if(!visitedNode.contains(elemNode)) { // not visited yet
				visitedNode.add(elemNode);	
				elemNode.setPrev(aStarElement.getPreviousNode());
				if(elemNode.equals(toNode)) {
					dis = aStarElement.getG();
					break;
				}
				else {
					for(Segment segment: elemNode.getNeighbourSegment()) {
						Node neighbour=null;
						if(segment.start==elemNode) {neighbour = segment.end;}
						else if(segment.end==elemNode) {neighbour = segment.start;}
						if(!visitedNode.contains(neighbour)) {
							double g = aStarElement.getG()+segment.length;
							double f = g +  euclideanDistance(neighbour,toNode);
							AStarElem newElem = new AStarElem(neighbour,elemNode,g,f);
							fringe.add(newElem);
						}
					}			
				}	
			}
		}
		ArrayList<Node> shortestPath = new ArrayList<>();		
		shortestPath = this.getShortestPath(toNode,shortestPath);
		for(int i=0; i<shortestPath.size()-1;i++) {
			Node from = shortestPath.get(i);
			Node to = shortestPath.get(i+1);
			ArrayList<Segment> segments = new ArrayList<>(from.segments);
			for(Segment segment: segments) {
				
				if((segment.start==from && segment.end==to) || (segment.start==to && segment.end == from)){
					String roadName = graph.roads.get(segment.roadID).name;
					double segmentLength = segment.length;
					text =text + roadName+" with length of "+segmentLength + "\n";
					graph.highlightedSegments.add(segment);
				}
			}
		}
		fromNode = null;
		toNode = null;
	}
		text=text+("\n total distance is "+dis);
		getTextOutputArea().setText(text);
}
	
	protected void onAStarComp() {
		graph.highlightedSegments.clear();
		String text ="";
		double dis = 0;
		fringe.clear();
		ArrayList<Node> visited = new ArrayList<>();
		if(fromNode == null || toNode == null) {
			System.err.println("one of the two node is null");
		}
		else {
			
			graph.setHighlightFromNode(fromNode);
			graph.setHighlightToNode(toNode);
			findAllNeighbourForAllNodes();	
			
			// if default is finding the shortest path in distance
			if(distance==true) {
				AStarElem firstElem = new AStarElem(fromNode, null, 0 , euclideanDistance(fromNode,toNode));
				fringe.add(firstElem);
				while(!fringe.isEmpty()) {
					AStarElem aStarElement = fringe.poll();
					Node elemNode = aStarElement.getCurrentNode();		
					if(!visited.contains(elemNode)) { // not visited yet
						visited.add(elemNode);	
						elemNode.setPrev(aStarElement.getPreviousNode());
						if(elemNode.equals(toNode)) {
							dis = aStarElement.getG();
							break;
						}											
						for(Segment segment: elemNode.getOutgoingSegment()) {							
							Node neighbour=null;
							if(segment.start==elemNode) {neighbour = segment.end;}
							else if(segment.end==elemNode) {neighbour = segment.start;}
							if(!visited.contains(neighbour)) {
								double g = aStarElement.getG()+segment.length;
								double f = g +  euclideanDistance(neighbour,toNode);
								AStarElem newElem = new AStarElem(neighbour,elemNode,g,f);
								fringe.add(newElem);
							}
						}
					}
				}		
			}
			
			// if default is finding the shortest path in time
			else if(time == true) {
				// APS will be node,prev,time taken,estimated time
				AStarElem firstElem = new AStarElem(fromNode, null, 0 , estimatedTime(fromNode,toNode));
				fringe.add(firstElem);
				while(!fringe.isEmpty()) {
					AStarElem aStarElement = fringe.poll();
					Node elemNode = aStarElement.getCurrentNode();		
					if(!visited.contains(elemNode)) { // not visited yet
						visited.add(elemNode);	
						elemNode.setPrev(aStarElement.getPreviousNode());
						if(elemNode.equals(toNode)) {
							dis = aStarElement.getG();
							break;
						}
						
						for(Segment segment: elemNode.getOutgoingSegment()) {
							Node neighbour=null;
							if(segment.start==elemNode) {neighbour = segment.end;}
							else if(segment.end==elemNode) {neighbour = segment.start;}
							if(!visited.contains(neighbour)) {
								double g = aStarElement.getG()+timeNeedToGoThroughSegment(segment);
								double f = g + estimatedTime(neighbour,toNode);
								AStarElem newElem = new AStarElem(neighbour,elemNode,g,f);
								fringe.add(newElem);
							}
						}
					}
				}
			}
		}
		ArrayList<Node> shortestPath = new ArrayList<>();	
		double dis2 =0;
		shortestPath = this.getShortestPath(toNode,shortestPath);
		ArrayList<String> roadName = new ArrayList<>();
		ArrayList<Double> lengths = new ArrayList<>();
		String roadName1="";
		double roadLength =0;
		Node node1 = shortestPath.get(0);
		Node node2 = shortestPath.get(1);
		for(Segment segment: node1.segments) {
			if((segment.start==node1 && segment.end==node2) || segment.end==node1 && segment.start==node2) {
				roadName1 = graph.roads.get(segment.roadID).name;
			}
			break;
		}
		for(int i=0; i<shortestPath.size()-1;i++) {
			Node from = shortestPath.get(i);
			Node to = shortestPath.get(i+1);
			ArrayList<Segment> segments = new ArrayList<>(from.segments);
			for(Segment segment: segments) {
				if(graph.roads.get(segment.roadID).oneWay==0) {
					if((segment.start==from && segment.end==to) || (segment.start==to && segment.end == from)){
						graph.highlightedSegments.add(segment);
						if(graph.roads.get(segment.roadID).name.equals(roadName1)) {
							roadLength = roadLength + segment.length;
						}
						else {
							roadName.add(roadName1); 
							lengths.add(roadLength);
							roadName1 =graph.roads.get(segment.roadID).name;
							roadLength=segment.length;
						}
					}
				}
				if(graph.roads.get(segment.roadID).oneWay==1) {
					if(segment.start== from && segment.end==to) {
						graph.highlightedSegments.add(segment);
						if(graph.roads.get(segment.roadID).name.equals(roadName1)) {roadLength = roadLength + segment.length;}
						else {
							roadName.add(roadName1); 
							lengths.add(roadLength);
							roadName1 =graph.roads.get(segment.roadID).name;
							roadLength=segment.length;
						}
					}
				}			
			}
			if(i+1==shortestPath.size()-1) {
				roadName.add(roadName1);
				lengths.add(roadLength);
			}
			
		}
		for(int j=0; j<lengths.size();j++) {
//			System.out.println(lengths.size());
//			System.out.println(roadName.size());
			double length = lengths.get(j);
			dis2 = dis2+length;
			String road = roadName.get(j);

			text = text+"\n" + road + ": " + length;
		}
		text = text + "\n" + dis2;
		getTextOutputArea().setText(text);
		fromNode = null;
		toNode = null;
	}
	
	
	public ArrayList<Node> getShortestPath(Node current,ArrayList<Node> path){
		while(current!=null) {
			path.add(0,current);
			current=current.getPre();
		}
		return path;
	}
	
	
	@Override
	protected void onAPs() {
		// getting a random node
		findAllNeighbourForAllNodes();
		splitNodeGroups();
		for(int i=0; i<nodeGroups.size();i++) {
			Node rootNode = nodeGroups.get(i).get(0);		
			rootNode.setDepth(0);
			for(Node neighbour: rootNode.getNeighbour()) {
				if(neighbour.getDepth()==-1) {
					iterArtPts(neighbour,1,rootNode);
					 rootNode.setNOSubtree();
				}
				if(rootNode.getNOSubtree()>1) {
					if(!graph.highlightedAPs.contains(rootNode)) {
						graph.highlightedAPs.add(rootNode);
					}
				}
			}
		}
		
		System.out.println(" xx " + graph.highlightedAPs.size());
		
	}
	
	public void iterArtPts(Node firstNode, int depth, Node root) {
		Stack<APs> fringe1 = new Stack<>();
		APs element = new APs(firstNode, depth, root);
		fringe1.push(element);
		
		while(!fringe1.isEmpty()) {
			APs point = fringe1.peek();
			Node currentNode = point.node;
			
			// if point hasn't been visited	
			if(currentNode.getDepth()==-1) {
				currentNode.setDepth(point.getDepth());
				currentNode.setReachBack(point.getDepth());
				ArrayList<Segment> segments = new ArrayList<>(currentNode.segments);
				for(Segment segment:segments) {
					// adding all neighbors that are not the parent
					if(segment.start==currentNode && segment.end!=point.getParent()) {
						// if start is current and end is not parent
						currentNode.addToNeighbour(segment.end); 
					}
					else if(segment.end==currentNode && segment.start!=point.getParent()) {
						currentNode.addToNeighbour(segment.start);
					}
				}
			}
			
			// if node is visited but children not empty
			else if(!currentNode.getNeighbour().isEmpty()) {
				Node neighbour = currentNode.getNeighbour().get(0);
				currentNode.removeNeighbour(neighbour);
				if(neighbour.getDepth()!= -1) {
					currentNode.setReachBack(min(neighbour.getDepth(),currentNode.getReachBack()));
				}
				else {
					APs newPoint = new APs(neighbour,point.getDepth()+1,currentNode);
					fringe1.push(newPoint);
				}
			}
			
			// if node is visited but no more children
			else {
				if(currentNode!=firstNode) {
					point.getParent().setReachBack(min(currentNode.getReachBack(),
													   point.getParent().getReachBack()));
					if(currentNode.getReachBack()>= point.getParent().getDepth()) {
						if(!graph.highlightedAPs.contains(point.getParent())) {
							graph.highlightedAPs.add(point.getParent());
						}
					}
				}
				fringe1.remove(point);
			}
		}

	}
	
	public int min(int x, int y) {
		if(x-y>0) {return y;}
		else {return x;}
	}
	
	public void splitNodeGroups() {
		ArrayList<Node> copyListOfAllNodes = new ArrayList<>(graph.nodes.values());
		ArrayList<Node> visited = new ArrayList<Node>();
		while(!copyListOfAllNodes.isEmpty()) {
			ArrayList<Node> group = new ArrayList<>();
			Node rootNode = copyListOfAllNodes.get(0);
			group = recursiveCall(rootNode, group,visited);
			nodeGroups.add(group);
			copyListOfAllNodes.removeAll(group);
			visited.addAll(group);
		}
				
	}
	
	public ArrayList<Node> recursiveCall(Node node, ArrayList<Node> group,ArrayList<Node> visited) {
		visited.add(node);
		group.add(node);
		for(Node neighbour: node.getNeighbour()) {
			if(!visited.contains(neighbour)) {
				node = neighbour;
				group = recursiveCall(node, group,visited);
			}
		}
		return group;
	}
	
	public void findAllNeighbourForAllNodes() {
		for(Node fromNode: graph.nodes.values()) {
			for(Segment segment: fromNode.segments) {
				// if the segment of the road isn't one way 
				if(graph.roads.get(segment.roadID).oneWay==0) {
					if(segment.start == fromNode) {
						//adding to the outgoing neighbour
						fromNode.addToOutg(segment.end);
						//adding to the outgoing segment
						fromNode.addToOutSegments(segment);
						if(!fromNode.getNeighbour().contains(segment.end)) { // in case there's duplicate
							fromNode.addToNeighbour(segment.end);
							fromNode.addToNeighbourtSegments(segment);
						}
					}
					if(segment.end==fromNode) {
						fromNode.addToOutg(segment.start);
						//adding to the outgoing segment
						fromNode.addToOutSegments(segment);
						if(!fromNode.getNeighbour().contains(segment.start)) { // in case there's duplicate
							fromNode.addToNeighbour(segment.start);
							fromNode.addToNeighbourtSegments(segment);
						}
					}
				}
				// if the segment is from a one way road only add in the same order
				else if(graph.roads.get(segment.roadID).oneWay==1) {
					if(segment.start==fromNode) {
						fromNode.addToOutg(segment.end);
						fromNode.addToOutSegments(segment);
						fromNode.addToNeighbour(segment.end);
						fromNode.addToNeighbourtSegments(segment);
						
					}
					if(segment.end==fromNode) { // one way but still can be neighbour
						if(!fromNode.getNeighbour().contains(segment.start)) { // in case there's duplicate
							fromNode.addToNeighbour(segment.start);
							fromNode.addToNeighbourtSegments(segment);
						}
					}
				}			
			}
		}
	}

	@Override
	protected void onDistance() {
		distance = true;
		time = false;
		
		
	}

	@Override
	protected void onTime() {
		distance = false;
		time = true;
		

		
	}
	public double euclideanDistance(Node formNode, Node toNode) {
		return fromNode.location.distance(toNode.location);
	}
	
	public double estimatedTime(Node fromNode, Node toNode) {
		// average speed in the map is 80
		// average road class is 2
		// heurisicTime will be the eucledean distance/(average speed + average roadclass)
		double estimate = euclideanDistance(fromNode,toNode)/(80+3);
		return estimate;
	}
	
	public double timeNeedToGoThroughSegment(Segment segment) {
		double speedLimit = graph.roads.get(segment.roadID).getSpeedLimit();
		double time = segment.length / (speedLimit+graph.roads.get(segment.roadID).roadclass);
		return time;
		}
}





// code for COMP261 assignments