package code;

public class AStarElem implements Comparable<AStarElem>{
	private double  g= 0;
	private double  f= 0; // f = g + heuristic of neighbor
	private Node previous=null;
	private Node currentNode = null;
	
	public AStarElem(Node node, Node prev, double g1, double f1) {
		g = g1; 		// g is actual distance
		f = f1;			// f is estimated distance ( f = g + estimated )
		currentNode = node;
		previous = prev;
	}
	
	
	public Node getCurrentNode() {return currentNode;}
	public Node getPreviousNode() {return previous;}
	public double getG() {return g;}

	@Override
	public int compareTo(AStarElem o) {
		if (this.f > o.f) {
			return 1;
		} else if (this.f < o.f) {
			return -1;
		}

		return 0;
	}
	
}
