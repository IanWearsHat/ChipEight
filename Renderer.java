import java.awt.Color;
import java.awt.Graphics2D;

public class Renderer {
    int cols;
    int rows;

    int scale;

    int[] display;

    public Renderer(int scale) {
        this.cols = 64; // resolution width
        this.rows = 32; // resolution height

        this.scale = scale; // scale the pixels up so that each pixel isn't actually one pixel on the screen.

        this.display = new int[this.cols * this.rows]; //one-dim array to hold all pixels' values
    }

    public boolean setPixel(int x, int y) {
        // if a pixel is out of bounds, wrap it around
        if (x > this.cols) {
            x -= this.cols;
        }
        else if (x < 0) {
            x += this.cols;
        }

        if (y > this.rows) {
            y -= this.rows;
        }
        else if (y < 0) {
            y += this.rows;
        }

        int pixelLoc = x + (y * this.cols);

        this.display[pixelLoc] ^= 1; // pixels are XORed onto the screen

        return this.display[pixelLoc] == 1; // if true, pixel was erased. if false, pixel was not erased.
    }

    public void clear() {
        this.display = new int[this.cols * this.rows];
    }

    public void testDraw() {
        this.setPixel(0, 0);
        this.setPixel(64, 2);
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < this.cols * this.rows; i++) {
            int x = (i % this.cols) * this.scale;
            int y = (int) (Math.floor(i / this.cols) * this.scale);
            System.out.println(i);

            if (this.display[i] == 1) {
                g.setColor(new Color(54, 56, 64));
                g.fillRect(x, y, this.scale, this.scale);
            }
        }
    }
}
