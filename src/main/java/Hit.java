import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Hit {

    private final double distance;
    private final Obj obj;
    private final Vector3D normal;
    private final Vector3D impact;


    public Hit(Obj obj, Vector3D normal, Vector3D impact, double distance) {
        this.distance = distance;
        this.obj = obj;
        this.normal = normal;
        this.impact = impact;
    }

    public Obj getObj() {
        return obj;
    }

    public double getDistance() {
        return distance;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public Vector3D getImpact() {
        return impact;
    }

}
