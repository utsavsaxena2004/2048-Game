import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Game2048 extends JPanel implements KeyListener {
    private static final int GRID_SIZE = 4;
    private static final int TILE_SIZE = 100;
    private static final int TILE_MARGIN = 16;
    private int[][] board;
    private boolean gameOver;

    public Game2048() {
        setPreferredSize(new Dimension(500, 500));
        setBackground(new Color(0xbbada0));
        setFocusable(true);
        addKeyListener(this);
        board = new int[GRID_SIZE][GRID_SIZE];
        addRandomTile();
        addRandomTile();
    }

    private void addRandomTile() {
        List<Point> empty = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] == 0) {
                    empty.add(new Point(i, j));
                }
            }
        }
        if (!empty.isEmpty()) {
            Point p = empty.get((int)(Math.random() * empty.size()));
            board[p.x][p.y] = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = board[i];
            int[] newRow = new int[GRID_SIZE];
            int last = 0;
            for (int j = 0; j < GRID_SIZE; j++) {
                if (row[j] != 0) {
                    if (newRow[last] == 0) {
                        newRow[last] = row[j];
                    } else if (newRow[last] == row[j]) {
                        newRow[last++] *= 2;
                    } else {
                        newRow[++last] = row[j];
                    }
                }
            }
            if (!Arrays.equals(row, newRow)) {
                board[i] = newRow;
                moved = true;
            }
        }
        return moved;
    }

    private void rotateBoardClockwise() {
        int[][] newBoard = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                newBoard[j][GRID_SIZE - 1 - i] = board[i][j];
            }
        }
        board = newBoard;
    }

    private boolean move(Direction dir) {
        boolean moved = false;
        for (int i = 0; i < dir.rotations; i++) rotateBoardClockwise();
        moved = moveLeft();
        for (int i = 0; i < (4 - dir.rotations) % 4; i++) rotateBoardClockwise();
        if (moved) addRandomTile();
        repaint();
        checkGameOver();
        return moved;
    }

    private void checkGameOver() {
        for (int[] row : board)
            for (int cell : row)
                if (cell == 0) return;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE - 1; j++) {
                if (board[i][j] == board[i][j + 1] || board[j][i] == board[j + 1][i]) return;
            }
        }
        gameOver = true;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                drawTile(g2, board[i][j], j, i);
            }
        }
        if (gameOver) {
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            g2.drawString("Game Over", 130, 250);
        }
    }

    private void drawTile(Graphics2D g, int value, int x, int y) {
        int xOffset = offsetCoors(x);
        int yOffset = offsetCoors(y);
        g.setColor(getBackground(value));
        g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
        g.setColor(getForeground(value));
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String s = value > 0 ? String.valueOf(value) : "";
        FontMetrics fm = getFontMetrics(g.getFont());
        int w = fm.stringWidth(s);
        int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];
        g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE / 2 + h / 2);
    }

    private int offsetCoors(int arg) {
        return arg * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN;
    }

    private Color getBackground(int value) {
        return switch (value) {
            case 2 -> new Color(0xeee4da);
            case 4 -> new Color(0xede0c8);
            case 8 -> new Color(0xf2b179);
            case 16 -> new Color(0xf59563);
            case 32 -> new Color(0xf67c5f);
            case 64 -> new Color(0xf65e3b);
            case 128 -> new Color(0xedcf72);
            case 256 -> new Color(0xedcc61);
            case 512 -> new Color(0xedc850);
            case 1024 -> new Color(0xedc53f);
            case 2048 -> new Color(0xedc22e);
            default -> new Color(0xcdc1b4);
        };
    }

    private Color getForeground(int value) {
        return value < 16 ? new Color(0x776e65) : new Color(0xf9f6f2);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> move(Direction.LEFT);
            case KeyEvent.VK_RIGHT -> move(Direction.RIGHT);
            case KeyEvent.VK_UP -> move(Direction.UP);
            case KeyEvent.VK_DOWN -> move(Direction.DOWN);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    enum Direction {
        LEFT(0), UP(1), RIGHT(2), DOWN(3);
        final int rotations;
        Direction(int r) { this.rotations = r; }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("2048 Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new Game2048());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
