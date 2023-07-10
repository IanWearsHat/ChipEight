public class Renderer {
    int cols;
    int rows;

    int scale;

    int ctx;

    int[] display;

    public Renderer(int scale) {
        this.cols = 64; // resolution width
        this.rows = 32; // resolution height

        this.scale = scale;

        this.display = new int[this.cols * this.rows];
    }
}
