import javax.swing.JPanel;
import java.awt.event.*;
import java.util.HashMap;
import java.util.function.IntConsumer;  

public class Keyboard implements KeyListener {
    HashMap<Character, Integer> KEYMAP = new HashMap<>();
    boolean[] keysPressed = new boolean[16];
    IntConsumer onNextKeyPress = null;

    public Keyboard() {
        KEYMAP.put('1', 0x1);
        KEYMAP.put('2', 0x2);
        KEYMAP.put('3', 0x3);
        KEYMAP.put('4', 0xc);
        KEYMAP.put('Q', 0x4);
        KEYMAP.put('W', 0x5);
        KEYMAP.put('E', 0x6);
        KEYMAP.put('R', 0xD);
        KEYMAP.put('A', 0x7);
        KEYMAP.put('S', 0x8);
        KEYMAP.put('D', 0x9);
        KEYMAP.put('F', 0xE);
        KEYMAP.put('Z', 0xA);
        KEYMAP.put('X', 0x0);
        KEYMAP.put('C', 0xB);
        KEYMAP.put('V', 0xF);
    }

    public boolean isKeyPressed(int keyCode) {
        return this.keysPressed[keyCode];
    }

    public void keyTyped(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            if (this.KEYMAP.containsKey(c)) {
                int key = this.KEYMAP.get(c);
                this.keysPressed[key] = true;

                if (this.onNextKeyPress != null) {
                    this.onNextKeyPress.accept(key);
                    this.onNextKeyPress = null;
                }
            }
        }
    }
    
    public void keyReleased(KeyEvent e) {
        int id = e.getID();
        if (id == KeyEvent.KEY_RELEASED) {
            char c = e.getKeyChar();
            if (this.KEYMAP.containsKey(c)) {
                int key = this.KEYMAP.get(c);
                this.keysPressed[key] = false;
            }
        }
    }

    public void keyPressed(KeyEvent e) {}

    public void bind(JPanel panel) {
        panel.addKeyListener(this);
    }
}
