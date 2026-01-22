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

        // 2. "Inkräktaren" - BÖRJAR EFTER 10 SEKUNDER
        if (frameCount > 600) { // Ca 10 sekunder (600 frames vid 60fps)
            g2d.setColor(new Color(150, 150, 150, 20));
            g2d.fillRect(intruderX, 250, 60, 60);
            if (intruderX < getWidth() + 100) {
                intruderX += 0.5;
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
            "varje gång den