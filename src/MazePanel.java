import javax.swing.*;
import java.awt.*;

public class MazePanel extends JPanel {
    private int[][] map;
    private JPanel controlPanel;
    private final int CELL_SIZE = 25;
    private final int ROWS = 21;
    private final int COLS = 21;

    public MazePanel(int[][] map) {
        this.map = map;
        this.setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
    }

    public void setMap(int[][] newMap) {
        this.map = newMap;
    }

    @Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(map == null) return;
		for(int r = 0; r < ROWS; r++) {
			for(int c = 0; c < COLS; c++) {
                if(r == 1 && c == 1) {
                    g.setColor(Color.GREEN);
                } else if (r == ROWS - 2 && c == COLS - 2) {
                    g.setColor(Color.RED);
                } else if(map[r][c] == 0) {
					g.setColor(Color.BLACK);
				} else if(map[r][c] == 1) {
					g.setColor(Color.WHITE);
				} else if(map[r][c] == 2) {
					g.setColor(Color.CYAN);
				} else if(map[r][c] == 3) {
					g.setColor(Color.YELLOW);
				}
                g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}
	}

}