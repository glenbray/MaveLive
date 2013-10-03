package com.mazelive;

public class Cell {
	
	private int x;
	private int y;
	
	//Link for Paths
	private Cell next;
		
	private boolean isStart;
	private boolean isEnd;	
	private boolean visited;
	private boolean path;
	private boolean doublePath;
	
	//Walls
	private boolean north;
	private boolean south;
	private boolean east;
	private boolean west;
	
	public Cell(int x, int y)
	{		
		this.north = true;
		this.south = true;
		this.east = true;
		this.west = true;
		this.visited = false;
		this.path = false;
		this.doublePath = false;
		this.x = x;
		this.y = y;
	}
	
	public boolean isNorth() {
		return north;
	}

	public boolean isSouth() {
		return south;
	}

	public boolean isEast() {
		return east;
	}

	public boolean isWest() {
		return west;
	}

	public void setNorth(boolean north) {
		this.north = north;
	}

	public void setSouth(boolean south) {
		this.south = south;
	}

	public void setEast(boolean east) {
		this.east = east;
	}

	public void setWest(boolean west) {
		this.west = west;
	}
	
	public boolean isStart() {
		return isStart;
	}

	public boolean isEnd() {
		return isEnd;
	}

	public boolean isVisited() {
		return visited;
	}

	public boolean isPath() {
		return path;
	}

	public boolean isDoublePath() {
		return doublePath;
	}

	public void setStart(boolean start)
	{
		this.isStart = start;
	}
	
	public void setEnd(boolean end)
	{
		this.isEnd = end;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public void setPath(boolean path) {
		this.path = path;
	}

	public void setDoublePath(boolean doublePath) {
		this.doublePath = doublePath;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Cell getNext() {
		return next;
	}

	public void setNext(Cell next) {
		this.next = next;
	}	
}
