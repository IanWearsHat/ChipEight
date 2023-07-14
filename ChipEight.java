import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class ChipEight extends JPanel implements Runnable {
    private static final long FRAME_DELAY = 1000/60L;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    private Renderer renderer;
    private Keyboard keyboard;
    private Speaker speaker;

    public ChipEight() {
        this.renderer = new Renderer(10);
        this.keyboard = new Keyboard();
        this.speaker = new Speaker();

        this.keyboard.bind(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); // clears the screen before every draw
        Graphics2D g2d = (Graphics2D) g;

        this.renderer.draw(g2d); // calls the renderer to handle rendering pixels
    }

    @Override
    public void run() {
        while (true) {
            requestFocusInWindow();
            repaint();
            // this.cpu.cycle();
            this.keyboard.testKeys();

            try {
                Thread.sleep(FRAME_DELAY);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        ChipEight chipEight = new ChipEight();

        frame.add(chipEight);
        frame.setTitle("Chip8 Emulator");
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();

        chipEight.run();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
    }
}
