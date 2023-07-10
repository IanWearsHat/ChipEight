import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class ChipEight extends Frame {
    public ChipEight() {
        super("Chip8 Emulator");

        setSize(500, 500);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLUE);
        g2d.drawRect(75,75,300,200);
    }

    public static void main(String args[]) {
        new ChipEight();
    }
}
