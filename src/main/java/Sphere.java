import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Sphere extends Obj {


    private Vector3D position;
    private double radius;


    public Sphere(double x, double y, double z, double radius, double KaR, double KaG, double KaB, double KdR, double KdG, double KdB, double KsR, double KsG, double KsB, double KrR, double KrG, double KrB) {
        position = new Vector3D(x, y, z);
        this.radius = radius;
        setMaterial(new Material("sphere-material", new ArrayRealVector(new double[]{KaR, KaG, KaB}), new ArrayRealVector(new double[]{KdR, KdG, KdB}), new ArrayRealVector(new double[]{KsR, KsG, KsB}), new ArrayRealVector(new double[]{KrR, KrG, KrB})));
    }

    public Sphere(String[] parts) {
        this(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Double.parseDouble(parts[5]), Double.parseDouble(parts[6]), Double.parseDouble(parts[7]), Double.parseDouble(parts[8]), Double.parseDouble(parts[9]), Double.parseDouble(parts[10]), Double.parseDouble(parts[11]), Double.parseDouble(parts[12]), Double.parseDouble(parts[13]), Double.parseDouble(parts[14]), Double.parseDouble(parts[15]), Double.parseDouble(parts[16]));
    }

    public Vector3D getPosition() {
        return position;
    }

}
