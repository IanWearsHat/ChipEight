import javax.swing.JPanel;
import java.awt.event.*;
import java.util.HashMap;
import java.util.function.IntConsumer;  

public class Keyboard implements KeyListener {
    HashMap<Character, Integer> KEYMAP = new HashMap<>();
    boolean[] keysPressed = new boolean[16]; // size because of the number of keys on the chip8 keypad
    IntConsumer onNextKeyPress = null;

    public Keyboard() {
        /* Maps keys on QWERTY keyboard to Chip8 keyboard 
         * Keys pressed must be lowercase, meaning no caps lock
        */
        KEYMAP.put('1', 0x1);
        KEYMAP.put('2', 0x2);
        KEYMAP.put('3', 0x3);
        KEYMAP.put('4', 0xc);
        KEYMAP.put('q', 0x4);
        KEYMAP.put('w', 0x5);
        KEYMAP.put('e', 0x6);
        KEYMAP.put('r', 0xD);
        KEYMAP.put('a', 0x7);
        KEYMAP.put('s', 0x8);
        KEYMAP.put('d', 0x9);
        KEYMAP.put('f', 0xE);
        KEYMAP.put('z', 0xA);
        KEYMAP.put('x', 0x0);
        KEYMAP.put('c', 0xB);
        KEYMAP.put('v', 0xF);
    }

    public boolean isKeyPressed(int keyCode) {
        // potential for a keycode to be passed that is out of bounds, meaning > 15
        return this.keysPressed[keyCode];
    }

    public void testKeys() {
        System.out.print("\033[H\033[2J");
        for (int i = 0; i < this.keysPressed.length; i++) {
            if (i % 4 == 0) {
                System.out.println();
            }
            System.out.print(this.keysPressed[i] + "\t");
        }
        System.out.flush();
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

    /* Binds this Keyboard to a panel, only used to bind to the ChipEight class */
    public void bind(JPanel panel) {
        panel.addKeyListener(this);
    }
}
