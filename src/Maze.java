import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.Deque;
import java.util.ArrayDeque;

public class Maze extends JFrame {
	private final int  CELL_SIZE = 25;
	private final int ROWS = 21;
	private final int COLS = 21;
	private int[][] map;
	private boolean isAnimating = false;
	private boolean isEditing = false;

	private JPanel mapContainer;
	private MazePanel mazePanel;
	private JTextArea statusArea;
	private JComboBox<String> algoSelector;
	private JButton generateBtn;
	private JButton searchBtn;
	private JButton editBtn;
	private JButton clearMapBtn;
	private JPanel controlPanel;

	private MouseAdapter mazeMouseAdapter;
	
	public Maze() {
		super("Java Maze Solver (BFS / DFS)");
		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension((COLS + 10) * CELL_SIZE, (ROWS + 10) * CELL_SIZE));

		this.statusArea = new JTextArea(3, 30);
		this.statusArea.setEditable(false);
		this.add(new JScrollPane(this.statusArea), BorderLayout.SOUTH);

		this.generateMaze();
		
		this.algoSelector = new JComboBox<>(new String[] {"BFS", "DFS"});
		this.generateBtn = new JButton("Generate Maze");
		this.searchBtn = new JButton("Search Path");
		this.editBtn = new JButton("Edit Mode: OFF");
		this.clearMapBtn = new JButton("Clear Map");

		this.controlPanel = new JPanel();
		this.controlPanel.add(new JLabel("Algorithm:"));
		this.controlPanel.add(this.algoSelector);
		this.controlPanel.add(this.generateBtn);
		this.controlPanel.add(this.searchBtn);
		this.controlPanel.add(this.editBtn);
		this.controlPanel.add(this.clearMapBtn);

		this.mapContainer = new JPanel(new GridBagLayout());
		this.mapContainer.setBackground(Color.DARK_GRAY);
		
		this.mazePanel = new MazePanel(this.map);
		this.mapContainer.add(this.mazePanel);

		this.add(this.mapContainer, BorderLayout.CENTER);

		this.generateBtn.addActionListener(e -> {
			if(isAnimating) return;
			this.generateMaze();
			this.mazePanel.setMap(this.map);
			this.mazePanel.repaint();
		});
		this.searchBtn.addActionListener(e -> this.startSearch());
		this.clearMapBtn.addActionListener(e -> this.clearMap());
		this.editBtn.addActionListener(e -> {
			isEditing = !isEditing;
			if(isEditing) {
				this.editBtn.setText("Edit Mode: ON");
				this.statusArea.setText("Edit mode enabled. Click cells to toggle walls.");
				this.searchBtn.setEnabled(false);
			}else{
				this.editBtn.setText("Edit Mode: OFF");
				this.statusArea.setText("Edit mode disabled.");
				this.searchBtn.setEnabled(true);
			}
		});

