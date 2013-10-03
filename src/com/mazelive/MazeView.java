package com.mazelive;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;

public class MazeView extends SurfaceView implements Runnable {

	private Paint paint = new Paint();
	private boolean drawing;
	private Cell[][] maze;
	private int height;
	private int length;
	private int pixels;

	int newX;

	public MazeView(Context context, int mazeLength, int mazeHeight) {
		super(context);
		this.length = mazeLength;
		this.height = mazeHeight;		
		this.pixels = 30;
		this.maze = new Cell[mazeLength][mazeHeight];
		this.drawing = true;
		this.generateMaze();
		newX = 0;
		
		//Start thread to solve solution
		Thread t = new Thread(this);
		t.start();
	}

	private synchronized void incrementX() {
		++this.newX;
	}
	
	private synchronized int read() {
		return this.newX;
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		int x = 0, y = 0;
		this.paint.setColor(Color.YELLOW);
		this.paint.setStrokeWidth(3);


		for(int i = 0; i <= this.length - 1; i++) // Row
		{
			for(int j = 0; j <= this.height - 1; j++) // Column
			{ 
				Cell cell = maze[j][i];

				if(cell.isStart() || cell.isEnd())
					this.fillRectangle(canvas, x, y);

				if(cell.isNorth())
					canvas.drawLine(x, y, x + this.pixels, y, paint); // north wall

				if(cell.isSouth())
					canvas.drawLine(x, y + this.pixels, x + this.pixels, y + this.pixels, paint); // south wall

				if(cell.isEast())
					canvas.drawLine(x + this.pixels, y, x + this.pixels, y + this.pixels, paint); // east wall

				if(cell.isWest())
					canvas.drawLine(x, y, x, y + this.pixels, paint); // west wall

				x = x + this.pixels;
			}
			y = y + this.pixels;
			x = 0;
		}
		
		//Draw Maze Path
		this.paint.setColor(Color.rgb(186, 85, 211));
		this.paint.setStrokeWidth(10);		
		
		Cell current = this.maze[0][0];
		int pathCount = 0;		
		x = this.pixels / 2; 
		y = this.pixels / 2;
		
		while(pathCount <= this.read()) {
			Cell next = null;
			if(current.getNext() != null)
				next = current.getNext();
			else
				break;
			
			int cx = current.getX(), cy = current.getY();
			int nx = next.getX(), ny = next.getY();
			
			//If going north or south
			if(cx == nx) {
				//Go south
				if(cy < ny) {
					canvas.drawLine(x, y, x, y = y + this.pixels, paint);
				}
				//Go north
				else {
					canvas.drawLine(x, y - this.pixels, x, y, paint);
					y = y - this.pixels;
				}			
			}		
			//If going east or west
			if(cy == ny) {
				//Go west
				if(cx > nx) {
					canvas.drawLine(x - this.pixels, y, x, y, paint);
					x = x - this.pixels;
				}
				//Go east
				else {
					canvas.drawLine(x, y, x = x + this.pixels, y, paint);
				}
			}
			++pathCount;			
			current = next;
		}		
		this.paint.setStrokeWidth(3);

	}

