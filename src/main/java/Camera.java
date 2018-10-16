import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Camera {

    private Scene scene;

    private RealVector eye;
    private RealVector look;
    private RealVector up;
    private double d;
    private double xmin, ymin, xmax, ymax;
    private int width, height;
    private RealVector ambient;


    public Camera(double x, double y, double z) {
        eye = new ArrayRealVector(new double[]{x, y, z});
    }

    public void renderPPM(File output) throws FileNotFoundException {
        // Height rows, width columns, 3 ints per pixel for color
        short[][][] rows = new short[height][width][3];
        PrintWriter writer = new PrintWriter(output);

        writer.println("P3");
        writer.println(width + " " + height + " 255");

        // Iterate over pixels left to right, top to bottom
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[0].length; col++) {

                // TODO: Find raycast vector

                // TODO: Fire raycast onto all meshes

                // TODO: Calculate color at nearest impact

                rows[row][col][0] = 0; // Red [0-255]
                rows[row][col][1] = 0; // Green [0-255]
                rows[row][col][2] = 0; // Blue [0-255]

                writer.println(rows[row][col][0] + " " + rows[row][col][1] + " " + rows[row][col][2]);
            }
        }

        writer.close();
    }

    public void setLook(double x, double y, double z) {
        look = new ArrayRealVector(new double[]{x, y, z});
    }

    public void setUp(double x, double y, double z) {
        up = new ArrayRealVector(new double[]{x, y, z});
    }

    public void setD(double d) {
        this.d = d;
    }

    public void setBounds(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setAmbient(double r, double g, double b) {
        ambient = new ArrayRealVector(new double[]{r, g, b});
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    @Override
    public String toString() {
        return "Camera:" + "\n" +
                "  eye: " + eye + "\n" +
                "  look: " + look + "\n" +
                "  up: " + up + "\n" +
                "  d: " + d + "\n" +
                "  bounds: [(" + xmin + ", " + ymin + "), (" + xmax + ", " + ymax + ")]" + "\n" +
                "  res: (" + width + ", " + height + ")" + "\n" +
                "  ambient: " + ambient;
    }
}
