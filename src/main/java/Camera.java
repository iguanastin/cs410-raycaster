import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

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
    private int recursionLevel;
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
        double near = -d;
        if (near > 0) near = -near;

        Vector3D wv = eyeV.subtract(lookV).normalize();
        Vector3D uv = upV.crossProduct(wv).normalize();
        Vector3D vv = wv.crossProduct(uv);

        // Iterate over pixels left to right, top to bottom
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double px = col / (width - 1.0) * (xmax - xmin) + xmin;
                double py = row / (height - 1.0) * (ymin - ymax) + ymax; // Top to bottom

                Vector3D pixpt = eyeV.add(near, wv).add(px, uv).add(py, vv);
                Vector3D shoot = pixpt.subtract(eyeV).normalize();

                // ------------------------------------ Raycast all meshes ---------------------------------------------

                Hit hit = raycast(pixpt, shoot);

                RealVector color = new ArrayRealVector(new double[]{0, 0, 0});

                if (hit != null) {
                    color = computeColor(hit);
                }

                //Convert and write pixel to file
                writer.println((int) (color.getEntry(0) * 255) + " " + (int) (color.getEntry(1) * 255) + " " + (int) (color.getEntry(2) * 255));
            }
        }

        writer.close();
        System.out.println("Render Time: " + (System.currentTimeMillis() - time) / 1000.0 + "s");
    }

    private RealVector computeColor(Hit hit) {
        RealVector color = ambient.ebeMultiply(hit.getObj().getMaterial().getKa());

        for (Light light : getScene().getLights()) {
            Vector3D lightPos = new Vector3D(light.getPos().getEntry(0), light.getPos().getEntry(1), light.getPos().getEntry(2));
            if (light.isInfinite()) lightPos = lightPos.add(hit.getImpact());

            //Get vector from light to impact
            Vector3D lightDirection = hit.getImpact().subtract(lightPos).normalize();

            double cosTheta = hit.getNormal().dotProduct(lightDirection);
            if (cosTheta > 0.000001) {
                Hit hit2 = raycast(hit.getImpact(), lightDirection.negate());
                if (hit2 == null) {
                    color = color.add(hit.getObj().getMaterial().getKd().ebeMultiply(light.getColor()).mapMultiply(cosTheta));

                    if (hit.getObj().getMaterial().getKs() != null) {
                        Vector3D toC = hit.getDirection().subtract(lightDirection).normalize();
                        Vector3D spR = hit.getNormal().scalarMultiply(2 * cosTheta).subtract(lightDirection);
                        double CdR = toC.dotProduct(spR);
                        if (CdR > 0.000001) {
                            color = color.add(hit.getObj().getMaterial().getKs().ebeMultiply(light.getColor()).mapMultiply(Math.pow(CdR, 16)));
                        }
                    }
                }
            }
        }

        return color;
    }

    private Hit raycast(Vector3D origin, Vector3D direction) {
        direction = direction.normalize();
        double nearest = Double.MAX_VALUE;
        Obj nearestObj = null;
        Vector3D nearestNormal = null;

        for (Obj obj : getScene().getObjs()) {
            if (obj instanceof Sphere) {
                Sphere sphere = (Sphere) obj;

                final Vector3D Tv = sphere.getPosition().subtract(origin);
                final double v = Tv.dotProduct(direction);
                final double csq = Tv.dotProduct(Tv);
                final double disc = sphere.getRadius() * sphere.getRadius() - (csq - v * v);

                if (disc < 0) continue;
                final double t = v - Math.sqrt(disc);

                if (t > 0.0001 && t < nearest) {
                    nearest = t;
                    nearestObj = sphere;
                    nearestNormal = sphere.getPosition().subtract(origin.add(nearest, direction)).normalize();
                }
            } else if (obj instanceof Model) {
                Model model = (Model) obj;
                int i = 0;
                for (int[] face : model.getFaces()) {
                    // Initialize a, b, c, d, and l. (x,y,z)
                    RealVector temp = model.getVertex(face[2]);
                    final double ax = temp.getEntry(0), ay = temp.getEntry(1), az = temp.getEntry(2);
                    temp = model.getVertex(face[1]);
                    final double bx = temp.getEntry(0), by = temp.getEntry(1), bz = temp.getEntry(2);
                    temp = model.getVertex(face[0]);
                    final double cx = temp.getEntry(0), cy = temp.getEntry(1), cz = temp.getEntry(2);
                    final double dx = direction.getX(), dy = direction.getY(), dz = direction.getZ();
                    final double lx = origin.getX(), ly = origin.getY(), lz = origin.getZ();

                    //Compute mmdet, beta, gamma, and t
                    double mmdet = ((az - cz) * dy - (ay - cy) * dz) * (ax - bx) - ((az - cz) * dx - (ax - cx) * dz) * (ay - by) + ((ay - cy) * dx - (ax - cx) * dy) * (az - bz);
                    double beta = ((az - cz) * dy - (ay - cy) * dz) * (ax - lx) - ((az - cz) * dx - (ax - cx) * dz) * (ay - ly) + ((ay - cy) * dx - (ax - cx) * dy) * (az - lz);
                    beta /= mmdet;
                    double gamma = ((az - lz) * dy - (ay - ly) * dz) * (ax - bx) - ((az - lz) * dx - (ax - lx) * dz) * (ay - by) + ((ay - ly) * dx - (ax - lx) * dy) * (az - bz);
                    gamma /= mmdet;
                    double t = ((ay - ly) * (az - cz) - (ay - cy) * (az - lz)) * (ax - bx) - ((ax - lx) * (az - cz) - (ax - cx) * (az - lz)) * (ay - by) + ((ax - lx) * (ay - cy) - (ax - cx) * (ay - ly)) * (az - bz);
                    t /= mmdet;

                    //Test for collision
                    if (beta >= 0 && gamma >= 0 && beta + gamma <= 1 && t > 0.0001 && t < nearest) {
                        //Construct 3 vertices of the face
                        Vector3D v1 = new Vector3D(ax, ay, az);
                        Vector3D v2 = new Vector3D(bx, by, bz);
                        Vector3D v3 = new Vector3D(cx, cy, cz);

                        //Compute normal
                        Vector3D normal = v1.subtract(v2).crossProduct(v1.subtract(v3)).normalize();

                        //Invert normal if it's not facing the source
                        if (normal.dotProduct(direction) < -0.0001) {
                            normal = normal.negate();
                        }

                        //Set current nearest impact
                        nearest = t;
                        nearestObj = model;
                        nearestNormal = normal;
                    }
                }
            }
        }

        if (nearestObj != null) {
            return new Hit(nearestObj, origin, direction, nearestNormal, origin.add(nearest, direction), nearest);
        } else {
            return null;
        }
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

    public void setRecursionLevel(int level) {
        this.recursionLevel = level;
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
