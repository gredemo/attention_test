import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class AttentionTest extends JPanel implements ActionListener {
    private int ballCount = 7; // 1 grön + 6 vita = 7 totalt
    private int greenBounces = 0;
    private int userClicks = 0;
    private ArrayList<Ball> balls = new ArrayList<>();
    private Timer timer;
    private JButton clickButton;
    
    // Variabler för de dolda elementen
    private float backgroundHue = 0.6f;
    private int intruderX = -100;
    private int frameCount = 0;

    public AttentionTest() {
        setLayout(new BorderLayout());
        
        Random rand = new Random();
        
        // Skapa bollar - 1 grön + 6 vita
        balls.add(new Ball(100, 100, 3, 4, Color.GREEN, true));
        
        for (int i = 0; i < ballCount - 1; i++) {
            balls.add(new Ball(rand.nextInt(500), rand.nextInt(400), 
                      rand.nextInt(5) + 2, rand.nextInt(5) + 2, Color.WHITE, false));
        }

        // Skapa klick-knapp
        clickButton = new JButton("KLICKA HÄR VID GRÖN STUDS!");
        clickButton.setFont(new Font("Arial", Font.BOLD, 18));
        clickButton.setPreferredSize(new Dimension(300, 50));
        clickButton.setFocusPainted(false);
        clickButton.setBackground(new Color(100, 200, 100));
        clickButton.addActionListener(e -> {
            userClicks++;
            // Ge visuell feedback
            clickButton.setBackground(Color.GREEN);
            Timer flashTimer = new Timer(100, evt -> {
                clickButton.setBackground(new Color(100, 200, 100));
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.add(clickButton);
        add(buttonPanel, BorderLayout.SOUTH);

        timer = new Timer(16, this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Ändra bakgrunden långsamt
        setBackground(Color.getHSBColor(backgroundHue, 0.5f, 0.8f));
        if (backgroundHue < 0.9f) {
            backgroundHue += 0.0002f; 
        }

        // 2. "Inkräktaren" - BÖRJAR EFTER 3 SEKUNDER
if (frameCount > 180) { // Ca 3 sekunder (180 frames vid 60fps)
    g2d.setColor(new Color(100, 100, 100, 80)); // Mörkare grå, högre opacitet
    g2d.fillRect(intruderX, 250, 70, 70); // Större: 80x80
    if (intruderX < getWidth() + 200) { // Ändrat från +100 till +200 för att åka längre
        intruderX += 1.2; // Snabbare: 1.2 pixels/frame
    }
}

        // 3. Rita alla bollar
        for (Ball b : balls) {
            g2d.setColor(b.color);
            if (b.isTarget) {
                g2d.fillOval(b.x, b.y, 30, 30); 
            } else {
                int roundness = Math.max(0, 30 - (int)((backgroundHue - 0.6f) * 100)); 
                g2d.fillRoundRect(b.x, b.y, 30, 30, roundness, roundness);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frameCount++;
        
        for (Ball b : balls) {
            b.move(getWidth(), getHeight());
            if (b.isTarget && b.hitWall) {
                greenBounces++;
                b.hitWall = false;
            }
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Uppmärksamhetstest");
        AttentionTest test = new AttentionTest();
        
        frame.add(test);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // VISA INSTRUKTIONER
        JOptionPane.showMessageDialog(frame, 
            "UPPGIFT: Klicka på den gröna knappen LÄNGST NER\n" +
            "varje gång den GRÖNA bollen studsar mot en vägg.\n\n" +
            "Testet tar 15 sekunder.\n\n" +
            "Tryck OK för att börja!");
        
        test.timer.start();

        new Timer(20000, e -> {  // 20 sekunder istället för 15
            test.timer.stop();
            test.clickButton.setEnabled(false);
            
            int diff = Math.abs(test.greenBounces - test.userClicks);
            String accuracy = diff == 0 ? "Perfekt!" : 
                            diff <= 2 ? "Mycket bra!" : 
                            diff <= 5 ? "Ganska bra" : "Svårt att hänga med!";
            
            JOptionPane.showMessageDialog(frame, 
                "Testet klart!\n\n" +
                "Faktiska gröna studsar: " + test.greenBounces + "\n" +
                "Dina klick: " + test.userClicks + "\n" +
                "Differens: " + diff + " (" + accuracy + ")\n\n" +
                "MEN: Märkte du att...\n" +
                "1. Bakgrunden ändrade färg från blå till rosa?\n" +
                "2. En grå fyrkant gled långsamt genom mitten av skärmen?\n" +
                "3. De vita bollarna förvandlades gradvis till kvadrater?");
            System.exit(0);
        }).start();
    }

    class Ball {
        int x, y, dx, dy;
        Color color;
        boolean isTarget, hitWall = false;

        Ball(int x, int y, int dx, int dy, Color c, boolean target) {
            this.x = x; 
            this.y = y; 
            this.dx = dx; 
            this.dy = dy;
            this.color = c; 
            this.isTarget = target;
        }

        void move(int width, int height) {
    x += dx; 
    y += dy;
    
    // Studsa mot höger/vänster vägg
    if (x <= 0 || x >= width - 30) { 
        dx *= -1; 
        if(isTarget) hitWall = true; 
    }
    // Studsa mot tak/golv - MINUS 100 för att ge plats åt knappen
    if (y <= 0 || y >= height - 100) { 
        dy *= -1; 
        if(isTarget) hitWall = true; 
    }
}
}
}