	public void run() {

		Cell cell = this.maze[0][0];
		this.solveMaze();

		int pathCount = 0;
		
		while(!cell.isEnd()) {
			++pathCount;
			cell = cell.getNext();
		}
		
		while(drawing) {
			if(this.newX >= pathCount)
				drawing = false;
			else
			{
				try {

					Thread.sleep(60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				this.incrementX();
				postInvalidate();

			}
		}
	}

	public void solveMaze() {

		Cell cell = this.maze[0][0];

		while(!cell.isEnd()) {
			cell = this.getNeighbour(cell.getX(), cell.getY());
			if(cell.isEnd()) {
				cell.setPath(true);
				this.maze[this.length - 1][this.height - 1] = cell;
			}
		}

		// 1. Directional arrays - Up, Down, Right, Left
		int x = 0, y = 0;

		// 2. Check which directions are available to move to and store those cells in list
		Cell current = this.maze[x][y];
		
		while(!current.isEnd()) {
			
			int xN[] = {x, x, x + 1, x - 1};
			int yN[] = {y - 1, y + 1, y, y};

			ArrayList<Cell> neighbours = new ArrayList<Cell>();

			current.setPath(false);
			this.maze[x][y] = current;
			
			for(int i = 0; i <= 3; ++i) {
				int tempX = xN[i], tempY = yN[i];			

				if(tempX < 0 || tempY < 0 || tempX > this.length - 1 || tempY > this.height - 1)
					continue;

				// 3. Get cells available
				Cell temp = null;

				switch(i)
				{
				case 0: // North
					if(!current.isNorth()) {
						temp = this.maze[tempX][tempY];
						neighbours.add(temp);

					}
					break;
				case 1: // South
					if(!current.isSouth()) {
						temp = this.maze[tempX][tempY];
						neighbours.add(temp);
					}

					break;
				case 2: // East
					if(!current.isEast()) {
						temp = this.maze[tempX][tempY];
						neighbours.add(temp);
					}
					break;
				case 3: // West
					if(!current.isWest()) {
						temp = this.maze[tempX][tempY];
						neighbours.add(temp);
					}
					break;					
				}
			}

			// 3. Attach objects to neighbours with PATHS only
			Cell next = null;

			for(int i = 0; i < neighbours.size(); ++i) {
				next = neighbours.get(i);
				if(next.isPath() && !next.isDoublePath())
					break;
			}
			current.setNext(next);
			this.maze[current.getX()][current.getY()] = current;
			this.maze[next.getX()][next.getY()] = next;

			x = next.getX(); y = next.getY();
			current = next;
		}
	}

	private Cell getNeighbour(int x, int y) {
		Cell cell = this.maze[x][y];
		cell.setPath(true);

		// 1. Directional arrays - Up, Down, Right, Left
		int xN[] = {x, x, x + 1, x - 1};
		int yN[] = {y - 1, y + 1, y, y};

		// 2. Check which directions are available to move to and store those cells in list
		ArrayList<Cell> neighbours = new ArrayList<Cell>();

		for(int i = 0; i <= 3; ++i) {
			int tempX = xN[i], tempY = yN[i];			

			if(tempX < 0 || tempY < 0 || tempX > this.length - 1 || tempY > this.height - 1)
				continue;

			// 3. Get cells available
			Cell temp = null;

			switch(i)
			{
			case 0: // North
				if(!cell.isNorth()) {
					temp = this.maze[tempX][tempY];
					neighbours.add(temp);
				}
				break;
			case 1: // South
				if(!cell.isSouth()) {
					temp = this.maze[tempX][tempY];
					neighbours.add(temp);
				}

				break;
			case 2: // East
				if(!cell.isEast()) {
					temp = this.maze[tempX][tempY];
					neighbours.add(temp);
				}
				break;
			case 3: // West
				if(!cell.isWest()) {
					temp = this.maze[tempX][tempY];
					neighbours.add(temp);
				}
				break;					
			}
		}
		if(neighbours.size() == 1) {

			this.maze[x][y] = cell;
			return neighbours.get(0);
		}
		else if(neighbours.size() == 0)
			return null;
		else
		{
			// 4. Select cell to move to randomly			
			Random rand = new Random();
			ArrayList<Cell> paths = new ArrayList<Cell>();

			//Check for paths that have not been taken yet
			for(int i = 0; i <= neighbours.size() - 1; i++) {
				Cell path = neighbours.get(i);
				if(!path.isPath())
					paths.add(path);
			}

			Cell nextCell = null;
			// Priorities paths that have not been taken yet otherwise backtrack
			if(paths.size() > 0)
			{
				if(paths.size() == 1)
					return paths.get(0);

				nextCell = paths.get(rand.nextInt(paths.size()));
			}
			else {
				nextCell = neighbours.get(rand.nextInt(neighbours.size()));
				cell.setDoublePath(true);
				//				nextCell.setDoublePath(true);
			}

			// 5. Set path in current cell
			this.maze[x][y] = cell;

			// 6. Return next cell
			return nextCell;
		}
	}

	private void fillRectangle(Canvas canvas, int x, int y) {		
		if(x == 0 && y == 0)
			paint.setColor(Color.BLUE);
		else
			paint.setColor(Color.RED);
		canvas.drawRect(x + 4, y + 4, x + this.pixels - 4, y + this.pixels - 4, paint);
		paint.setColor(Color.GREEN);
	}

	//Generate a random maze
	public void generateMaze() {
		// 1. Initialise maze array, set start and end positions
		for(int i = 0; i <= this.length - 1; i++) // ROW
		{
			for(int j = 0; j <= this.height - 1; j++) // COLOMN
			{	
				Cell cell = new Cell(i, j);
				if(i == 0 && j == 0)
					cell.setStart(true);
				if(i == this.length - 1 && j == this.height - 1)
					cell.setEnd(true);

				this.maze[i][j] = cell;
			}
		}

		Stack<int[]> stack = new Stack<int[]>();
		int totalCells = this.length * this.height - 1;
		int visited = 0;

		//Start at position 0, 0
		int x = 0;
		int y = 0;

		while(visited < totalCells)
		{
			//2. Find all neighbouring Cells
			int[] xx = new int[]{x, x, x + 1, x - 1};
			int[] yy = new int[]{y - 1, y + 1, y , y};

			boolean exists = false;
			for(int i = 0; i < 4; i++) // Check neighbour cells
			{
				int l = xx[i]; //x
				int h = yy[i]; //y

				try
				{	
					if(!this.maze[l][h].isVisited())
					{
						exists = true;
						break;
					}
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					continue;
				}
			}			

			Cell current = this.maze[x][y];

			// 3. Check if Neighbour exists and check if current Cell is the end
			if(exists && !current.isEnd())
			{
				int index = 0;
				Cell next = null;

				int[] c = new int[]{x, y};
				stack.push(c);

				//4. Check for a valid Neighbour cell, set x, y to next Cell
				boolean valid = false;
				while(!valid)
				{
					index = this.getDirections();
					x = xx[index];
					y = yy[index];

					if(x < 0 || y < 0 || x > this.length - 1 || y > this.height - 1)
						continue;

					if(this.maze[x][y].isVisited())
						continue;

					valid = true;
					break;
				}

				int[] pxpy = stack.peek();				
				next = this.maze[x][y];

				switch(index)
				{
				case 0: // North
					current.setNorth(false);
					next.setSouth(false);
					break;
				case 1: // South
					current.setSouth(false);
					next.setNorth(false);
					break;
				case 2: // East
					current.setEast(false);
					next.setWest(false);
					break;
				case 3:  // West
					current.setWest(false);
					next.setEast(false);
					break;
				}

				current.setVisited(true);
				this.maze[pxpy[0]][pxpy[1]] = current;
				this.maze[x][y] = next;	
				++visited;

				if(visited == totalCells)
				{					
					next.setVisited(true);
					this.maze[x][y] = next;
				}
			}
			else
			{
				if(stack.size() > 0)
				{
					this.maze[x][y].setVisited(true);
					int[] c = stack.pop();
					x = c[0];
					y = c[1];
				}
			}
		}
	}

	//Get a random direction: 0, 1, 2, 3
	private int getDirections() {	
		int[] num = new int[] {0, 1, 2, 3};
		Random r = new Random();

		for(int i = 0; i < num.length; i++)
		{
			int index = r.nextInt(3);

			int temp = num[i];
			num[i] = num[index];
			num[index] = temp;
		}		
		int value = num[r.nextInt(3)];

		return value;
	}
}
