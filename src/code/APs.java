package code;

import java.util.ArrayList;

public class APs {
	public Node node = null;
	private int depth = 0;
	public Node parent = null;
	private int reachBack =0;
	
	public APs(Node node1, int depth1, Node parent1) {
		node = node1;
		depth = depth1;
		parent = parent1;		
	}
	
	public int getDepth() {return depth;}
	public int getReachBack() {return reachBack;}
	public void setDepth(int x) {depth=x;}
	public void setReachBack(int x) {reachBack=x;}
	public Node getParent() {return parent;}
}
