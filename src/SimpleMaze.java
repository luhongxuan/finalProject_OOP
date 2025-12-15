import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class SimpleMaze extends JPanel {
    // 1. 設定迷宮大小與常數
    private final int CELL_SIZE = 30; 
    private final int ROWS = 21;      
    private final int COLS = 21;      
    
    // 0:牆壁, 1:路, 2:路徑點(搜尋痕跡), 3:最終路徑
    private int[][] map; 
    private boolean isAnimating = false; // 防止重複點擊的旗標
    
    public SimpleMaze() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        generateMaze(); 

        // 2. 滑鼠點擊事件
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 如果正在動畫中，就不理會點擊
                if (isAnimating) return;

                // 【修改 1】開啟一個新的執行緒來跑動畫，才不會讓視窗卡死
                new Thread(() -> {
                    isAnimating = true;
                    generateMaze(); // 重新生成
                    repaint();      // 先畫出剛生成的空迷宮
                    
                    try { Thread.sleep(500); } catch (Exception ex) {} // 停頓一下讓你看清楚起點
                    
                    solveMaze(1, 1); // 開始解，這裡面會有動畫延遲
                    isAnimating = false;
                }).start();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 為了避免還沒生成地圖就繪圖導致錯誤
        if (map == null) return; 

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                switch (map[y][x]) {
                    case 0: g.setColor(Color.BLACK); break;      
                    case 1: g.setColor(Color.WHITE); break;      
                    case 2: g.setColor(Color.YELLOW); break;     
                    case 3: g.setColor(Color.BLUE); break;       
                }
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    // 4. 迷宮生成 (這裡沒有動畫，瞬間完成)
    private void generateMaze() {
        map = new int[ROWS][COLS]; 
        carve(1, 1);
        map[1][1] = 1;
        map[ROWS - 2][COLS - 2] = 1;
    }

    private void carve(int x, int y) {
        map[y][x] = 1; 
        int[][] dirs = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        ArrayList<int[]> dirList = new ArrayList<>();
        Collections.addAll(dirList, dirs);
        Collections.shuffle(dirList);

        for (int[] dir : dirList) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx > 0 && nx < COLS - 1 && ny > 0 && ny < ROWS - 1 && map[ny][nx] == 0) {
                map[y + dir[1]/2][x + dir[0]/2] = 1; 
                carve(nx, ny); 
            }
        }
    }

    // 5. 【修改 2】加入動畫的路徑搜尋
    private boolean solveMaze(int x, int y) {
        if (x < 0 || x >= COLS || y < 0 || y >= ROWS || map[y][x] != 1) {
            return false;
        }

        map[y][x] = 2; // 標記黃色 (走過)
        
        // --- 動畫關鍵區 ---
        repaint(); // 通知畫面更新
        try { 
            Thread.sleep(50); // 暫停 50 毫秒 (你可以調整這個數字改變速度)
        } catch (InterruptedException e) {}
        // ----------------

        if (x == COLS - 2 && y == ROWS - 2) {
            map[y][x] = 3; 
            repaint();
            return true;
        }

        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for (int[] dir : dirs) {
            if (solveMaze(x + dir[0], y + dir[1])) {
                map[y][x] = 3; // 標記藍色 (正確路徑)
                // 如果想要回溯路徑也有動畫，可以把下面的註解打開
                /*
                repaint();
                try { Thread.sleep(20); } catch (InterruptedException e) {}
                */
                return true;
            }
        }
        return false; 
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple Animated Maze");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SimpleMaze());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}