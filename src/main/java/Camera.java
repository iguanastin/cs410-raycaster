import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

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
        PrintWriter writer = new PrintWriter(output);

        writer.println("P3");
        writer.println(width + " " + height + " 255");

        //Create camera vars
        final Vector3D eyeV = new Vector3D(eye.getEntry(0), eye.getEntry(1), eye.getEntry(2));
        final Vector3D lookV = new Vector3D(look.getEntry(0), look.getEntry(1), look.getEntry(2));
        final Vector3D upV = new Vector3D(up.getEntry(0), up.getEntry(1), up.getEntry(2));
        final double near = -d;

        Vector3D wv = eyeV.subtract(lookV).normalize();
        Vector3D uv = upV.crossProduct(wv).normalize();
        Vector3D vv = wv.crossProduct(uv);

        // Iterate over pixels left to right, top to bottom
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                RealVector color = new ArrayRealVector(new double[]{0, 0, 0});

                // TODO: Find raycast vector
                double px = col / (width - 1.0) * (xmax - xmin) + xmin;
//                final double py = row / (height - 1.0) * (ymax - ymin) + ymin; // Bottom to top
                double py = row / (height - 1.0) * (ymin - ymax) + ymax; // Top to bottom

                Vector3D pixpt = eyeV.add(near, wv).add(px, uv).add(py, vv);
                Vector3D shoot = pixpt.subtract(eyeV).normalize();

                // TODO: Fire raycast onto all meshes
                // Nearest impact
                double nearest = Double.MAX_VALUE;
                Model nearestModel = null;
                int[] nearestFace = null;

                for (Model model : getScene().getModels()) {
                    for (int[] face : model.getFaces()) {

                        RealVector temp = model.getVertex(face[0]);
                        final double ax = temp.getEntry(0), ay = temp.getEntry(1), az = temp.getEntry(2);
                        temp = model.getVertex(face[1]);
                        final double bx = temp.getEntry(0), by = temp.getEntry(1), bz = temp.getEntry(2);
                        temp = model.getVertex(face[2]);
                        final double cx = temp.getEntry(0), cy = temp.getEntry(1), cz = temp.getEntry(2);
                        final double dx = shoot.getX(), dy = shoot.getY(), dz = shoot.getZ();
                        final double lx = eyeV.getX(), ly = eyeV.getY(), lz = eyeV.getZ();

                        double mmdet = ((az - cz) * dy - (ay - cy) * dz) * (ax - bx) -
                                ((az - cz) * dx - (ax - cx) * dz) * (ay - by) +
                                ((ay - cy) * dx - (ax - cx) * dy) * (az - bz);
                        double beta = ((az - cz) * dy - (ay - cy) * dz) * (ax - lx) -
                                ((az - cz) * dx - (ax - cx) * dz) * (ay - ly) +
                                ((ay - cy) * dx - (ax - cx) * dy) * (az - lz);
                        beta = beta / mmdet;
                        double gamma = ((az - lz) * dy - (ay - ly) * dz) * (ax - bx) -
                                ((az - lz) * dx - (ax - lx) * dz) * (ay - by) +
                                ((ay - ly) * dx - (ax - lx) * dy) * (az - bz);
                        gamma = gamma / mmdet;
                        double t = ((ay - ly) * (az - cz) - (ay - cy) * (az - lz)) * (ax - bx) -
                                ((ax - lx) * (az - cz) - (ax - cx) * (az - lz)) * (ay - by) -
                                ((ax - lx) * (ay - cy) - (ax - cx) * (ay - ly)) * (az - bz);
                        t = t / mmdet;

                        if (beta >= 0 && gamma >= 0 && beta + gamma <= 1 && t > 0 && t < nearest) {
                            nearest = t;
                            nearestFace = face;
                            nearestModel = model;
                        }
                    }
                }

                // TODO: Calculate color at nearest impact
                if (nearest != Double.MAX_VALUE) {
                    color = new ArrayRealVector(new double[]{0.5, 0.5, 0.5});
                }

                writer.println((int)(color.getEntry(0) * 255) + " " + (int)(color.getEntry(1) * 255) + " " + (int)(color.getEntry(2) * 255));
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
