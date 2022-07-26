import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.LinkedList;
import java.util.Collections;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 680;
    static final int SCREEN_HEIGHT = 800;
    static final int UNIT_SIZE = 40;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 75;

    private boolean running = false;
    private boolean pause;
    private Timer timer;

    private int boardWidth = SCREEN_WIDTH / UNIT_SIZE - 7;
    private int boardHeight = SCREEN_HEIGHT / UNIT_SIZE;
    private int[][] board;
    private int passedFrames;
    private int requiredFrames = 4;
    private LinkedList<Integer> nextTetrominoes;
    private int currentPiece;
    private int currentRotation;
    private int currentX, currentY;
    private int startX = 5, startY = 0;

    private final int[][][][] tetrominoes = {
            {
                    // Z tetromino
                    { { 0, 0 }, { 0, 1 }, { -1, 1 }, { -1, 2 } }, { { -1, 0 }, { 0, 0 }, { 0, 1 }, { 1, 1 } },
                    { { 1, 0 }, { 1, 1 }, { 0, 1 }, { 0, 2 } }, { { -1, 1 }, { 0, 1 }, { 0, 2 }, { 1, 2 } }
            },
            {
                    // S tetromino
                    { { -1, 0 }, { -1, 1 }, { 0, 1 }, { 0, 2 } }, { { -1, 1 }, { 0, 1 }, { 0, 0 }, { 1, 0 } },
                    { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 2 } }, { { -1, 2 }, { 0, 2 }, { 0, 1 }, { 1, 1 } },
            },
            {
                    // T tetromino
                    { { 0, 0 }, { 0, 1 }, { 0, 2 }, { -1, 1 } }, { { 0, 0 }, { 0, 1 }, { -1, 1 }, { 1, 1 } },
                    { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 0, 2 } }, { { -1, 1 }, { 0, 1 }, { 1, 1 }, { 0, 2 } },
            },
            {
                    // O tetromino
                    { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } }, { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },
                    { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } }, { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },
            },
            {
                    // L tetromino
                    { { -1, 1 }, { 0, 1 }, { 1, 1 }, { -1, 2 } }, { { -1, 0 }, { 0, 0 }, { 0, 1 }, { 0, 2 } },
                    { { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 } }, { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 1, 2 } },
            },
            {
                    // J tetromino
                    { { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } }, { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 1, 0 } },
                    { { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 2 } }, { { 0, 0 }, { 0, 1 }, { 0, 2 }, { -1, 2 } },
            },
            {
                    // I Tetromino
                    { { -2, 1 }, { -1, 1 }, { 0, 1 }, { 1, 1 } }, { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 0, 3 } },
                    { { -2, 2 }, { -1, 2 }, { 0, 2 }, { 1, 2 } }, { { -1, 0 }, { -1, 1 }, { -1, 2 }, { -1, 3 } }
            }
    };

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        board = new int[boardHeight][boardWidth];
        startGame();
    }

    private void startGame() {
        pause = false;
        passedFrames = 0;
        nextTetrominoes = new LinkedList<>();
        createNextBag();
        getNextTetromino();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void move(int direction) {
        updateBoard(0);
        currentX += direction;
        if (checkOutOfBounds() || checkCollision()) {
            currentX -= direction;
        }
        updateBoard(1);
    }

    private void changeRotation(int value) {
        updateBoard(0);
        currentRotation += value;
        currentRotation = currentRotation % 4;
        if (currentRotation < 0) {
            currentRotation = 3;
        }
        if (checkOutOfBounds() || checkCollision()) {
            currentRotation -= value;
            currentRotation = currentRotation % 4;
            if (currentRotation < 0) {
                currentRotation = 3;
            }
        }
        updateBoard(1);
    }

    private void drop() {
        updateBoard(0);
        currentY++;
        if (checkCollision()) {
            currentY--;
            updateBoard(1);
            checkCompletion();
            getNextTetromino();
            return;
        }
        updateBoard(1);
    }

    private void updateBoard(int updateValue) {
        for (int i = 0; i < 4; i++) {
            board[currentY + tetrominoes[currentPiece][currentRotation][i][1]][currentX
                    + tetrominoes[currentPiece][currentRotation][i][0]] = updateValue;
        }
    }

    private boolean checkCollision() {
        for (int i = 0; i < 4; i++) {
            if (currentY + tetrominoes[currentPiece][currentRotation][i][1] >= boardHeight) {
                return true;
            }
            if (board[currentY + tetrominoes[currentPiece][currentRotation][i][1]][currentX
                    + tetrominoes[currentPiece][currentRotation][i][0]] == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOutOfBounds() {
        for (int i = 0; i < 4; i++) {
            int temp = currentX + tetrominoes[currentPiece][currentRotation][i][0];
            if (temp < 0 || temp >= boardWidth) {
                return true;
            }
        }
        return false;
    }

    private void getNextTetromino() {
        currentPiece = nextTetrominoes.removeFirst();
        if (nextTetrominoes.isEmpty()) {
            createNextBag();
        }
        spawnTetromino();
    }

    private void createNextBag() {
        for (int i = 0; i < 28; i++) {
            nextTetrominoes.add(i % 7);
        }
        Collections.shuffle(nextTetrominoes);
    }

    private void spawnTetromino() {
        currentX = startX;
        currentY = startY;
        currentRotation = 0;
        if (checkCollision()) {
            running = false;
            return;
        }
        updateBoard(1);
    }

    private int checkCompletion() {
        int count = 0;
        for (int i = board.length - 1; i >= 0; i--) {
            boolean flag = true;
            for (int j = 0; j < board[i].length; j++) {
                if (!flag) {
                    break;
                }
                if (board[i][j] != 1) {
                    flag = false;
                }
            }
            if (flag) {
                count++;
                for (int j = 0; j < board[i].length; j++) {
                    board[i][j] = 0;
                }
                slideDown(i);
                i++;
            }
        }
        return count;
    }

    private void slideDown(int startPosition) {
        for (int i = startPosition; i > 1; i--) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = board[i - 1][j];
            }
        }
        for (int i = 0; i < board[0].length; i++) {
            board[0][i] = 0;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            drawGame(g);
        } else if (pause) {
            drawMenu(g);
        } else {
            drawGameOver(g);
        }
    }

    public void drawGame(Graphics g) {
        // Drawing lines to make where the tetrominoes are droping more clear
        for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
            g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
        }
        // Drawing the border of the board
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, UNIT_SIZE, UNIT_SIZE * boardHeight);
        g.fillRect((boardWidth + 1) * UNIT_SIZE, 0, UNIT_SIZE, UNIT_SIZE * boardHeight);
        // Drawing the board itself
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j] == 1) {
                    g.fillRect((j + 1) * UNIT_SIZE, i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                }
            }
        }
        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        g.drawString("Next", 13 * UNIT_SIZE, 4 * UNIT_SIZE);
        g.setColor(Color.WHITE);
        int next = nextTetrominoes.getFirst();
        for (int i = 0; i < 4; i++) {
            g.fillRect((14 + tetrominoes[next][0][i][0]) * UNIT_SIZE, (5 + tetrominoes[next][0][i][1]) * UNIT_SIZE,
                    UNIT_SIZE, UNIT_SIZE);
        }
    }

    public void drawMenu(Graphics g) {
        // Drawing the pause screen
        // In the pause screen board can not be seen to prevent taking more time than
        // required
        String text = "Pause";
        g.setColor(Color.blue);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString(text, (SCREEN_WIDTH - metrics2.stringWidth(text)) / 2, SCREEN_HEIGHT / 2);
    }

    public void drawGameOver(Graphics g) {
        String text = "GAME OVER";
        g.setColor(Color.blue);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString(text, (SCREEN_WIDTH - metrics2.stringWidth(text)) / 2, SCREEN_HEIGHT / 2);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (running) {
            if (passedFrames >= requiredFrames) {
                passedFrames = 0;
                drop();
            }
            passedFrames++;
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_P:
                    running = !running;
                    pause = !pause;
                    break;
                case KeyEvent.VK_LEFT:
                    move(-1);
                    break;
                case KeyEvent.VK_RIGHT:
                    move(1);
                    break;
                case KeyEvent.VK_DOWN:
                    drop();
                    break;
                case KeyEvent.VK_Z:
                    changeRotation(-1);
                    break;
                case KeyEvent.VK_X:
                    changeRotation(1);
                    break;
            }
        }
    }
}
