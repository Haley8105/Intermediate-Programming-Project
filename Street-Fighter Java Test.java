import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SimpleFightingGame extends JPanel implements Runnable, KeyListener {
    // Game window and physics constants
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int FLOOR = 250;

    // Game state
    private Thread gameThread;
    private boolean running = false;
    private String gameOverMessage = "";

    // The two fighters
    private Fighter p1;
    private Fighter p2;

    // Keyboard state tracking
    private boolean wPressed, aPressed, dPressed;
    private boolean upPressed, leftPressed, rightPressed;

    public SimpleFightingGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        addKeyListener(this);

        // Initialize fighters (x, y, color, facingRight)
        p1 = new Fighter(100, FLOOR, Color.RED, true);
        p2 = new Fighter(650, FLOOR, Color.BLUE, false);
    }

    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Main game loop (runs at roughly 60 Frames Per Second)
        while (running) {
            update();
            repaint();
            try {
                Thread.sleep(16); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Stop updating if someone won
        if (!gameOverMessage.isEmpty()) return;

        // Player 1 movement
        if (aPressed) p1.moveLeft();
        else if (dPressed) p1.moveRight();
        if (wPressed) p1.jump();

        // Player 2 movement
        if (leftPressed) p2.moveLeft();
        else if (rightPressed) p2.moveRight();
        if (upPressed) p2.jump();

        // Apply gravity and update screen boundaries
        p1.updatePhysics(FLOOR, WIDTH);
        p2.updatePhysics(FLOOR, WIDTH);

        // Auto-face opponents
        if (p1.x < p2.x) {
            p1.facingRight = true;
            p2.facingRight = false;
        } else {
            p1.facingRight = false;
            p2.facingRight = true;
        }

        // Update attack animation cooldowns
        p1.updateAttack();
        p2.updateAttack();

        // Check Win Conditions
        if (p1.health <= 0) gameOverMessage = "Player 2 Wins!";
        if (p2.health <= 0) gameOverMessage = "Player 1 Wins!";
    }

    private void handleAttack(Fighter attacker, Fighter defender) {
        if (attacker.isAttacking || !gameOverMessage.isEmpty()) return; // Prevent spamming
        attacker.attack();

        // Simple collision detection for the punch
        int attackRange = 70;
        int verticalRange = 80;
        
        if (Math.abs(attacker.x - defender.x) < attackRange && 
            Math.abs(attacker.y - defender.y) < verticalRange) {
            
            // Apply damage and a small knockback effect
            defender.health -= 10;
            defender.x += attacker.facingRight ? 30 : -30;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the ground
        g.setColor(new Color(50, 50, 50));
        g.fillRect(0, FLOOR + 100, WIDTH, HEIGHT - FLOOR);

        // Draw the fighters
        p1.draw(g);
        p2.draw(g);

        // Draw Health Bar Backgrounds (Red)
        g.setColor(Color.RED);
        g.fillRect(50, 20, 300, 20);
        g.fillRect(450, 20, 300, 20);
        
        // Draw Health Bar Foreground (Green)
        g.setColor(Color.GREEN);
        g.fillRect(50, 20, Math.max(0, p1.health) * 3, 20);
        // P2 health bar drains towards the right for a mirrored UI look
        g.fillRect(450 + (100 - Math.max(0, p2.health)) * 3, 20, Math.max(0, p2.health) * 3, 20);

        // Draw Player Labels
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Player 1", 50, 15);
        g.drawString("Player 2", 700, 15);

        // Display Game Over Text
        if (!gameOverMessage.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.YELLOW);
            FontMetrics metrics = g.getFontMetrics();
            int x = (WIDTH - metrics.stringWidth(gameOverMessage)) / 2;
            g.drawString(gameOverMessage, x, HEIGHT / 2);
        }
    }

    // --- Keyboard Controls ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // P1 Controls
        if (key == KeyEvent.VK_A) aPressed = true;
        if (key == KeyEvent.VK_D) dPressed = true;
        if (key == KeyEvent.VK_W) wPressed = true;
        if (key == KeyEvent.VK_F) handleAttack(p1, p2);

        // P2 Controls
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_ENTER) handleAttack(p2, p1);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A) aPressed = false;
        if (key == KeyEvent.VK_D) dPressed = false;
        if (key == KeyEvent.VK_W) wPressed = false;

        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_UP) upPressed = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    // --- Main Method ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Java Street Fighter");
        SimpleFightingGame game = new SimpleFightingGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
        game.startGame();
    }

    // --- Inner Class for Fighter Logic ---
    class Fighter {
        int x, y;
        int health = 100;
        Color color;
        boolean facingRight;
        
        int velocityY = 0;
        boolean isJumping = false;
        boolean isAttacking = false;
        int attackTimer = 0;

        final int WIDTH = 50;
        final int HEIGHT = 100;
        final int SPEED = 6;
        final int JUMP_STRENGTH = -16;
        final int GRAVITY = 1;

        public Fighter(int x, int y, Color color, boolean facingRight) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.facingRight = facingRight;
        }

        public void moveLeft() { x -= SPEED; }
        public void moveRight() { x += SPEED; }
        
        public void jump() {
            if (!isJumping) {
                velocityY = JUMP_STRENGTH;
                isJumping = true;
            }
        }

        public void updatePhysics(int floorY, int screenWidth) {
            // Apply gravity
            y += velocityY;
            if (y < floorY) {
                velocityY += GRAVITY;
            } else {
                y = floorY;
                velocityY = 0;
                isJumping = false;
            }
            
            // Keep fighters inside screen bounds
            if (x < 0) x = 0;
            if (x > screenWidth - WIDTH) x = screenWidth - WIDTH;
        }

        public void attack() {
            isAttacking = true;
            attackTimer = 12; // How long the punch stays visible
        }

        public void updateAttack() {
            if (attackTimer > 0) {
                attackTimer--;
            } else {
                isAttacking = false;
            }
        }

        public void draw(Graphics g) {
            // Draw body
            g.setColor(color);
            g.fillRect(x, y, WIDTH, HEIGHT);

            // Draw a yellow rectangle to represent a punch
            if (isAttacking) {
                g.setColor(Color.YELLOW);
                if (facingRight) {
                    g.fillRect(x + WIDTH, y + 20, 35, 15);
                } else {
                    g.fillRect(x - 35, y + 20, 35, 15);
                }
            }
        }
    }
}