		this.mazeMouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Maze.this.handleMouseInput(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Maze.this.handleMouseInput(e);
			}
		};

		this.mazePanel.addMouseListener(this.mazeMouseAdapter);
		this.mazePanel.addMouseMotionListener(this.mazeMouseAdapter);
		
		this.add(controlPanel, BorderLayout.NORTH);
		this.pack();
		this.setVisible(true);
	}

	private void generateMaze() {
		this.map = new int[ROWS][COLS];
		this.carve(1, 1);
		this.map[1][1] = 1;
		this.map[ROWS - 2][COLS - 2] = 1;
		this.statusArea.setText("New maze generated. Ready to search!");
	}
	
	private void carve(int r, int c) {
		map[r][c] = 1;
		int[][] dirs =  {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
		ArrayList<int[]> dirList = new ArrayList<>();
		Collections.addAll(dirList, dirs);
		Collections.shuffle(dirList);

		for(int[] dir : dirList) {
			int nr = r + dir[0];
			int nc = c + dir[1];
			if(nr > 0 && nr < ROWS - 1 && nc > 0 && nc < COLS - 1 && map[nr][nc] == 0) {
				map[r + dir[0] / 2][c + dir[1] / 2] = 1;
				carve(nr, nc);
			}
		}
	}

	private void clearMap() {
		for(int r = 1; r < ROWS - 1; r++) {
			for(int c = 1; c < COLS - 1; c++) {
				map[r][c] = 1;
			}
		}
		this.mazePanel.repaint();
	}

	public void startSearch() {
		if(isAnimating) return;

		this.resetPath();

		String selectAlgo = (String) this.algoSelector.getSelectedItem();
		new Thread(() -> {
			isAnimating = true;
			this.mazePanel.repaint();

			try{
				Thread.sleep(500);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			if (selectAlgo.equals("DFS")) {
				this.startSolveDFS(1, 1);
			}else if (selectAlgo.equals("BFS")) {
				this.solveBFS(1, 1);
			}
			isAnimating = false;
		}).start();
		this.statusArea.setText("Searching using " + selectAlgo + "...");
	}

	private void resetPath() {
		for(int r = 0; r < ROWS; r++) {
			for(int c = 0; c < COLS; c++) {
				if(map[r][c] == 2 || map[r][c] == 3) {
					map[r][c] = 1;
				}
			}
		}
		this.mazePanel.repaint();
	}

	int nodesDFSVisited = 0;
	private void startSolveDFS(int startRow, int startCol) {
		if(startRow < 0 || startRow >= ROWS || startCol < 0 || startCol >= COLS) return;
		long startTime = System.currentTimeMillis();
		if(this.solveDFS(startRow, startCol)){
			long duration = System.currentTimeMillis() - startTime;
			this.statusArea.setText("DFS completed! Nodes visited: " + this.nodesDFSVisited + ". Time taken: " + duration + " ms.");
		}else{
			long duration = System.currentTimeMillis() - startTime;
			this.statusArea.setText("DFS failed! No path found. Nodes visited: " + this.nodesDFSVisited + ". Time taken: " + duration + " ms.");
		}		
	}

	private boolean solveDFS(int r, int c) {
		if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return false;
		map[r][c] = 3;
		this.nodesDFSVisited++;

		this.mazePanel.repaint();

		try{
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(r == ROWS - 2 && c == COLS - 2) {
			map[r][c] = 3;
			this.mazePanel.repaint();
			return true;
		}

		int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
		for (int[] dir : directions) {
			int nr = r + dir[0];
			int nc = c + dir[1];
			if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && (map[nr][nc] == 1 || map[nr][nc] == 2)) {
				if (solveDFS(nr, nc)) {
					map[r][c] = 2;
					this.mazePanel.repaint();
					try{
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return true;
				}
			}
		}
		return false;
	}

	private void solveBFS(int startRow, int startCol) {
		if(startRow < 0 || startRow >= ROWS || startCol < 0 || startCol >= COLS) return;
		Deque<int[]> deque = new ArrayDeque<>();

		int[][][] parent = new int[ROWS][COLS][2];

		deque.offer(new int[]{startRow, startCol});
		map[startRow][startCol] = 3;
		this.mazePanel.repaint();

		try{
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int nodeVisited = 0;
		long startTime = System.currentTimeMillis();
		boolean found = false;

		while(!deque.isEmpty()){
			int[] current = deque.poll();
			int r = current[0];
			int c = current[1];

			nodeVisited++;
			if(r == ROWS - 2 && c == COLS - 2){
				found = true;
				break;
			}

			int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};	
			for(int[] dir : directions) {
				int nr = r + dir[0];
				int nc = c + dir[1];
				if(nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && (map[nr][nc] == 1 || map[nr][nc] == 2)) {
					deque.offer(new int[]{nr, nc});
					map[nr][nc] = 3;
					parent[nr][nc][0] = r;
					parent[nr][nc][1] = c;

					this.mazePanel.repaint();
					try{
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(found) {
			int r = ROWS - 2;
			int c = COLS - 2;
			
			while(r != startRow || c != startCol) {
				this.map[r][c] = 2;
				this.mazePanel.repaint();
				try{
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int pr = parent[r][c][0];
				int pc = parent[r][c][1];

				r = pr;
				c = pc;
			}
			map[startRow][startCol] = 2;
			this.mazePanel.repaint();
			long duration = System.currentTimeMillis() - startTime;
			this.statusArea.setText("BFS completed! Nodes visited: " + nodeVisited + ". Time taken: " + duration + " ms.");
		}else{
			long duration = System.currentTimeMillis() - startTime;
			this.statusArea.setText("BFS failed! No path found. Nodes visited: " + nodeVisited + ". Time taken: " + duration + " ms.");
		}
	}

	private int paintMode = 0;
	private void handleMouseInput(MouseEvent e) {
		if (!isEditing) return;
		
		int c = e.getX() / CELL_SIZE;
		int r = e.getY() / CELL_SIZE;

		if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
			if ((r == 1 && c == 1) || (r == ROWS - 2 && c == COLS - 2)) return;

			if(e.getID() == MouseEvent.MOUSE_PRESSED) {
				if (map[r][c] == 1) {
					this.paintMode = 0;
				}else{
					this.paintMode = 1;
				}
				map[r][c] = this.paintMode;
			}else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				map[r][c] = this.paintMode;
			}

			this.mazePanel.repaint();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Maze();
		});
	}
}