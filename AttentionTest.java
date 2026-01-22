import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class AttentionTest extends JPanel implements ActionListener {
    private int ballCount = 5;
    private int greenBounces = 0;
    private ArrayList<Ball> balls = new ArrayList<>();
    private Timer timer;
    
    // Variabler för de dolda elementen (förändringsblindhet)
    private float backgroundHue = 0.6f; // Startar som blåaktig
    private int intruderX = -100;

    public AttentionTest() {
        Random rand = new Random();
        
        // Skapa bollar, varav en är grön (målet)
        balls.add(new Ball(100, 100, 3, 4, Color.GREEN, true));
        
        // Skapa de vita störningsmomenten
        for (int i = 0; i < ballCount - 1; i++) {
            balls.add(new Ball(rand.nextInt(500), rand.nextInt(400), 
                      rand.nextInt(5) + 2, rand.nextInt(5) + 2, Color.WHITE, false));
        }

        // Timer för uppdatering av grafiken (ca 60 bilder per sekund)
        timer = new Timer(16, this);
        timer.start();
        
        // Visa instruktioner direkt vid start
        JOptionPane.showMessageDialog(null, 
            "UPPGIFT: Räkna hur många gånger den GRÖNA bollen studsar mot väggarna.\n" +
            "Håll fokus, testet tar ca 15 sekunder.");
    }

    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    // 1. Ändra bakgrunden långsamt (Change Blindness)
    setBackground(Color.getHSBColor(backgroundHue, 0.5f, 0.8f));
    if (backgroundHue < 0.9f) {
        backgroundHue += 0.0002f; 
    }

    // 2. "Inkräktaren" (Inattentional Blindness)
    g2d.setColor(new Color(150, 150, 150, 100)); // Semitransparent grå
    g2d.fillRect(intruderX, 250, 80, 80);
    if (intruderX < getWidth() + 100) {
        intruderX += 1;
    }

    // 3. Rita alla bollar (Här har vi slagit ihop logiken)
    for (Ball b : balls) {
        g2d.setColor(b.color);
        if (b.isTarget) {
            // Den gröna bollen är alltid en perfekt cirkel
            g2d.fillOval(b.x, b.y, 30, 30); 
        } else {
            // De vita bollarna ändras gradvis till kvadrater (Change Blindness)
            int roundness = Math.max(0, 30 - (int)((backgroundHue - 0.6f) * 100)); 
            g2d.fillRoundRect(b.x, b.y, 30, 30, roundness, roundness);
        }
    }
}

    @Override
    public void actionPerformed(ActionEvent e) {
        // Uppdatera bollarnas position och kolla studsar
        for (Ball b : balls) {
            b.move(getWidth(), getHeight());
            if (b.isTarget && b.hitWall) {
                greenBounces++;
                b.hitWall = false; // Återställ flaggan efter räkning
            }
        }
        repaint(); // Be systemet rita om skärmen
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Uppmärksamhetstest");
        AttentionTest test = new AttentionTest();
        
        frame.add(test);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Centrera fönstret
        frame.setVisible(true);

        // Timer för att avsluta testet efter 15 sekunder
        new Timer(15000, e -> {
            test.timer.stop();
            JOptionPane.showMessageDialog(frame, "Testet klart!\n\n" +
                "Din räkning av gröna studsar: " + test.greenBounces + "\n\n" +
                "MEN: Märkte du att...\n" +
                "1. Bakgrunden ändrade färg från blå till rosa?\n" +
                "2. En grå fyrkant gled långsamt genom mitten av skärmen?");
            System.exit(0);
        }).start();
    }

    // Inre klass för att hantera bollarnas logik
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
            // Studsa mot tak/golv
            if (y <= 0 || y >= height - 30) { 
                dy *= -1; 
                if(isTarget) hitWall = true; 
            }
        }
    }
}