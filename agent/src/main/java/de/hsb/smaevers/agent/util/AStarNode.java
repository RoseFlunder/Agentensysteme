package de.hsb.smaevers.agent.util;

public class AStarNode <T> implements Comparable<AStarNode<T>>{
	
	private T data = null;
	private AStarNode<T> predecessor = null;
	private int g = 0;
	private int h = 0;
	
	public AStarNode(T data, int h){
		this.data = data;
		this.h = h;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public AStarNode<T> getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(AStarNode<T> predecessor) {
		this.predecessor = predecessor;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}
	
	public int getF(){
		return g + h;
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (data != null && obj != null && obj instanceof AStarNode){
			AStarNode<?> other = (AStarNode<?>) obj;
			return data.equals(other.getData());
		}
			
		return this == obj;
	}

	@Override
	public int compareTo(AStarNode<T> o) {
		return Integer.compare(getF(), o.getF());
	}
	
	
}
