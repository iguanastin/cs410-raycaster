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
        long time = System.currentTimeMillis();

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

                double px = col / (width - 1.0) * (xmax - xmin) + xmin;
                double py = row / (height - 1.0) * (ymin - ymax) + ymax; // Top to bottom

                Vector3D pixpt = eyeV.add(near, wv).add(px, uv).add(py, vv);
                Vector3D shoot = pixpt.subtract(eyeV).normalize();

                // ------------------------------------ Raycast all meshes ---------------------------------------------

                // Nearest impact
                double nearest = Double.MAX_VALUE;
                Model nearestModel = null;
                Vector3D nearestNormal = null;

                for (Model model : getScene().getModels()) {
                    for (int[] face : model.getFaces()) {

                        RealVector temp = model.getVertex(face[0]);
                        final double ax = temp.getEntry(0), ay = temp.getEntry(1), az = temp.getEntry(2);
                        temp = model.getVertex(face[1]);
                        final double bx = temp.getEntry(0), by = temp.getEntry(1), bz = temp.getEntry(2);
                        temp = model.getVertex(face[2]);
                        final double cx = temp.getEntry(0), cy = temp.getEntry(1), cz = temp.getEntry(2);
                        final double dx = shoot.getX(), dy = shoot.getY(), dz = shoot.getZ();
                        final double lx = pixpt.getX(), ly = pixpt.getY(), lz = pixpt.getZ();

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
                            Vector3D v1 = new Vector3D(ax, ay, az);
                            Vector3D v2 = new Vector3D(bx, by, bz);
                            Vector3D v3 = new Vector3D(cx, cy, cz);

                            Vector3D normal = v1.subtract(v2).crossProduct(v1.subtract(v3)).normalize();
                            if (normal.dotProduct(shoot) > 0) {
                                nearest = t;
                                nearestModel = model;
                                nearestNormal = normal;
                            }
                        }
                    }
                }

                if (nearest != Double.MAX_VALUE) {
                    color = ambient.ebeMultiply(nearestModel.getMaterial().getKa());

                    Vector3D impact = pixpt.add(nearest, shoot);

                    for (Light light : getScene().getLights()) {
                        Vector3D lightPos = new Vector3D(light.getPos().getEntry(0), light.getPos().getEntry(1), light.getPos().getEntry(2));
                        if (light.isInfinite()) lightPos = lightPos.add(impact);
                        Vector3D lightToPointVector = lightPos.subtract(impact).normalize();
                        double cosTheta = nearestNormal.negate().dotProduct(lightToPointVector);
                        if (cosTheta > 0) {
                            color = color.add(nearestModel.getMaterial().getKd().ebeMultiply(light.getColor()).mapMultiplyToSelf(cosTheta));
                        }
                    }
                }

                //Convert and write pixel to file
                writer.println((int)(color.getEntry(0) * 255) + " " + (int)(color.getEntry(1) * 255) + " " + (int)(color.getEntry(2) * 255));
            }
        }

        writer.close();
        System.out.println("Render Time: " + (System.currentTimeMillis() - time)/1000.0 + "s");
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
